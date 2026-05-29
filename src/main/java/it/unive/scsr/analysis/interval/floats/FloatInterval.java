package it.unive.scsr.analysis.interval.floats;

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

import java.math.BigDecimal;

public class FloatInterval implements BaseNonRelationalValueDomain<FloatIntervalLattice> {

    @Override
    public FloatIntervalLattice top() {
        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice bottom() {
        return FloatIntervalLattice.BOTTOM;
    }

    @Override
    public FloatIntervalLattice
    evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        if (constant.getValue() instanceof Number) {
            MathNumber n = new MathNumber(new BigDecimal(constant.getValue().toString()));

            return new FloatIntervalLattice(n, n);
        }

        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice evalUnaryExpression(UnaryExpression expression, FloatIntervalLattice arg, ProgramPoint pp,
                                                    SemanticOracle oracle) throws SemanticException {
        if (arg.i == null)
            return FloatIntervalLattice.BOTTOM;

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            MathNumber u = arg.i.getHigh();
            MathNumber l = arg.i.getLow();

            return new FloatIntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
        }

        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice evalBinaryExpression(BinaryExpression expression, FloatIntervalLattice left,
                                                     FloatIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left.i == null || right.i == null)
            return FloatIntervalLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator) {

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            return new FloatIntervalLattice(l1.add(l2), u1.add(u2));

        } else if (expression.getOperator() instanceof MultiplicationOperator) {

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            return new FloatIntervalLattice(min(l1.multiply(l2), l1.multiply(u2), u1.multiply(l2), u1.multiply(u2)),
                    max(l1.multiply(l2), l1.multiply(u2), u1.multiply(l2), u1.multiply(u2)));

        } else if (expression.getOperator() instanceof SubtractionOperator) {

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            return new FloatIntervalLattice(l1.subtract(u2), u1.subtract(l2));

        } else if (expression.getOperator() instanceof DivisionOperator) {

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            if (l2.equals(MathNumber.ZERO) && u2.equals(MathNumber.ZERO)) return FloatIntervalLattice.BOTTOM;
            if (l2.leq(MathNumber.ZERO) && u2.geq(MathNumber.ZERO)) return FloatIntervalLattice.TOP;

            return new FloatIntervalLattice(min(l1.divide(l2), l1.divide(u2), u1.divide(l2), u1.divide(u2)),
                    max(l1.divide(l2), l1.divide(u2), u1.divide(l2), u1.divide(u2)));
        }

        return FloatIntervalLattice.TOP;
    }

    private MathNumber min(MathNumber a, MathNumber b, MathNumber c, MathNumber d) {
        MathNumber min1 = a.leq(b) ? a : b;
        MathNumber min2 = c.leq(d) ? c : d;

        return min1.leq(min2) ? min1 : min2;
    }

    private MathNumber max(MathNumber a, MathNumber b, MathNumber c, MathNumber d) {
        MathNumber max1 = a.geq(b) ? a : b;
        MathNumber max2 = c.geq(d) ? c : d;

        return max1.geq(max2) ? max1 : max2;
    }


}
