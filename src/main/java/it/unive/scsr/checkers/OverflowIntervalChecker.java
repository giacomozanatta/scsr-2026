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
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;
import it.unive.lisa.imp.expressions.IMPAddOrConcat;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.numeric.Addition;
import it.unive.lisa.program.cfg.statement.numeric.Division;
import it.unive.lisa.program.cfg.statement.numeric.Multiplication;
import it.unive.lisa.program.cfg.statement.numeric.Subtraction;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.numeric.IntInterval;

public class OverflowIntervalChecker <H extends HeapValue<H>, T extends TypeValue<T>> implements
SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> {

	private IntInterval representableIntegers;
	
	public OverflowIntervalChecker(int l, int u) {
		representableIntegers = new IntInterval(l, u);
	}
	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {
		
		if(node instanceof Addition || node instanceof IMPAddOrConcat || node instanceof Subtraction 
				|| node instanceof Multiplication || node instanceof Division) {
			checkOverflow(tool, graph, node);
		}
		return true;
	}
	
	private void checkOverflow(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {

		for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(node); // get post abstract state of denominator
			
				Set<SymbolicExpression> reachableIds = new HashSet<>();
				Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
				if (comExprIterator.hasNext()) {

					SymbolicExpression boolExpr = comExprIterator.next();
					try {
						reachableIds
								.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, node).elements);

						for (SymbolicExpression s : reachableIds) {
							Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, node);

							if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType() 
									|| !t.isNumericType())) // check only if the type is numerical
								continue;
							//extraction of the abstract value
							var valueState = postState.getExecutionState().valueState;

							SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
							
							Interval analysisValueDomain = (Interval) tool.getAnalysis().domain.valueDomain;
							
							IntInterval abstractValue = analysisValueDomain.eval(valueState, (ValueExpression) s,
									(ProgramPoint) node, oracle);
						
							if(!representableIntegers.includes(abstractValue)) {
								boolean overflow = abstractValue.getHigh().gt(representableIntegers.getHigh());
								boolean underflow = abstractValue.getLow().lt(representableIntegers.getLow());
								String sep = overflow && underflow ? "/" : "";
								tool.warnOn(node, "This is an " + (overflow ? "over" : "") + sep + (underflow ? "under" : "") + "flow");
							}
						}
					} catch (SemanticException e) {
						e.printStackTrace();
					}
				}

		}
		
	}

}
