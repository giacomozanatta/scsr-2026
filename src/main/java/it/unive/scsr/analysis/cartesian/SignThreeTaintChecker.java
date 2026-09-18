package it.unive.scsr.analysis.cartesian;
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
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
/** * Checker for the SignThreeTaint product domain. * * Detects calls to functions annotated as sinks and checks * whether their arguments are tainted. * * The sign component is not used by this checker: * only the taint component of SignThreeTaintLattice is inspected. * * @param <H> heap value type * @param <T> type value type */
public class SignThreeTaintChecker< H extends HeapValue<H>, T extends TypeValue<T>> implements SemanticCheck< SimpleAbstractState< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>> {
    @Override
    public boolean visit( SemanticTool< SimpleAbstractState< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>> tool, CFG graph, Statement node) {
        /* * We are interested only in unresolved calls, * since these are the calls that still need to be * resolved against their possible targets. */
        if (node instanceof UnresolvedCall) {
            UnresolvedCall uc = (UnresolvedCall) node;
            /* * Get all analysis results associated with the CFG. */
            for (var res : tool.getResultOf(graph)) {
                try {
                    Call resolved = tool.getResolvedVersion(uc, res);
                    /* * Native function call. */
                    if (resolved instanceof NativeCall) {
                        var nativeCfgs = ((NativeCall) resolved).getTargetedConstructs();
                        for (NativeCFG n : nativeCfgs)
                            process( tool, uc, resolved, n.getDescriptor(), res);
                        /* * User-defined CFG call. */}
                    else if (resolved instanceof CFGCall) {
                        CFGCall cfg = (CFGCall) resolved;
                        for (CFG n : cfg.getTargetedCFGs())
                            process( tool, uc, resolved, n.getDescriptor(), res); }
                } catch (SemanticException e) { e.printStackTrace(); }
            }
        } return true; }

    private void process( SemanticTool< SimpleAbstractState< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>> tool, UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor, AnalyzedCFG< SimpleAbstractState< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>> res) {
        /* * Check whether the called function is annotated as a sink. */
        if (!descriptor.getAnnotations().contains(TaintThreeLevels.SINK_MATCHER)) return;
        /* * For instance calls, parameter 0 is the receiver. * Therefore, actual arguments start from index 1. */
        int startIndex = resolved.getCallType() == CallType.INSTANCE ? 1 : 0;
        for (int i = startIndex; i < uc.getParameters().length; i++) {
            Expression par = uc.getParameters()[i];
            /* * Get the state immediately after evaluating * the parameter. */
            AnalysisState< SimpleAbstractState< HeapEnvironment<H>, ValueEnvironment<SignThreeTaintLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(par);
            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> iterator = postState.getExecutionExpressions().iterator();
            if (!iterator.hasNext()) continue;
            SymbolicExpression expression = iterator.next();
            try {
                /* * Find all symbolic expressions reachable from * the expression computed for the parameter. */
                reachableIds.addAll( tool.getAnalysis() .reachableFrom( postState, expression, (Statement) uc) .elements);
                for (SymbolicExpression s : reachableIds) {
                    /* * Ignore memory and pointer values. */
                    Set<Type> types = tool.getAnalysis() .getRuntimeTypesOf( postState, s, (Statement) uc);
                    if (types.stream().allMatch( t -> t.isInMemoryType() || t.isPointerType())) continue;
                    /* * Retrieve the value environment of * the SignThreeTaint product domain. */
                    ValueEnvironment<SignThreeTaintLattice> valueState = postState.getExecutionState().valueState;
                    /* * Retrieve the actual product value domain. */
                    SignThreeTaint signThreeTaint = (SignThreeTaint) tool.getAnalysis() .domain .valueDomain;
                    SemanticOracle oracle = tool.getAnalysis() .domain .makeOracle( postState.getExecutionState());
                    /* * Evaluate the symbolic expression in the * product domain. */
                    SignThreeTaintLattice abstractValue = signThreeTaint.eval( valueState, (ValueExpression) s, (ProgramPoint) uc, oracle);
                    /* * Extract the taint component. * * The abstract value has the form: * * [Sign, Taint] * */
                    TaintThreeLevelsLattice taint = abstractValue.getTaint();
                    /* * Definitely tainted. */
                    if (taint.isAlwaysTainted()) {
                        tool.warnOn( uc, "DEFINITE TAINT: value in a sink is " + "definitely tainted. Location: " + par.getLocation());
                        /* * Potentially tainted. */ }
                    else if (taint.equals( TaintThreeLevelsLattice.Top)) {
                        tool.warnOn( uc, "POSSIBLE TAINT: value in a sink might " + "be tainted (uncertainty). Location: " + par.getLocation()); }
                }
            } catch (SemanticException e) { e.printStackTrace(); }
        }
    }
}