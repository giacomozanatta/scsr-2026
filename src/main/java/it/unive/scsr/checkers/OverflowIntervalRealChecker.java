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
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.scsr.analysis.interval.Interval;
import it.unive.scsr.analysis.interval.IntervalLattice;

public class OverflowIntervalRealChecker <H extends HeapValue<H>, T extends TypeValue<T>> implements
SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>> {

	private IntervalLattice representableReals;
	
	public OverflowIntervalRealChecker(double l, double u) {
		representableReals = new IntervalLattice(new MathNumber(l), new MathNumber(u));
	}
	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {
		
		if(node instanceof Addition || node instanceof IMPAddOrConcat || node instanceof Subtraction 
				|| node instanceof Multiplication || node instanceof Division) {
			checkOverflow(tool, graph, node);
		}
		return true;
	}
	
	private void checkOverflow(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {

		for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(node); // get post abstract state of denominator
			
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
							
						// valueDomain is the Interval/IntervalReal domain instance, not an IntervalLattice value
						Interval analysisValueDomain = (Interval) tool.getAnalysis().domain.valueDomain;
						
						IntervalLattice abstractValue = analysisValueDomain.eval(valueState, (ValueExpression) s,
								(ProgramPoint) node, oracle);
					
						if(!representableReals.includes(abstractValue)) {
							// overflow: high bound exceeds representable maximum
							boolean overflow = abstractValue.getHigh().gt(representableReals.getHigh()); 
							// underflow: low bound is below representable minimum
								boolean underflow = abstractValue.getLow().lt(representableReals.getLow());
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
