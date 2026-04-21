package it.unive.scsr.analysis.interval;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;

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
        if (constant.getValue() instanceof Integer) {
            Integer n = (Integer) constant.getValue();
            return new IntervalLattice(n, n);
        }
        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left, IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left.isBottom() || right.isBottom()) return IntervalLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator) {
            Integer l = (left.getLow() == null || right.getLow() == null) ? null : left.getLow() + right.getLow();
            Integer h = (left.getHigh() == null || right.getHigh() == null) ? null : left.getHigh() + right.getHigh();
            return new IntervalLattice(l, h);
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            // Исправлено: формула [l1 - h2, h1 - l2]
            Integer l = (left.getLow() == null || right.getHigh() == null) ? null : left.getLow() - right.getHigh();
            Integer h = (left.getHigh() == null || right.getLow() == null) ? null : left.getHigh() - right.getLow();
            return new IntervalLattice(l, h);
        }

        return IntervalLattice.TOP;
    }
}