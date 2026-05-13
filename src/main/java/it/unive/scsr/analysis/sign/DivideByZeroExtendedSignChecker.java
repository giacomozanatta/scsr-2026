package it.unive.scsr.analysis.sign;
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
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.program.cfg.statement.numeric.Division;
//TASK REQUEST: To create 1 meaningful checker for detect a code property

//this checker detects possible and definite division by zero
//it works using the ExtendedSign domain to check if the denominator can be zero
//if it's zero we emit a definite warning if is leq ,geq zero of top we emit a possible warning
public class DivideByZeroExtendedSignChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> {
	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {

		if (node instanceof Division) {
			checkDivision(tool, graph, (Division) node);
		}
		return true;
	}

	private void checkDivision(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Division div) {

		for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> res : tool
				.getResultOf(graph)) {
			AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> postState = res
					.getAnalysisStateAfter(div.getRight()); // get post abstract state of denominator

			Set<SymbolicExpression> reachableIds = new HashSet<>();
			Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
			if (comExprIterator.hasNext()) {

				SymbolicExpression boolExpr = comExprIterator.next();
				try {
					reachableIds
							.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, div).elements);

					for (SymbolicExpression s : reachableIds) {
						Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, div);

						if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
							continue;
						// extraction of the abstract value
						var valueState = postState.getExecutionState().valueState;
						ExtendedSign domain = (ExtendedSign) tool.getAnalysis().domain.valueDomain;
						SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());

						ExtendedSignLattice abstractValue = domain.eval(valueState, (ValueExpression) s,
								(ProgramPoint) div, oracle);
						if (abstractValue.equals(ExtendedSignLattice.ZERO)) {
							tool.warnOn(div, "[WARNING] division by zero (denominator is zero)");
						} else if (abstractValue.equals(ExtendedSignLattice.LEQ_ZERO)
								|| abstractValue.equals(ExtendedSignLattice.GEQ_ZERO)
								|| abstractValue.equals(ExtendedSignLattice.TOP)) {
							tool.warnOn(div, "[POSSIBLE WARNING] zero is in the range of the denominator");
						}

					}
				} catch (SemanticException e) {
					e.printStackTrace();
				}
			}

		}

	}

}