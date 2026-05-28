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
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.scsr.analysis.sign.taint.SignTaint;
import it.unive.scsr.analysis.sign.taint.SignTaintLattice;

public class PaymentAmountChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> {

	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {
		if (node instanceof Call) {
			Call call = (Call) node;
			if (isPaymentOperation(call.getTargetName()))
				checkAmount(tool, graph, call);
		}
		return true;
	}

	private boolean isPaymentOperation(String name) {
		return "pay".equals(name) || "withdraw".equals(name) || "transfer".equals(name) || "refund".equals(name);
	}

	private void checkAmount(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Call call) {
		int amountIndex = call.getCallType() == CallType.STATIC ? 0 : 1;
		if (call.getParameters().length <= amountIndex)
			return;

		Expression amount = call.getParameters()[amountIndex];

		for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> res : tool
				.getResultOf(graph))
			try {
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> beforeAmount = res
						.getAnalysisStateBefore(amount);
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> afterAmount = res
						.getAnalysisStateAfter(amount);
				ValueEnvironment<SignTaintLattice> valueState = beforeAmount.getExecutionState().valueState;
				SignTaint domain = (SignTaint) tool.getAnalysis().domain.valueDomain;
				SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(beforeAmount.getExecutionState());

				for (SymbolicExpression expr : afterAmount.getExecutionExpressions()) {
					if (!(expr instanceof ValueExpression))
						continue;
					SignTaintLattice value = domain.eval(valueState, (ValueExpression) expr, (ProgramPoint) amount, oracle);
					ExtendedSignLattice sign = value.getSign();

					if (sign.isDefinitelyNegative())
						tool.warnOn(call, "Definite warning: payment amount is negative at " + amount.getLocation());
					else if (sign.mayBeNegative())
						tool.warnOn(call, "Possible warning: payment amount may be negative at " + amount.getLocation());
				}
			} catch (SemanticException e) {
				e.printStackTrace();
			}
	}
}
