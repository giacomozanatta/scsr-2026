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
import it.unive.scsr.analysis.intervalreal.IntervalReal;
import it.unive.scsr.analysis.intervalreal.IntervalRealLattice;

/**
 * OverflowIntervalRealChecker
 *
 * @brief A semantic checker over the {@link IntervalReal} abstract domain that detects
 *  arithmetic overflow and underflow at binary numeric operations ({@code +}, {@code -},
 *  {@code *}, {@code /}).
 *
 * @note An overflow is detected when the upper bound of the result interval exceeds
 *  {@code rangeMax}. On the other hand, an underflow is detected when the lower bound 
 *  falls below {@code rangeMin}.
 *
 * @param <H> heap abstract value type
 * @param <T> type abstract value type
 *
 * @author Gianmaria Pizzo 872966
 */
public class OverflowIntervalRealChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
    SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, TypeEnvironment<T>>, 
    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, TypeEnvironment<T>>> {

    /** 
     * Lower bound of the representable value range. 
     */
    private final MathNumber rangeMin;

    /** 
     * Upper bound of the representable value range.
     */
    private final MathNumber rangeMax;

    /**
     * Constructs a checker for the given representable range {@code [min, max]}.
     *
     * @param min lower bound of the representable range (e.g., {@code -Float.MAX_VALUE})
     * @param max upper bound of the representable range (e.g., {@code Float.MAX_VALUE})
     */
    public OverflowIntervalRealChecker(double min, double max) {
        this.rangeMin = new MathNumber(min);
        this.rangeMax = new MathNumber(max);
    }

    /**
     * Visits each statement in the CFG. 
     * 
     * @note Delegates to {@link #checkOverflow} only for
     *  arithmetic operations ({@code +}, {@code -}, {@code *}, {@code /})
     *  (all other statements are skipped).
     *
     * @param tool  the semantic analysis tool providing results and warning facilities
     * @param graph the CFG being visited
     * @param node  the current statement
     * @return {@code true} to continue visiting
     */
    @Override
    public boolean visit(
        SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, 
        TypeEnvironment<T>>,SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, 
        TypeEnvironment<T>>> tool, CFG graph, Statement node
    ) {
        if (
            node instanceof Addition || 
            node instanceof IMPAddOrConcat || 
            node instanceof Subtraction || 
            node instanceof Multiplication || 
            node instanceof Division
        ) {
            checkOverflow(tool, graph, node);
        }

        return true;
    }

    /**
     * Inspects the abstract value of an arithmetic result and emits overflow/underflow
     * warnings when the interval escapes the representable range.
     *
     * @param tool  the semantic analysis tool
     * @param graph the CFG containing the arithmetic node
     * @param node  the arithmetic statement to check
     */
    private void checkOverflow(
        SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, 
        TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, 
        TypeEnvironment<T>>> tool, CFG graph, Statement node
    ) {

        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
            // Post-state of the full arithmetic expression (result interval)
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntervalRealLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(node);
            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
    
            if (!comExprIterator.hasNext()) {
                continue;
            }

            SymbolicExpression boolExpr = comExprIterator.next();

            try {
                // Resolve all symbolic expressions reachable from the result
                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, node).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, node);

                    // Skip non-numeric types (heap locations, pointer types)
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType() || !t.isNumericType())){
                        continue;
                    }

                    // Extract the IntervalRealLattice abstract value for the result
                    var valueState = postState.getExecutionState().valueState;
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    IntervalReal domain = (IntervalReal) tool.getAnalysis().domain.valueDomain;
                    IntervalRealLattice abstractValue = domain.eval(valueState, (ValueExpression) s, (ProgramPoint) node, oracle);

                    if (abstractValue.isBottom() || abstractValue.isTop()) {
                        // Skip to avoid false positives on unreachable code (BOTTOM) 
                        // and indeterminate results (TOP)
                        continue;
                    }

                    // Check if either bound escapes the representable range
                    boolean overflow  = abstractValue.getHigh().gt(rangeMax);
                    boolean underflow = abstractValue.getLow().lt(rangeMin);

                    if (overflow || underflow) {
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
