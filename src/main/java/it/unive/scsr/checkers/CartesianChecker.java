package it.unive.scsr.checkers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.AnalyzedCFG;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.SimpleAbstractDomain;
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
import it.unive.scsr.analysis.cartesian.Cartesian;
import it.unive.scsr.analysis.cartesian.CartesianLattice;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class CartesianChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<
                SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (node instanceof UnresolvedCall uc) {
            for (var res : tool.getResultOf(graph)) {
                try {
                    Call resolved = tool.getResolvedVersion(uc, res);
                    if (resolved instanceof NativeCall nc) {
                        for (NativeCFG n : nc.getTargetedConstructs())
                            process(tool, uc, resolved, n.getDescriptor(), res);
                    } else if (resolved instanceof CFGCall cfg) {
                        for (CFG n : cfg.getTargetedCFGs())
                            process(tool, uc, resolved, n.getDescriptor(), res);
                    }
                } catch (SemanticException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private void process(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>> tool,
            UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
            AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>> res) {

        if (!descriptor.getAnnotations().contains(TaintThreeLevels.SINK_MATCHER))
            return;

        for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
            Expression par = uc.getParameters()[i];
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<CartesianLattice>, TypeEnvironment<T>>> postState =
                    res.getAnalysisStateAfter(par);

            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
            if (!it.hasNext())
                continue;

            SymbolicExpression boolExpr = it.next();
            try {
                reachableIds.addAll(
                        tool.getAnalysis().reachableFrom(postState, boolExpr, (Statement) uc).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, uc);
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                        continue;

                    ValueEnvironment<CartesianLattice> valueState = postState.getExecutionState().valueState;
                    Cartesian cartesianDomain = (Cartesian) tool.getAnalysis().domain.valueDomain;
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    CartesianLattice abstractValue = cartesianDomain.eval(valueState, (ValueExpression) s,
                            uc, oracle);

                    SignLattice sign = abstractValue.signLattice();
                    TaintThreeLevelsLattice taint = abstractValue.taintThreeLevelsLattice();

                    emitWarnings(tool, uc, par, sign, taint);
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }

    private void emitWarnings(
            SemanticTool<?, ?> tool,
            UnresolvedCall uc,
            Expression par,
            SignLattice sign,
            TaintThreeLevelsLattice taint) {

        String loc = par.getLocation().toString();
        boolean isTainted = taint == TaintThreeLevelsLattice.Taint;
        boolean isMaybeTainted = taint == TaintThreeLevelsLattice.Top;
        boolean isZero = sign == SignLattice.ZERO;
        boolean isNeg = sign == SignLattice.NEG;
        boolean isPos = sign == SignLattice.POS;
        boolean isSignTop = sign == SignLattice.TOP;

        // --- taint-only warnings ---
        if (isTainted)
            tool.warnOn(uc, "[TAINT] Definite taint reaches sink at: " + loc);
        if (isMaybeTainted)
            tool.warnOn(uc, "[TAINT] Possible taint reaches sink at: " + loc);

        // --- sign-only warnings ---
        if (isZero)
            tool.warnOn(uc, "[SIGN] Zero value reaches sink at: " + loc);
        if (isNeg)
            tool.warnOn(uc, "[SIGN] Negative value reaches sink at: " + loc);

        // --- combination warnings ---
        if (isTainted && isZero)
            tool.warnOn(uc, "[CARTESIAN] Definite tainted zero reaches sink at: " + loc);
        if (isTainted && isNeg)
            tool.warnOn(uc, "[CARTESIAN] Definite tainted negative value reaches sink at: " + loc);
        if (isTainted && isPos)
            tool.warnOn(uc, "[CARTESIAN] Definite tainted positive value reaches sink at: " + loc);
        if (isTainted && isSignTop)
            tool.warnOn(uc, "[CARTESIAN] Definite taint with unknown sign reaches sink at: " + loc);
        if (isMaybeTainted && isZero)
            tool.warnOn(uc, "[CARTESIAN] Possible tainted zero reaches sink at: " + loc);
        if (isMaybeTainted && isNeg)
            tool.warnOn(uc, "[CARTESIAN] Possible tainted negative value reaches sink at: " + loc);
    }
}