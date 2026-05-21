package it.unive.scsr.analysis.intervalreal;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;


/**
 * IntervalReal Class
 * 
 * An implementation of {@link BaseNonRelationalValueDomain} that represents sets of real numbers as intervals [l, u]. Note that:
 *  - The interval is closed, meaning it includes its endpoints (for ease of implementation we do not consider open intervals);
 *  - The class supports basic arithmetic operations and handles special cases like division by zero by returning TOP;
 *  - Unlike intervals, rounding is not necessary for real numbers, as they can be represented with arbitrary precision; 
 *  - We still need to consider the possibility of infinite bounds.
 * 
 * @author Gianmaria Pizzo 872966
 */
public class IntervalReal implements BaseNonRelationalValueDomain<IntervalRealLattice> {

    /**
     * Returns the top element of the lattice, representing the interval (-∞, +∞).
     * 
     * @return the top element of the lattice
     */
    @Override
    public IntervalRealLattice top() {
        return IntervalRealLattice.TOP;
    }

    /**
     * Returns the bottom element of the lattice, representing an empty interval.
     * 
     * @return the bottom element of the lattice
     */
    @Override
    public IntervalRealLattice bottom() {
        return IntervalRealLattice.BOTTOM;
    }

    /**
     * Helper method to compute the minimum of multiple MathNumber instances.
     * 
     * @param nums the MathNumber instances to compare
     * 
     * @return the minimum value among the provided instances
     */
    private static MathNumber min(MathNumber... nums) {
        if (nums.length == 0){
			throw new IllegalArgumentException("No numbers provided");
        }

        MathNumber min = nums[0];

        for (int i = 1; i < nums.length; i++){
            min = min.min(nums[i]);
        }

        return min;
    }

    /**
     * Helper method to compute the maximum of multiple MathNumber instances.
     * 
     * @param nums the MathNumber instances to compare
     * 
     * @return the maximum value among the provided instances
     */
    private static MathNumber max(MathNumber... nums) {
        if (nums.length == 0){
            throw new IllegalArgumentException("No numbers provided");
        }

        MathNumber max = nums[0];

        for (int i = 1; i < nums.length; i++) {
            max = max.max(nums[i]);
        }

        return max;
    }

    /**
     * Evaluates a constant expression and returns the corresponding interval.
     * 
     * @note We automatically promote all numeric constants to double precision to maintain a uniform representation of intervals,
     * 
     * @param constant the constant expression to evaluate
     * @param pp the program point where the expression is evaluated
     * @param oracle the semantic oracle for handling semantic queries
     * 
     * @return the interval representing the constant value
     * 
     * @throws SemanticException if an error occurs during semantic evaluation
     */
    @Override
    public IntervalRealLattice evalConstant(
        Constant constant, ProgramPoint pp, SemanticOracle oracle
    ) throws SemanticException {
        Object value = constant.getValue();

        if (value instanceof Number) {
            // Promote to double for uniformity in interval representation
            Double n = ((Number) value).doubleValue();

            return new IntervalRealLattice(n, n);
        }

        return top();
    }

    /**
     * Evaluates a unary expression and returns the corresponding interval.
     * 
     * @param expression the unary expression to evaluate
     * @param arg the interval representing the argument
     * @param pp the program point where the expression is evaluated
     * @param oracle the semantic oracle for handling semantic queries
     * 
     * @return the interval representing the result of the unary expression
     * 
     * @throws SemanticException if an error occurs during semantic evaluation
     */
    @Override
    public IntervalRealLattice evalUnaryExpression(
        UnaryExpression expression, IntervalRealLattice arg, ProgramPoint pp, SemanticOracle oracle
    ) throws SemanticException {
        // If the argument is bottom, the result is also bottom
        // This equals checking for null intervals
        if (arg.isBottom()) {
            return bottom();
        }

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            MathNumber u = arg.getHigh();
			MathNumber l = arg.getLow();

            // -[l, u] => [-u, -l]
            return new IntervalRealLattice(
                u.multiply(MathNumber.MINUS_ONE), 
                l.multiply(MathNumber.MINUS_ONE)
            );
        }

