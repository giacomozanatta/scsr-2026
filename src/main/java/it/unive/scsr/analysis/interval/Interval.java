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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
    public IntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(constant.getValue() instanceof Integer) {
            //I need to check the integer value to assign the right approx value
            Integer n = (Integer) constant.getValue();
            return new IntervalLattice(n,n);
        }
        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice evalUnaryExpression(UnaryExpression expression, IntervalLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(arg.i == null)
            return IntervalLattice.BOTTOM;
        if(expression.getOperator() == NumericNegation.INSTANCE) {
            MathNumber u = arg.i.getHigh();
            MathNumber l = arg.i.getLow();

            return new IntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
        }

        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left, IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(left.i == null || right.i == null)
            return IntervalLattice.BOTTOM;

        if(expression.getOperator() instanceof AdditionOperator){
            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            MathNumber ladd = l1.add(l2);
            MathNumber uadd = u1.add(u2);

            if(ladd.isNaN() || uadd.isNaN())
                return IntervalLattice.TOP;

            return new IntervalLattice(ladd, uadd);

        } else if(expression.getOperator() instanceof MultiplicationOperator){
            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            if(left.equals(IntervalLattice.ZERO) || right.equals(IntervalLattice.ZERO))
                return IntervalLattice.ZERO;

            // Calcolo tutti i possibili prodotti
            MathNumber p1 = l1.multiply(l2);
            MathNumber p2 = l1.multiply(u2);
            MathNumber p3 = u1.multiply(l2);
            MathNumber p4 = u1.multiply(u2);

            return getIntervalLattice(p1, p2, p3, p4);

        } else if(expression.getOperator() instanceof SubtractionOperator){
            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            MathNumber lsub = l1.subtract(u2);
            MathNumber usub = u1.subtract(l2);

            if(lsub.isNaN() || usub.isNaN())
                return IntervalLattice.TOP;

            return new IntervalLattice(lsub, usub);

        } else if(expression.getOperator() instanceof DivisionOperator){
            if (left.isBottom() || right.isBottom()) {
                return IntervalLattice.BOTTOM;
            }

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            IntervalLattice res;

            if (l2.isZero() && u2.isZero()) { // [l1,u1] / [0,0]
                System.out.println("Bottom");
                return IntervalLattice.BOTTOM;
            }

            if (l2.isZero()) { // [l1,u1] / [0,?]
                if(u2.isInfinite())
                    res = IntervalLattice.TOP;
                else if (l1.compareTo(MathNumber.ZERO) >= 0)
                    res = new IntervalLattice(u1.divide(u2), MathNumber.PLUS_INFINITY); // [+,+] / [0,+] = [u1/u2, +inf]
                else if (u1.isNegative())
                    res = new IntervalLattice(MathNumber.MINUS_INFINITY, l1.divide(u2)); // [-,-] / [0,+] = [-inf, l1/u2]
                else
                    res = IntervalLattice.TOP; // [-,+] / [0,+] = [-inf, +inf]
                return res;
            } else if (u2.isZero()) { // [l1,u1] / [?,0]
                if (l2.isInfinite())
                    res = IntervalLattice.TOP;
                else if (l1.compareTo(MathNumber.ZERO) >= 0)
                    res = new IntervalLattice(MathNumber.MINUS_INFINITY, u1.divide(l2)); // [+,+] / [-,0] = [-inf, u1/l2]
                else if (u1.isNegative())
                    res = new IntervalLattice(l1.divide(l2), MathNumber.PLUS_INFINITY); // [-,-] / [-,0] = [l1/l2, +inf]
                else
                    res = IntervalLattice.TOP; // [-,+] / [-,0]
                return res;
            }

            MathNumber minV = MathNumber.PLUS_INFINITY;
            MathNumber maxV = MathNumber.MINUS_INFINITY;

            List<MathNumber> vals = new ArrayList<>(List.of(
                    l1.divide(l2),
                    l1.divide(u2),
                    u1.divide(l2),
                    u1.divide(u2)
            ));

            for (MathNumber val : vals) {
                if (!val.isNaN()) {
                    minV = minV.min(val);
                    maxV = maxV.max(val);
                }
            }

            res = new IntervalLattice(minV, maxV);
            return res;
        }

        return IntervalLattice.TOP;
    }

    @Nonnull
    private IntervalLattice getIntervalLattice(MathNumber q1, MathNumber q2, MathNumber q3, MathNumber q4) {
        MathNumber min1 = q1.leq(q2) ? q1 : q2;
        MathNumber min2 = q3.leq(q4) ? q3 : q4;
        MathNumber min = min1.leq(min2) ? min1 : min2;

        MathNumber max1 = q1.geq(q2) ? q1 : q2;
        MathNumber max2 = q3.geq(q4) ? q3 : q4;
        MathNumber max = max1.geq(max2) ? max1 : max2;

        return new IntervalLattice(min,max);
    }
}