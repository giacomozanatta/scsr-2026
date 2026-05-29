package it.unive.scsr.checkers;

import it.unive.lisa.analysis.*;
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
import it.unive.lisa.lattices.numeric.PentagonLattice;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.BinaryExpression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.literal.Literal;
import it.unive.lisa.program.cfg.statement.numeric.Addition;
import it.unive.lisa.program.cfg.statement.numeric.Division;
import it.unive.lisa.program.cfg.statement.numeric.Multiplication;
import it.unive.lisa.program.cfg.statement.numeric.Subtraction;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.scsr.analysis.interval.realinterval.RealInterval;
import it.unive.scsr.analysis.interval.realinterval.RealIntervalLattice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class UselessMathOpChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> {

	private static final Logger LOG = LogManager.getLogger(UselessMathOpChecker.class);

	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {
		if (
				node instanceof Addition
						|| node instanceof Subtraction
						|| node instanceof Multiplication
						|| node instanceof Division
						|| node instanceof IMPAddOrConcat
		) {
			// LOG.debug("** Binary Expression | {}", ((BinaryExpression) node).getConstructName());
			getInterval(tool, graph, (BinaryExpression) node);
		}
		return true;
	}

	private void getInterval(SemanticTool<
			                         SimpleAbstractState<
					                         HeapEnvironment<H>,
					                         ValueEnvironment<PentagonLattice>,
					                         TypeEnvironment<T>>,
			                         SimpleAbstractDomain<
					                         HeapEnvironment<H>,
					                         ValueEnvironment<PentagonLattice>,
					                         TypeEnvironment<T>>> tool,
	                         CFG graph, BinaryExpression operation) {

		for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
			AnalysisState<SimpleAbstractState<
					HeapEnvironment<H>,
					ValueEnvironment<PentagonLattice>,
					TypeEnvironment<T>>> postStateExpr = res.getAnalysisStateAfter(operation); // Expression state
			var postStateLeft = res.getAnalysisStateAfter(operation.getLeft()); // Left operand
			var postStateRight = res.getAnalysisStateAfter(operation.getRight()); // Right operand

			// LOG.debug("POSTSTATE-EXP: {}", postStateExpr.toString());
			// LOG.debug("POSTSTATE-L: {}", postStateLeft.toString());
			// LOG.debug("POSTSTATE-R: {}", postStateRight.toString());

			// Set for all reachable IDs
			Set<SymbolicExpression> reachableIdsAll = new HashSet<>();
			// Set for IDs reachable from both left and right expressions of the binary expression
			Set<SymbolicExpression> reachableIdsComm = new HashSet<>();
			// Set for IDs reachable ONLY from left expressions of the binary expression
			Set<SymbolicExpression> reachableIdsLeft = new HashSet<>();
			// Set for IDs reachable ONLY from right expressions of the binary expression
			Set<SymbolicExpression> reachableIdsRight = new HashSet<>();
			// Get both sides iterators
			Iterator<SymbolicExpression> comExprIteratorL = postStateLeft.getExecutionExpressions().iterator();
			Iterator<SymbolicExpression> comExprIteratorR = postStateRight.getExecutionExpressions().iterator();
			if (comExprIteratorL.hasNext() && comExprIteratorR.hasNext()) {
				try {
					reachableIdsLeft.addAll(tool
							.getAnalysis()
							.reachableFrom(postStateExpr, comExprIteratorL.next(), operation)
							.elements
					);
					reachableIdsRight.addAll(tool
							.getAnalysis()
							.reachableFrom(postStateExpr, comExprIteratorR.next(), operation)
							.elements
					);
					// Union of left and right IDs
					reachableIdsAll.addAll(reachableIdsLeft);
					reachableIdsAll.addAll(reachableIdsRight);
					// Intersect left and right IDs
					reachableIdsComm.addAll(reachableIdsLeft);
					reachableIdsComm.retainAll(reachableIdsRight);
					// Intersect left and common IDs
					reachableIdsLeft.removeAll(reachableIdsComm);
					// Intersect right and common IDs
					reachableIdsRight.removeAll(reachableIdsComm);

					// Iterate over all IDs
					for (SymbolicExpression s : reachableIdsAll) {
						LOG.debug("***************");
						Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postStateExpr, s, operation);

						if (types.stream().allMatch(t ->
								t.isInMemoryType()
										|| t.isPointerType()
										|| !t.isNumericType()))
							continue;

						// LOG.debug("** Type: {}", types.toArray());

						//extraction of the abstract value
						Collection<PentagonLattice> abstractValues = postStateExpr
								.getExecutionState()
								.getAllLatticeInstances(PentagonLattice.class);

						// LOG.debug("** Abstract values set: {}", abstractValues.toArray());

						for(PentagonLattice a : abstractValues) {
							LOG.debug("** ID: {}", s.toString());

							ValueEnvironment<IntInterval> interval = a.first;
							IntInterval i;

							// Debug operand side
							if (reachableIdsLeft.contains(s))
								LOG.debug("<<<<< LEFT");
							else if (reachableIdsRight.contains(s))
								LOG.debug("RIGHT >>>>");
							else if (reachableIdsComm.contains(s))
								LOG.debug("<<<< EXTERNAL >>>>");

							// Check if ID is a constant or a variable
							if (s instanceof Constant c) {
								LOG.debug("** Constant: {}", c.toString());
								// If it is a number, build interval from the constant, otherwise skip
								if (c.getValue() instanceof Number n)
									i = new IntInterval(
											(new MathNumber(n.doubleValue())).roundDown(),
											(new MathNumber(n.doubleValue())).roundUp()
									);
								else
									continue;
							} else {
								i = interval.function.get(s); // If it is a variable, get the interval
							}
							LOG.debug("** {} > {} -> {} ****", interval.toString(), s.toString(), i);
							if (i != null) {
								LOG.debug("IS_ZERO: {} | IS_ONE: {}", i.is(0), i.is(1));
								if (operation instanceof IMPAddOrConcat && i.is(0)) {
									tool.warnOn(operation, "This addition is useless: adding zero");
								}
								if (operation instanceof Subtraction
										&& i.is(0)
										&& reachableIdsRight.contains(s)) {
									tool.warnOn(operation, "This subtraction is useless: subtracting zero");
								}
								if (operation instanceof Multiplication
										&& i.is(0)) {
									tool.warnOn(operation, "This multiplication is useless: multiplying by zero");
								}
								if (operation instanceof Multiplication
										&& i.is(1)) {
									tool.warnOn(operation, "This multiplication is useless: multiplying by one");
								}
								if (operation instanceof Division
										&& i.is(1)
										&& reachableIdsRight.contains(s)) {
									tool.warnOn(operation, "This division is useless: dividing by one");
								}
								if (operation instanceof Division
										&& i.is(0)
										&& reachableIdsLeft.contains(s)) {
									tool.warnOn(operation, "This division is useless: dividing zero");
								}
							}
						}

					}
				} catch (SemanticException e) {
					e.printStackTrace();
				}
			}

		}

	}

}