        return top();
    }

    /**
     * Evaluates a binary expression and returns the corresponding interval.
     * 
     * @param expression the binary expression to evaluate
     * @param left the interval representing the left argument
     * @param right the interval representing the right argument
     * @param pp the program point where the expression is evaluated
     * @param oracle the semantic oracle for handling semantic queries
     * 
     * @return the interval representing the result of the binary expression
     * 
     * @throws SemanticException if an error occurs during semantic evaluation
     */
    @Override
    public IntervalRealLattice evalBinaryExpression(
        BinaryExpression expression, IntervalRealLattice left, IntervalRealLattice right, ProgramPoint pp, SemanticOracle oracle
    ) throws SemanticException {
        if (left.isBottom() || right.isBottom()){
            return bottom();
        }

        BinaryOperator operator = expression.getOperator();
        MathNumber l1 = left.getLow();
        MathNumber u1 = left.getHigh();
        MathNumber l2 = right.getLow();
        MathNumber u2 = right.getHigh();

        if (l1.isNaN() || u1.isNaN() || l2.isNaN() || u2.isNaN()) {
            return top();
        }

        if (operator instanceof AdditionOperator) {
            // Catch: (+∞) + (-∞) or (-∞) + (+∞)
            if (
                (l1.isPlusInfinity() && l2.isMinusInfinity()) || 
                (l1.isMinusInfinity() && l2.isPlusInfinity()) ||
                (u1.isPlusInfinity() && u2.isMinusInfinity()) || 
                (u1.isMinusInfinity() && u2.isPlusInfinity())
            ) {
                return top();
            }

            MathNumber lAdd = l1.add(l2);
            MathNumber uAdd = u1.add(u2);

            return new IntervalRealLattice(lAdd, uAdd);
        }

        if (operator instanceof SubtractionOperator) {
            // Catch: (+∞) - (+∞) or (-∞) - (-∞)
            if (
                (l1.isPlusInfinity() && u2.isPlusInfinity()) || 
                (l1.isMinusInfinity() && u2.isMinusInfinity()) ||
                (u1.isPlusInfinity() && l2.isPlusInfinity()) || 
                (u1.isMinusInfinity() && l2.isMinusInfinity())
            ) {
                return top();
            }

            MathNumber lSub = l1.subtract(u2);
            MathNumber uSub = u1.subtract(l2);

            return new IntervalRealLattice(lSub, uSub);
        }

        if (operator instanceof MultiplicationOperator) {
            // Pre-calculate boundaries while masking out 0 * infinity anomalies
            MathNumber p1 = (l1.isInfinite() && l2.isZero()) || (l1.isZero() && l2.isInfinite()) ? MathNumber.ZERO : l1.multiply(l2);
            MathNumber p2 = (l1.isInfinite() && u2.isZero()) || (l1.isZero() && u2.isInfinite()) ? MathNumber.ZERO : l1.multiply(u2);
            MathNumber p3 = (u1.isInfinite() && l2.isZero()) || (u1.isZero() && l2.isInfinite()) ? MathNumber.ZERO : u1.multiply(l2);
            MathNumber p4 = (u1.isInfinite() && u2.isZero()) || (u1.isZero() && u2.isInfinite()) ? MathNumber.ZERO : u1.multiply(u2);

            return new IntervalRealLattice(
                min(p1, p2, p3, p4), max(p1, p2, p3, p4)
            );
        }

        if (operator instanceof DivisionOperator) {
            // 1. Absolute Division by Zero -> Completely invalid state
            if (l2.isZero() && u2.isZero()) {
                return bottom();
            }

            // 2. Indeterminate Infinite Division (inf / inf) -> Drops to TOP
            if ((l1.isInfinite() || u1.isInfinite()) && (l2.isInfinite() || u2.isInfinite())) {
                return top();
            }

            // 3. Left-Open Zero Division [0, +u2]
            if (l2.isZero()) {
                if (u2.isInfinite()) return top();
                if (l1.geq(MathNumber.ZERO)) {
                    return new IntervalRealLattice(u1.divide(u2), MathNumber.PLUS_INFINITY);
                } else if (u1.isNegative()) {
                    return new IntervalRealLattice(MathNumber.MINUS_INFINITY, l1.divide(u2));
                }
                return top();
            }

            // 4. Right-Open Zero Division [-l2, 0]
            if (u2.isZero()) {
                if (l2.isInfinite()) {
                    return top();
                }

                if (l1.geq(MathNumber.ZERO)) {
                    return new IntervalRealLattice(MathNumber.MINUS_INFINITY, u1.divide(l2));
                } else if (u1.isNegative()) {
                    return new IntervalRealLattice(l1.divide(l2), MathNumber.PLUS_INFINITY);
                }

                return top();
            }

            // 5. Divisor straddles across zero completely (examp;e: [-1.5, +2.0])
            if (l2.isNegative() && u2.isPositive()) {
                return top();
            }

            // Fallback: Standard division loop for clean numbers
            MathNumber d1 = l1.divide(l2);
            MathNumber d2 = l1.divide(u2);
            MathNumber d3 = u1.divide(l2);
            MathNumber d4 = u1.divide(u2);

            return new IntervalRealLattice(min(d1, d2, d3, d4), max(d1, d2, d3, d4));
        }

        return top();
    }

}
