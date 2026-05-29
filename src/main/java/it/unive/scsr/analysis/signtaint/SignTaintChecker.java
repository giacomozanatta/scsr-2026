package it.unive.scsr.analysis.signtaint;

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

public class SignTaintChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<
            SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>,
            SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<
                SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (!(node instanceof UnresolvedCall)) return true;
        UnresolvedCall uc = (UnresolvedCall) node;

        for (var res : tool.getResultOf(graph)) {
            try {
                Call resolved = tool.getResolvedVersion(uc, res);
                if (resolved instanceof NativeCall) {
                    for (NativeCFG n : ((NativeCall) resolved).getTargetedConstructs())
                        process(tool, uc, resolved, n.getDescriptor(), res);
                } else if (resolved instanceof CFGCall) {
                    for (CFG n : ((CFGCall) resolved).getTargetedCFGs())
                        process(tool, uc, resolved, n.getDescriptor(), res);
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void process(
            SemanticTool<
                SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> tool,
            UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
            AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> res) {

        if (!descriptor.getAnnotations().contains(SignTaint.SINK_MATCHER)) return;

        int start = resolved.getCallType() == CallType.INSTANCE ? 1 : 0;
        for (int i = start; i < uc.getParameters().length; i++) {
            Expression par = uc.getParameters()[i];
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> postState
                = res.getAnalysisStateAfter(par);

            Set<SymbolicExpression> reachable = new HashSet<>();
            Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
            if (!it.hasNext()) continue;

            SymbolicExpression boolExpr = it.next();
            try {
                reachable.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, (Statement) uc).elements);

                for (SymbolicExpression s : reachable) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, (Statement) uc);
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType())) continue;

                    ValueEnvironment<SignTaintLattice> valueState = postState.getExecutionState().valueState;
                    SignTaint domain = (SignTaint) tool.getAnalysis().domain.valueDomain;
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    SignTaintLattice val = domain.eval(valueState, (ValueExpression) s, (ProgramPoint) uc, oracle);

                    if (val.taint.isAlwaysTainted())
                        tool.warnOn(uc, "Definite warning: tainted value [sign=" + val.sign.representation() + "] at sink");
                    else if (val.taint.isPossiblyTainted())
                        tool.warnOn(uc, "Possible warning: possibly-tainted value [sign=" + val.sign.representation() + "] at sink");
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }
}
