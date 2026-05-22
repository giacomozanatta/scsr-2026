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
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.numeric.Division;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.scsr.analysis.interval_real.IntervalReal;
import it.unive.scsr.analysis.interval_real.IntervalRealLattice;

/**
 * DivByZeroIntervalRealChecker
 *
 * A semantic checker over the {@link IntervalReal} abstract domain that detects
 * potential division-by-zero errors at {@link Division} nodes.
 *
 * @param <H> heap abstract value type
 * @param <T> type abstract value type
 *
 * @author Gianmaria Pizzo 872966
 */
public class DivByZeroIntervalRealChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>,
        TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, TypeEnvironment<T>>> {

    /**
     * Visits each statement in the CFG. 
     * 
     * @note Delegates to {@link #checkDivision} only for
     *  {@link Division} nodes (all other statements are skipped).
     *
     * @param tool  the semantic analysis tool providing results and warning facilities
     * @param graph the CFG being visited
     * @param node  the current statement
     * @return {@code true} to continue visiting
     */
    @Override
    public boolean visit(
        SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>,
        TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>,
        TypeEnvironment<T>>> tool, CFG graph, Statement node
    ) {
        if (node instanceof Division) {
            checkDivision(tool, graph, (Division) node);
        }

        return true;
    }

    /**
     * Inspects the abstract value of the denominator at a division node and emits
     * warnings when the interval includes zero.
     *
     * @param tool  the semantic analysis tool
     * @param graph the CFG containing the division
     * @param div   the division statement to check
     */
    private void checkDivision(
        SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>,
        TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>,
        TypeEnvironment<T>>> tool, CFG graph, Division div
    ) {
        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
            // Post-state of the denominator expression, not the full division
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(div.getRight());
            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();

            if (!comExprIterator.hasNext()) {
                continue;
            }

            SymbolicExpression boolExpr = comExprIterator.next();

            try {
                // Resolve all symbolic expressions reachable from the denominator
                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, div).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, div);

                    // Only check numeric values
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType())){
                        continue;
                    }

                    // Extract the IntervalRealLattice abstract value for the denominator
                    var valueState = postState.getExecutionState().valueState;
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    IntervalReal domain = (IntervalReal) tool.getAnalysis().domain.valueDomain;
                    IntervalRealLattice abstractValue = domain.eval(valueState, (ValueExpression) s, (ProgramPoint) div, oracle);

                    // If BOTTOM then unreachable code and no warning needed
                    if (abstractValue.isBottom()) {
                        continue;
                    }

                    // Case 1: [0, 0] -> denominator is certainly zero
                    boolean defZero = abstractValue.getLow().isZero() && abstractValue.getHigh().isZero();

                    // low <= 0 <= high -> zero is in the interval, division may fail
                    boolean mayZero = abstractValue.getLow().leq(MathNumber.ZERO) && abstractValue.getHigh().geq(MathNumber.ZERO);

                    if (defZero){
                        tool.warnOn(div, "This is definitely a division by zero");
                    } else if (mayZero) {
                        tool.warnOn(div, "This may be a division by zero");
                    }
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }
}
