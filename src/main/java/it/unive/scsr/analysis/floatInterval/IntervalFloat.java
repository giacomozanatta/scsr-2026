package it.unive.scsr.analysis.floatInterval;

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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class IntervalFloat implements BaseNonRelationalValueDomain<IntervalFloatLattice> {

    @Override
    public IntervalFloatLattice top() { return IntervalFloatLattice.TOP; }

    @Override
    public IntervalFloatLattice bottom() { return IntervalFloatLattice.BOTTOM; }

    // ------------------------------------------------------------------
    // evalConstant  ← now handles Integer, Long, Float, Double
    // ------------------------------------------------------------------

    @Override
    public IntervalFloatLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        Object v = constant.getValue();

        if (v instanceof Integer)  { double d = (Integer) v;  return new IntervalFloatLattice(d, d); }
        if (v instanceof Long)     { double d = (Long)    v;  return new IntervalFloatLattice(d, d); }
        if (v instanceof Float)    { double d = (Float)   v;  return new IntervalFloatLattice(d, d); }
        if (v instanceof Double)   { double d = (Double)  v;  return new IntervalFloatLattice(d, d); }

        return IntervalFloatLattice.TOP;
    }

    // ------------------------------------------------------------------
    // evalUnaryExpression
    // ------------------------------------------------------------------

    @Override
    public IntervalFloatLattice evalUnaryExpression(UnaryExpression expression, IntervalFloatLattice arg,
                                               ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if (arg.isBottom()) return IntervalFloatLattice.BOTTOM;

        if (expression.getOperator() == NumericNegation.INSTANCE)
            // Negate: [-high, -low]
            return new IntervalFloatLattice(-arg.high, -arg.low);

        return IntervalFloatLattice.TOP;
    }

    // ------------------------------------------------------------------
    // evalBinaryExpression
    // ------------------------------------------------------------------

    @Override
    public IntervalFloatLattice evalBinaryExpression(BinaryExpression expression, IntervalFloatLattice left,
                                                IntervalFloatLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if (left.isBottom() || right.isBottom())
            return IntervalFloatLattice.BOTTOM;

        double l1 = left.low,  u1 = left.high;
        double l2 = right.low, u2 = right.high;

        if (expression.getOperator() instanceof AdditionOperator)
            return new IntervalFloatLattice(l1 + l2, u1 + u2);

        if (expression.getOperator() instanceof SubtractionOperator)
            // [a,b] - [c,d] = [a-d, b-c]
            return new IntervalFloatLattice(l1 - u2, u1 - l2);

        if (expression.getOperator() instanceof MultiplicationOperator)
            return mul(left, right);

        if (expression.getOperator() instanceof DivisionOperator)
            return div(left, right);

        return IntervalFloatLattice.TOP;
    }

    // ------------------------------------------------------------------
    // Multiplication  (fixed: was returning bounds in wrong order)
    // ------------------------------------------------------------------

    private IntervalFloatLattice mul(IntervalFloatLattice left, IntervalFloatLattice right) {
        if (left.equals(IntervalFloatLattice.ZERO) || right.equals(IntervalFloatLattice.ZERO))
            return IntervalFloatLattice.ZERO;

        double l1 = left.low,  u1 = left.high;
        double l2 = right.low, u2 = right.high;

        double ll = l1 * l2, lh = l1 * u2;
        double hl = u1 * l2, hh = u1 * u2;

        return new IntervalFloatLattice(
                Math.min(Math.min(ll, lh), Math.min(hl, hh)),
                Math.max(Math.max(ll, lh), Math.max(hl, hh))
        );
    }

    // ------------------------------------------------------------------
    // Division
    //
    // For floats we do NOT round — 2.5 stays 2.5, which is correct.
    // We still handle the case where 0 ∈ divisor interval carefully.
    //
    // Dividing by [c,d]:
    //   0 ∉ [c,d]          → multiply by [1/d, 1/c]
    //   [c,d] = [0,0]      → undefined (return TOP as safe over-approx)
    //   0 is upper bound   → divisor in (-∞, 0]: multiply by (-∞, 1/c]
    //   0 is lower bound   → divisor in [0, +∞): multiply by [1/d, +∞)
    //   0 is strictly inside → split, take hull of both sides
    // ------------------------------------------------------------------

    private IntervalFloatLattice div(IntervalFloatLattice left, IntervalFloatLattice right) {
        if (left.equals(IntervalFloatLattice.ZERO))   return IntervalFloatLattice.ZERO;
        if (right.equals(IntervalFloatLattice.ZERO))  return IntervalFloatLattice.TOP;  // div by zero

        double l2 = right.low, u2 = right.high;

        // 0 not in [l2, u2]
        if (l2 > 0 || u2 < 0)
            return mul(left, new IntervalFloatLattice(1.0 / u2, 1.0 / l2));

        // 0 is the upper bound  → divisor ≤ 0
        if (u2 == 0.0)
            return mul(left, new IntervalFloatLattice(Double.NEGATIVE_INFINITY, 1.0 / l2));

        // 0 is the lower bound  → divisor ≥ 0
        if (l2 == 0.0)
            return mul(left, new IntervalFloatLattice(1.0 / u2, Double.POSITIVE_INFINITY));

        // 0 is strictly inside [l2, u2]: hull of the two sub-cases
        IntervalFloatLattice lower  = mul(left, new IntervalFloatLattice(Double.NEGATIVE_INFINITY, 1.0 / l2));
        IntervalFloatLattice higher = mul(left, new IntervalFloatLattice(1.0 / u2, Double.POSITIVE_INFINITY));

        if (includes(lower, higher)) return lower;
        if (includes(higher, lower)) return higher;

        return new IntervalFloatLattice(
                Math.min(lower.low,  higher.low),
                Math.max(lower.high, higher.high)
        );
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    public boolean includes(IntervalFloatLattice a, IntervalFloatLattice b) {
        if (a.isBottom() || b.isBottom()) return false;
        return a.low <= b.low && b.high <= a.high;
    }
}