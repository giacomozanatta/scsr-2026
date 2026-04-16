package it.unive.scsr.analysis.interval;

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

public class Interval implements BaseNonRelationalValueDomain<IntervalLattice> {

    @Override
    public IntervalLattice top() {
        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice bottom() {
        return IntervalLattice.BOTTOM;
    }

    @Override
    public IntervalLattice
    evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        if (constant.getValue() instanceof Integer) {
            // I need to check the integer value to
            // assign the right approx value
            Integer n = (Integer) constant.getValue();

            return new IntervalLattice(n, n);
        }

        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice evalUnaryExpression(UnaryExpression expression, IntervalLattice arg, ProgramPoint pp,
                                               SemanticOracle oracle) throws SemanticException {

        if (arg.i == null)
            return IntervalLattice.BOTTOM;

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            MathNumber u = arg.i.getHigh();
            MathNumber l = arg.i.getLow();

            return new IntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
        }

        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left,
                                                IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left.i == null || right == null)
            return IntervalLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator) {
            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            return new IntervalLattice(l1.add(l2), u1.add(u2));
        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            // the first value x is inside a range [a,b]
            // the second value y is inside a range [c,d]
            // the actual values are unknown so
            // i have to return a range of possible values for x*y
            // unique map of abcd pair-multiplied: a*b a*c a*d b*c b*d c*d
            // but idc about same-interval mul so what remains is a*c a*d b*c b*d
            // a = l1, b = u1, c = l2, d = u2

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            // ac min ad min bc
            // same idea for max
            MathNumber ac = l1.multiply(l2);
            MathNumber ad = l1.multiply(u2);
            MathNumber bc = u1.multiply(l2);
            MathNumber bd = u1.multiply(u2);
            MathNumber min = ac.min(ad).min(bc).min(bd);
            MathNumber max = ac.max(ad).max(bc).max(bd);

            return new IntervalLattice(min, max);
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            // the first value x is inside a range [a,b]
            // the second value y is inside a range [c,d]
            // the actual values are unknown so
            // i have to return a range of possible values for x-y
            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));
        } else if (expression.getOperator() instanceof DivisionOperator) {
            // the first value x is inside a range [a,b]
            // the second value y is inside a range [c,d]
            // the actual values are unknown so
            // i have to return a range of possible values for x/y
            // unique map of abcd pair-divided: a/b a/c a/d b/c b/d c/d
            // but idc about same-interval div so what remains is a/c a/d b/c b/d
            // i need to check for div by 0, just have to do it for the second interval [c,d]
            // a = l1, b = u1, c = l2, d = u2

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            // ex [-2, 2]
            if (l2.leq(MathNumber.ZERO) && u2.geq(MathNumber.ZERO)) {
                return IntervalLattice.BOTTOM;
            }

            // ac min ad min bc
            // same idea for max
            MathNumber ac = l1.divide(l2);
            MathNumber ad = l1.divide(u2);
            MathNumber bc = u1.divide(l2);
            MathNumber bd = u1.divide(u2);
            MathNumber min = ac.min(ad).min(bc).min(bd);
            MathNumber max = ac.max(ad).max(bc).max(bd);

            return new IntervalLattice(min, max);
        }

        return IntervalLattice.TOP;
    }
}
