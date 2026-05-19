package it.unive.scsr.analysis.interval.extendedInterval;

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
import it.unive.scsr.analysis.interval.IntervalLattice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExtendedInterval  implements BaseNonRelationalValueDomain<ExtendedIntervalLattice> {
    @Override
    public ExtendedIntervalLattice top() {
        return ExtendedIntervalLattice.TOP;
    }

    @Override
    public ExtendedIntervalLattice bottom() {
        return ExtendedIntervalLattice.BOTTOM;
    }

    @Override
    public ExtendedIntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(constant.getValue() instanceof Number) {
            String valStr = constant.getValue().toString();
            MathNumber val = new MathNumber(new BigDecimal(valStr));
            return new ExtendedIntervalLattice(val, val);
        }
        return ExtendedIntervalLattice.TOP;
    }

    @Override
    public ExtendedIntervalLattice evalUnaryExpression(UnaryExpression expression, ExtendedIntervalLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(arg.isBottom()) return ExtendedIntervalLattice.BOTTOM;
        if(expression.getOperator() == NumericNegation.INSTANCE){
            MathNumber u = arg.getLow();
            MathNumber h = arg.getHigh();
            return new ExtendedIntervalLattice(h.multiply(MathNumber.MINUS_ONE), u.multiply(MathNumber.MINUS_ONE));
        }
        return ExtendedIntervalLattice.TOP;
    }

    @Override
    public ExtendedIntervalLattice evalBinaryExpression(BinaryExpression expression, ExtendedIntervalLattice left, ExtendedIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(left.isBottom() || right.isBottom()) return ExtendedIntervalLattice.BOTTOM;

        MathNumber l1 = left.getLow();
        MathNumber u1 = left.getHigh();
        MathNumber l2 = right.getLow();
        MathNumber u2 = right.getHigh();

        if(expression.getOperator() instanceof AdditionOperator){
            MathNumber ladd = l1.add(l2);
            MathNumber uadd = u1.add(u2);

            if(ladd.isNaN() || uadd.isNaN()) return ExtendedIntervalLattice.TOP;
            return new ExtendedIntervalLattice(ladd, uadd);

        } else if(expression.getOperator() instanceof SubtractionOperator){
            MathNumber lsub = l1.subtract(u2);
            MathNumber usub = u1.subtract(l2);

            if(lsub.isNaN() || usub.isNaN()) return ExtendedIntervalLattice.TOP;
            return new ExtendedIntervalLattice(lsub, usub);
        } else if(expression.getOperator() instanceof MultiplicationOperator){
            if(left.equals(ExtendedIntervalLattice.ZERO) || right.equals(ExtendedIntervalLattice.ZERO)) return ExtendedIntervalLattice.ZERO;

            MathNumber p1 = l1.multiply(l2);
            MathNumber p2 = l1.multiply(u2);
            MathNumber p3 = u1.multiply(l2);
            MathNumber p4 = u1.multiply(u2);
            MathNumber max = p1.max(p2).max(p3).max(p4);

            MathNumber min = p1.min(p2).min(p3).min(p4);
            return new ExtendedIntervalLattice(min, max);
        } else if(expression.getOperator() instanceof DivisionOperator){
            if(l2.compareTo(MathNumber.ZERO) < 0 && u2.compareTo(MathNumber.ZERO) > 0) return ExtendedIntervalLattice.TOP;

            MathNumber minV = MathNumber.PLUS_INFINITY;
            MathNumber maxV = MathNumber.MINUS_INFINITY;

            List<MathNumber> vals = new ArrayList<>();
            if (!l2.isZero()) { vals.add(l1.divide(l2)); vals.add(u1.divide(l2)); }
            if (!u2.isZero()) { vals.add(l1.divide(u2)); vals.add(u1.divide(u2)); }

            for (MathNumber val : vals) {
                if (!val.isNaN()) {
                    minV = minV.min(val);
                    maxV = maxV.max(val);
                }
            }
            return new ExtendedIntervalLattice(minV, maxV);
        }
        return ExtendedIntervalLattice.TOP;
    }
}
