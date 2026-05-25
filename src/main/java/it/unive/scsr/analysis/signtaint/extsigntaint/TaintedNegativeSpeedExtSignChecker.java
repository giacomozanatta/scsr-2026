package it.unive.scsr.analysis.signtaint.extsigntaint;

import it.unive.lisa.analysis.*;
import it.unive.lisa.analysis.nonrelational.heap.HeapEnvironment;
import it.unive.lisa.analysis.nonrelational.heap.HeapValue;
import it.unive.lisa.analysis.nonrelational.type.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.type.TypeValue;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.scsr.analysis.sign.extsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Unified semantic check for the ExtendedSign x TaintThreeLevels product domain.
 * We will consider that "moveForward" functions to "setSpeed" are sinks and we want to
 * check both if the speed is correctly set non-negative, and if it's tainted or not.
 *
 * This check handles three concerns in a single pass:
 *
 * 1. SIGN check : in "moveForward*" functions, warns when the argument to
 *    "setSpeed" is or may be negative (mirrors NonNegativeSpeedInMoveForwardChecker,
 *    operating only on the sign component of the product domain).
 *
 * 2. TAINT-IN-SINK check : for any call to a sink annotated function, warns
 *    when any argument is tainted or possibly tainted (mirrors TaintThreeLevelsChecker,
 *    operating only on the taint component of the product domain).
 *
 * 3. COMBINED check : in "moveForward*" functions, additionally warns when the
 *    speed argument is both possibly negative AND possibly tainted simultaneously,
 *    leveraging the joint information available in the product domain.
 */
public class TaintedNegativeSpeedExtSignChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<
                SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            Statement node) {

        // Check 1 + 3: sign & combined analysis for setSpeed inside moveForward*
        if (graph.getDescriptor().getName().startsWith("moveForward")
                && node instanceof Call
                && ((Call) node).getTargetName().equals("setSpeed")) {
            checkSpeed(tool, graph, (Call) node);
        }

        // Check 2: general taint-in-sink analysis
        if (node instanceof UnresolvedCall uc) {
            checkSink(tool, graph, uc);
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // Check 1 + 3: sign and combined analysis for setSpeed in moveForward*
    // -------------------------------------------------------------------------

    private void checkSpeed(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            Call c) {

        for (var res : tool.getResultOf(graph)) {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> postState =
                    res.getAnalysisStateAfter(
                            c.getCallType() == CallType.STATIC ? c.getParameters()[0] : c.getParameters()[1]);

            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
            if (!it.hasNext()) continue;

            try {
                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, it.next(), c).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, c);
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                        continue;

                    ExtendedSignTaintLattice av       = evalProduct(tool, postState, s, c);
                    ExtendedSignLattice signPart  = av.first;
                    TaintThreeLevelsLattice  taintPart = av.second;
                    String fn = graph.getDescriptor().getName();

                    // Check 1: pure sign warning (mirrors NonNegativeSpeedInMoveForwardChecker)
                    if (signPart == ExtendedSignLattice.NEG)
                        tool.warnOn(c, "[SIGN] Speed is negative in " + fn);
                    else if (signPart == ExtendedSignLattice.NEGZERO)
                        tool.warnOn(c, "[SIGN] Speed is negative or zero in " + fn);
                    else if (signPart == ExtendedSignLattice.NOTZERO || signPart == ExtendedSignLattice.TOP)
                        tool.warnOn(c, "[SIGN] Speed may be negative in " + fn);

                    // Check 3: combined sign + taint warning
                    boolean possiblyNegative = signPart == ExtendedSignLattice.NEG
                            || signPart == ExtendedSignLattice.NEGZERO
                            || signPart == ExtendedSignLattice.NOTZERO
                            || signPart == ExtendedSignLattice.TOP;
                    boolean possiblyTainted  = taintPart.isAlwaysTainted() || taintPart.isPossiblyTainted();

                    if (possiblyNegative && possiblyTainted) {
                        if (signPart == ExtendedSignLattice.NEG && taintPart.isAlwaysTainted())
                            tool.warnOn(c, "[COMBINED/DEFINITE] Speed is tainted and negative in " + fn);
                        else if (signPart == ExtendedSignLattice.NEGZERO && taintPart.isAlwaysTainted())
                            tool.warnOn(c, "[COMBINED/DEFINITE] Speed is tainted and negative or zero in " + fn);
                        else
                            tool.warnOn(c, "[COMBINED/POSSIBLE] Speed may be tainted and negative in " + fn);
                    }
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 2: general taint-in-sink analysis (mirrors TaintThreeLevelsChecker)
    // -------------------------------------------------------------------------

    private void checkSink(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            UnresolvedCall uc) {

        for (var res : tool.getResultOf(graph)) {
            try {
                Call resolved = tool.getResolvedVersion(uc, res);
                if (resolved instanceof NativeCall) {
                    var nativeCfgs = ((NativeCall) resolved).getTargetedConstructs();
                    for (NativeCFG n :nativeCfgs)
                        processSink(tool, uc, resolved, n.getDescriptor(), res);
                } else if (resolved instanceof CFGCall cfg) {
                    for (CFG n : cfg.getTargetedCFGs())
                        processSink(tool, uc, resolved, n.getDescriptor(), res);
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }

    private void processSink(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> tool,
            UnresolvedCall uc,
            Call resolved,
            CodeMemberDescriptor descriptor,
            AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> res) {

        // called function is not annotated as a sink -> ignore
        if (!descriptor.getAnnotations().contains(TaintThreeLevels.SINK_MATCHER))
            return;

        // called function is a sink
        int start = resolved.getCallType() == CallType.INSTANCE ? 1 : 0;
        for (int i = start; i < uc.getParameters().length; i++) {
            Expression par = uc.getParameters()[i];
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> postState =
                    res.getAnalysisStateAfter(par); // compute the post state related to each parameter

            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
            if (!it.hasNext()) continue;

            try {
                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, it.next(), uc).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, uc);
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                        continue;

                    TaintThreeLevelsLattice taintPart = evalProduct(tool, postState, s, uc).second;

                    if (taintPart.isAlwaysTainted())
                        tool.warnOn(uc, "[TAINT/DEFINITE] Tainted value reaching sink at " + par.getLocation());
                    else if (taintPart.isPossiblyTainted())
                        tool.warnOn(uc, "[TAINT/POSSIBLE] Possibly tainted value reaching sink at " + par.getLocation());
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper: evaluate the product abstract value for a symbolic expression
    // -------------------------------------------------------------------------

    private ExtendedSignTaintLattice evalProduct(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> tool,
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignTaintLattice>, TypeEnvironment<T>>> postState,
            SymbolicExpression s,
            Statement node) throws SemanticException {

        ValueEnvironment<ExtendedSignTaintLattice> valueState = postState.getExecutionState().valueState;
        ExtendedSignTaint domain = (ExtendedSignTaint) tool.getAnalysis().domain.valueDomain;
        SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
        return domain.eval(valueState, (ValueExpression) s, (ProgramPoint) node, oracle);
    }
}