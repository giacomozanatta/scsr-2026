package it.unive.scsr.analysis.interval.doubleinterval;

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
import it.unive.lisa.util.numeric.MathNumber;

public class DoubleInterval implements BaseNonRelationalValueDomain<DoubleIntervalLattice> {

    @Override
    public DoubleIntervalLattice top() {
        return DoubleIntervalLattice.TOP;
    }

    @Override
    public DoubleIntervalLattice bottom() {
        return DoubleIntervalLattice.BOTTOM;
    }

    @Override
    public DoubleIntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        if (constant.getValue() instanceof Double n) {
            return new DoubleIntervalLattice(n, n);
        }

        return DoubleIntervalLattice.TOP;
    }

    @Override
    public DoubleIntervalLattice evalUnaryExpression(UnaryExpression expression, DoubleIntervalLattice arg, ProgramPoint pp,
                                                     SemanticOracle oracle) throws SemanticException {

        if (arg.getLow() == null)
            return DoubleIntervalLattice.BOTTOM;

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            MathNumber u = arg.getHigh();
            MathNumber l = arg.getLow();

            return new DoubleIntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
        }

        return DoubleIntervalLattice.TOP;
    }

    @Override
    public DoubleIntervalLattice evalBinaryExpression(BinaryExpression expression, DoubleIntervalLattice left,
                                                      DoubleIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if ((left.getLow() == null || left.getHigh() == null) || (right.getLow() == null || right.getHigh() == null))
            return DoubleIntervalLattice.BOTTOM;

        MathNumber l1 = left.getLow();
        MathNumber l2 = right.getLow();

        MathNumber u1 = left.getHigh();
        MathNumber u2 = right.getHigh();

        if (expression.getOperator() instanceof AdditionOperator) {
            return new DoubleIntervalLattice(l1.add(l2), u1.add(u2));
        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            return mul(left, right);
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            return new DoubleIntervalLattice(l1.subtract(u2), u1.subtract(l2));
        } else if (expression.getOperator() instanceof DivisionOperator) {
            if (left.equals(DoubleIntervalLattice.ZERO))
                return DoubleIntervalLattice.ZERO;
            if (right.equals(DoubleIntervalLattice.ZERO))
                return DoubleIntervalLattice.TOP;

            if (!includes(right, DoubleIntervalLattice.ZERO))
                return round(mul(left, new DoubleIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.ONE.divide(l2))));
            else if (u2.isZero())
                return round(mul(left, new DoubleIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2))));
            else if (l2.isZero())
                return round(mul(left, new DoubleIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY)));
            else {
                DoubleIntervalLattice lower = mul(left, new DoubleIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2)));
                DoubleIntervalLattice higher = mul(left, new DoubleIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY));

                if (includes(lower, higher))
                    return lower;
                else if (includes(higher, lower))
                    return higher;
                else {
                    MathNumber l = lower.getLow().compareTo(higher.getLow()) > 0 ? higher.getLow() : lower.getLow();
                    MathNumber u = lower.getHigh().compareTo(higher.getHigh()) < 0 ? higher.getHigh() : lower.getHigh();
                    return round(new DoubleIntervalLattice(l, u));
                }
            }
        }

        return DoubleIntervalLattice.TOP;
    }

    private DoubleIntervalLattice round(DoubleIntervalLattice doubleIntervalLattice) {
        if (doubleIntervalLattice.isBottom() || doubleIntervalLattice.isTop())
            return doubleIntervalLattice;
        return new DoubleIntervalLattice(doubleIntervalLattice.getLow().roundDown(), doubleIntervalLattice.getLow().roundUp());
    }

    private DoubleIntervalLattice mul(DoubleIntervalLattice left, DoubleIntervalLattice right) {
        MathNumber l1 = left.getLow();
        MathNumber l2 = right.getLow();

        MathNumber u1 = left.getHigh();
        MathNumber u2 = right.getHigh();

        if (left.equals(DoubleIntervalLattice.ZERO) || right.equals(DoubleIntervalLattice.ZERO))
            return DoubleIntervalLattice.ZERO;
        else {
            if (l1.compareTo(MathNumber.ZERO) >= 0 && l2.compareTo(MathNumber.ZERO) >= 0)
                return new DoubleIntervalLattice(u1.multiply(u2), l1.multiply(l2));

            MathNumber ll = l1.multiply(l2);
            MathNumber lh = l1.multiply(u2);
            MathNumber hl = u1.multiply(l2);
            MathNumber hh = u1.multiply(u2);

            return new DoubleIntervalLattice(min(ll, lh, hl, hh), max(ll, lh, hl, hh));
        }
    }

    private static MathNumber min(MathNumber... nums) {
        if (nums.length == 0)
            throw new IllegalArgumentException("No numbers provided");

        MathNumber min = nums[0];
        for (int i = 1; i < nums.length; i++)
            min = min.min(nums[i]);

        return min;
    }

    private static MathNumber max(MathNumber... nums) {
        if (nums.length == 0)
            throw new IllegalArgumentException("No numbers provided");

        MathNumber max = nums[0];
        for (int i = 1; i < nums.length; i++)
            max = max.max(nums[i]);

        return max;
    }

    public boolean includes(DoubleIntervalLattice a,
                            DoubleIntervalLattice b) {
        if (a.isBottom() || b.isBottom())
            return false;
        return a.getLow().compareTo(b.getLow()) <= 0 && a.getHigh().compareTo(b.getHigh()) >= 0;
    }
}
