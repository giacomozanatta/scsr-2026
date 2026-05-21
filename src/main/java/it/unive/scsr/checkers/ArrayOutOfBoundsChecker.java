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
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;
import it.unive.lisa.imp.expressions.IMPArrayAccess;
import it.unive.lisa.imp.expressions.IMPNewArray;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.lattices.numeric.PentagonLattice;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ArrayOutOfBoundsChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> {

	private static final Interval intervalDomain = new Interval();

	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {

		if (node instanceof IMPArrayAccess)
			checkBounds(tool, graph, (IMPArrayAccess) node);

		return true;
	}

	private void checkBounds(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
			CFG graph, IMPArrayAccess access) {

		// Identify the array variable being accessed
		if (!(access.getLeft() instanceof VariableRef)) return;
		String arrayName = ((VariableRef) access.getLeft()).getName();

		// Find the IMPNewArray assignment for that variable in this CFG
		IMPNewArray newArray = null;
		for (Statement stmt : graph.getNodes()) {
			if (stmt instanceof Assignment) {
				Assignment assign = (Assignment) stmt;
				if (assign.getLeft() instanceof VariableRef
						&& ((VariableRef) assign.getLeft()).getName().equals(arrayName)
						&& assign.getRight() instanceof IMPNewArray) {
					newArray = (IMPNewArray) assign.getRight();
					break;
				}
			}
		}
		if (newArray == null) return;

		// getSubExpressions()[0] is the size expression, e.g. the "10" in new int[10]
		// We need this to compare the upper-bound of the interval against the size of the array to detect OOB
		Expression sizeExpr = newArray.getSubExpressions()[0];

		for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {

			// Extract the abstract intervals for the index and for the array size to compare them
			IntInterval idxInterval  = extractInterval(tool, res, access, access.getRight());
			IntInterval sizeInterval = extractInterval(tool, res, access, sizeExpr);

			if (idxInterval == null || sizeInterval == null) continue;

			// Resolve symbolic identifiers for the index and size variables so we
			// can consult the upper-bounds set component (pentagon.second) below
			AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>>
					idxPostState = res.getAnalysisStateAfter(access.getRight());
			Identifier idxId  = extractIdentifier(tool, idxPostState, access);
			Identifier sizeId = extractIdentifier(tool,
					res.getAnalysisStateAfter(sizeExpr), access);

			// Lower bound check : index must be >= 0
			if (idxInterval.getLow().lt(MathNumber.ZERO)) {
				if (idxInterval.getHigh().lt(MathNumber.ZERO))
					tool.warnOn(access, "Array index is definitely negative (out of bounds)");
				else
					tool.warnOn(access, "Array index may be negative (out of bounds)");
			}

			// Upper bound check : index must be < size
			// Before warning, use the idx's upper-bounds set (taking advantage of Pentagon Domain over Interval Domain)
			// to figure out if it proves idx < size relationally (example : from a loop condition i < n)
			if (idxInterval.getHigh().geq(sizeInterval.getLow()) && !provedSafeByUpperBounds(idxPostState, idxId, sizeId)) {
				if (idxInterval.getLow().geq(sizeInterval.getHigh()))
					tool.warnOn(access, "Array index is definitely >= array size (out of bounds)");
				else
					tool.warnOn(access, "Array index may be out of bounds >= array size (out of bounds)");
			}
		}
	}

	// Returns the first numeric Identifier found in the reachable set of target's post-state
	// Returns null if the expression has no direct identifier (compound expression or a literal constant)
	private Identifier extractIdentifier(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
			AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> postState,
			Statement node) {

		Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
		if (!it.hasNext()) return null;
		SymbolicExpression expr = it.next();
		try {
			Set<SymbolicExpression> reachable = new HashSet<>(
					tool.getAnalysis().reachableFrom(postState, expr, node).elements);
			for (SymbolicExpression s : reachable) {
				if (!(s instanceof Identifier)) continue;
				Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, node);
				if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType() || !t.isNumericType()))
					continue;
				return (Identifier) s;
			}
		} catch (SemanticException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Returns true if the upper bounds set (pentagon.second) of any
	// abstract value in postState definitively proves idxId < sizeId
	// in which case no upper-bound OOB warning should be emitted
	private boolean provedSafeByUpperBounds(
			AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> postState,
			Identifier idxId,
			Identifier sizeId) {

		if (idxId == null || sizeId == null)
			return false;

		Collection<PentagonLattice> pentagons =
				postState.getExecutionState().getAllLatticeInstances(PentagonLattice.class);

		for (PentagonLattice pentagon : pentagons)
			if (!pentagon.isBottom() && pentagon.second.getState(idxId).contains(sizeId))
				return true;

		return false;
	}

	private IntInterval extractInterval(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
			AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> res,
			Statement node, Expression target) {

		AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> postState =
				res.getAnalysisStateAfter(target);

		Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
		if (!it.hasNext()) return null;

		SymbolicExpression expr = it.next();
		try {
			Set<SymbolicExpression> reachable = new HashSet<>(
					tool.getAnalysis().reachableFrom(postState, expr, node).elements);

			SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());

			for (SymbolicExpression s : reachable) {
				Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, node);
				if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType() || !t.isNumericType()))
					continue;

				Collection<PentagonLattice> abstractValues =
						postState.getExecutionState().getAllLatticeInstances(PentagonLattice.class);
				for (PentagonLattice pentagon : abstractValues) {
					IntInterval interval = intervalDomain.eval(pentagon.first, (ValueExpression) s, node, oracle);
					if (interval != null && !interval.isBottom())
						return interval;
				}
			}
		} catch (SemanticException e) {
			e.printStackTrace();
		}
		return null;
	}
}