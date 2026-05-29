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

public class FloatInterval implements BaseNonRelationalValueDomain<FloatIntervalLattice> {

    @Override public FloatIntervalLattice top()    { return FloatIntervalLattice.TOP; }
    @Override public FloatIntervalLattice bottom() { return FloatIntervalLattice.BOTTOM; }

    @Override
    public FloatIntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        Object v = constant.getValue();
        if (v instanceof Integer) { double d = (Integer) v; return new FloatIntervalLattice(d, d); }
        if (v instanceof Float)   { double d = (Float)   v; return new FloatIntervalLattice(d, d); }
        if (v instanceof Double)  { double d = (Double)  v; return new FloatIntervalLattice(d, d); }
        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice evalUnaryExpression(UnaryExpression expression, FloatIntervalLattice arg,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (arg.isBottom()) return FloatIntervalLattice.BOTTOM;
        if (expression.getOperator() == NumericNegation.INSTANCE)
            return new FloatIntervalLattice(-arg.hi, -arg.lo);
        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice evalBinaryExpression(BinaryExpression expression, FloatIntervalLattice left,
            FloatIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left.isBottom() || right.isBottom()) return FloatIntervalLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator)
            return new FloatIntervalLattice(left.lo + right.lo, left.hi + right.hi);

        if (expression.getOperator() instanceof SubtractionOperator)
            return new FloatIntervalLattice(left.lo - right.hi, left.hi - right.lo);

        if (expression.getOperator() instanceof MultiplicationOperator) {
            double p1 = left.lo * right.lo, p2 = left.lo * right.hi;
            double p3 = left.hi * right.lo, p4 = left.hi * right.hi;
            return new FloatIntervalLattice(
                Math.min(Math.min(p1, p2), Math.min(p3, p4)),
                Math.max(Math.max(p1, p2), Math.max(p3, p4)));
        }

        if (expression.getOperator() instanceof DivisionOperator) {
            if (right.lo <= 0 && right.hi >= 0) return FloatIntervalLattice.TOP;
            double p1 = left.lo / right.lo, p2 = left.lo / right.hi;
            double p3 = left.hi / right.lo, p4 = left.hi / right.hi;
            return new FloatIntervalLattice(
                Math.min(Math.min(p1, p2), Math.min(p3, p4)),
                Math.max(Math.max(p1, p2), Math.max(p3, p4)));
        }

        return FloatIntervalLattice.TOP;
    }
}
