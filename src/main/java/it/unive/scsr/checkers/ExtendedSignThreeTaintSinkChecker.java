package it.unive.scsr.checkers;

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
import it.unive.scsr.analysis.extendedsign.signtaint.ExtendedSignThreeTaint;
import it.unive.scsr.analysis.extendedsign.signtaint.ExtendedSignThreeTaintLattice;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Checker per "ExtendedSign × ThreeTaint" che emette un warning in caso di una variabile "tainted" passata ad una funzione sink:
 * - viene emesso [DEFINITE] qualora il valore sia certamente "tainted" (quindi TAINTED);
 * - viene emesso [POSSIBLE TAINED] qualora il valore potrebbe essere "tainted" (quindi TOP);
 * In entrambi i casi viene fornita anche l'informazione sul segno.
 * Nessun warning viene emesso se la variabile è "clean" (CLEAN)
 */
public class ExtendedSignThreeTaintSinkChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<
                SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {
        if (node instanceof UnresolvedCall) {
            UnresolvedCall uc = (UnresolvedCall) node;
            for (var res : tool.getResultOf(graph)) {
                try {
                    Call resolved = tool.getResolvedVersion(uc, res);
                    if (resolved instanceof NativeCall) {
                        for (NativeCFG n : ((NativeCall) resolved).getTargetedConstructs()) {
                            process(tool, uc, resolved, n.getDescriptor(), res);
                        }
                    } else if (resolved instanceof CFGCall) {
                        for (CFG n : ((CFGCall) resolved).getTargetedCFGs()) {
                            process(tool, uc, resolved, n.getDescriptor(), res);
                        }
                    }
                } catch (SemanticException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private void process(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> tool,
            UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
            AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> res) {

        if (!descriptor.getAnnotations().contains(ExtendedSignThreeTaint.SINK_MATCHER)) {
            return;
        }

        for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
            Expression par = uc.getParameters()[i];
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> postState =
                    res.getAnalysisStateAfter(par);

            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> iter = postState.getExecutionExpressions().iterator();
            if (!iter.hasNext()) {
                continue;
            }

            SymbolicExpression boolExpr = iter.next();
            try {
                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, (Statement) uc).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, (Statement) uc);
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType())) {
                        continue;
                    }

                    ValueEnvironment<ExtendedSignThreeTaintLattice> valueState = postState.getExecutionState().valueState;
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    ExtendedSignThreeTaint domain = (ExtendedSignThreeTaint) tool.getAnalysis().domain.valueDomain;
                    ExtendedSignThreeTaintLattice value = domain.eval(valueState, (ValueExpression) s, (ProgramPoint) uc, oracle);

                    String sign = value.sign.representation().toString();
                    if (value.isAlwaysTainted()) {
                        tool.warnOn(uc, "[TAINTED] sign=" + sign + " at " + par.getLocation());
                    } else if (value.isPossiblyTainted()) {
                        tool.warnOn(uc, "[POSSIBLE TAINTED] sign=" + sign + " at " + par.getLocation());
                    }
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }
}
