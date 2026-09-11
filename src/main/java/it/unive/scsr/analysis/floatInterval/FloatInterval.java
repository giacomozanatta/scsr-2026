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
import it.unive.lisa.util.numeric.MathNumber;

public class FloatInterval implements BaseNonRelationalValueDomain<FloatIntervalLattice> {

    @Override
    public FloatIntervalLattice top(){return FloatIntervalLattice.TOP;}

    @Override
    public FloatIntervalLattice bottom(){return FloatIntervalLattice.BOTTOM;}

    @Override
    public FloatIntervalLattice
    evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle){
        if (constant.getValue() instanceof Number n) {
            MathNumber num = new MathNumber(n.doubleValue());
            return new FloatIntervalLattice(num, num);
        }
        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice evalUnaryExpression(UnaryExpression expression, FloatIntervalLattice arg, ProgramPoint pp,
                                               SemanticOracle oracle) throws SemanticException {
        if(arg.getHigh() == null && arg.getLow()==null)
            return FloatIntervalLattice.BOTTOM;

        if(expression.getOperator() == NumericNegation.INSTANCE) {
            MathNumber u = arg.getHigh();
            MathNumber l = arg.getLow();

            return new FloatIntervalLattice(u.multiply(MathNumber.MINUS_ONE),l.multiply(MathNumber.MINUS_ONE));
        }

        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice evalBinaryExpression(BinaryExpression expression, FloatIntervalLattice left,
                                                     FloatIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        //redundant as it's guaranteed both are not bottom
        if (left.isBottom() || right.isBottom()) {
            return FloatIntervalLattice.BOTTOM;
        }

        MathNumber l1 = left.getLow();
        MathNumber u1 = left.getHigh();
        MathNumber l2 = right.getLow();
        MathNumber u2 = right.getHigh();

        if (expression.getOperator() instanceof AdditionOperator) {
            return new FloatIntervalLattice(l1.add(l2), u1.add(u2));

        } else if (expression.getOperator() instanceof SubtractionOperator) {
            return new FloatIntervalLattice(l1.subtract(u2), u1.subtract(l2));

        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            MathNumber x1 = l1.multiply(l2);
            MathNumber x2 = l1.multiply(u2);
            MathNumber x3 = u1.multiply(l2);
            MathNumber x4 = u1.multiply(u2);

            MathNumber min = x1.min(x2).min(x3).min(x4);
            MathNumber max = x1.max(x2).max(x3).max(x4);

            return new FloatIntervalLattice(min, max);

        } else if (expression.getOperator() instanceof DivisionOperator) {
           if (l2.leq(MathNumber.ZERO) && MathNumber.ZERO.leq(u2)) {
                return FloatIntervalLattice.TOP;
            }

            MathNumber x1 = l1.divide(l2);
            MathNumber x2 = l1.divide(u2);
            MathNumber x3 = u1.divide(l2);
            MathNumber x4 = u1.divide(u2);

            MathNumber min = x1.min(x2).min(x3).min(x4);
            MathNumber max = x1.max(x2).max(x3).max(x4);

            return new FloatIntervalLattice(min, max);
        }

        return FloatIntervalLattice.TOP;
    }

}
