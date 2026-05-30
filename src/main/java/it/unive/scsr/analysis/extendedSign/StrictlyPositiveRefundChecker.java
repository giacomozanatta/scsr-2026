package it.unive.scsr.analysis.extendedSign;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unive.lisa.analysis.AnalysisState;
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
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;


/**
 * @author Mattia Acquilesi - 896827
 * @author Alan Dal Col - 895879
 */
public class StrictlyPositiveRefundChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (graph.getDescriptor().getName().equals("processRefund")) {
            if (node instanceof Call) {
                Call c = (Call) node;

                if (c.getTargetName().equals("executeBankTransfer")) {
                    for (var res : tool.getResultOf(graph)) {

                        int paramIndex = (c.getCallType() == CallType.STATIC) ? 0 : 1;
                        AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> postState = res
                                .getAnalysisStateAfter(c.getParameters()[paramIndex]);

                        Set<SymbolicExpression> reachableIds = new HashSet<>();
                        Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();

                        if (comExprIterator.hasNext()) {
                            SymbolicExpression expr = comExprIterator.next();
                            try {
                                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, expr, (Statement) node).elements);

                                for (SymbolicExpression s : reachableIds) {
                                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, (Statement) node);

                                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                                        continue;

                                    ValueEnvironment<ExtendedSignLattice> valueState = postState.getExecutionState().valueState;
                                    ExtendedSign extendedSignDomain = (ExtendedSign) tool.getAnalysis().domain.valueDomain;
                                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                                    ExtendedSignLattice abstractValue = extendedSignDomain.eval(valueState, (ValueExpression) s, (ProgramPoint) node, oracle);

                                    if (abstractValue == ExtendedSignLattice.NEG || abstractValue == ExtendedSignLattice.NON_POS) {
                                        tool.warnOn(c, "CRITICAL: Logic Flaw! The system is processing a refund <= 0. Risk of an illegitimate charge to the customer");
                                    } else if (abstractValue == ExtendedSignLattice.ZERO) {
                                        tool.warnOn(c, "WARNING: Sending a zero-amount bank transfer. This wastes API calls and may cause exceptions on the payment gateway");
                                    } else if (abstractValue == ExtendedSignLattice.TOP || abstractValue == ExtendedSignLattice.NON_ZERO) {
                                        tool.warnOn(c, "INFO: The refund amount might not be strictly positive. Additional validation required before executing the transfer");
                                    }
                                }
                            } catch (SemanticException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}