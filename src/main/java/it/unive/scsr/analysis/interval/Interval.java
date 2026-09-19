package it.unive.scsr.analysis.interval;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;

/**
 * An abstract domain for Interval Analysis.
 * It tracks the minimum and maximum possible numeric values for variables.
 */
public class Interval implements BaseNonRelationalValueDomain<IntervalLattice> {

    // Returns the TOP element (representing an unknown value: [-inf, +inf])
    @Override
    public IntervalLattice top() { return IntervalLattice.TOP; }

    // Returns the BOTTOM element (representing an unreachable state)
    @Override
    public IntervalLattice bottom() { return IntervalLattice.BOTTOM; }

    // Evaluates constant numeric values and converts them into an exact interval [n, n]
    @Override
    public IntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (constant.getValue() instanceof Double || constant.getValue() instanceof Float) {
            Double n = Double.valueOf(constant.getValue().toString());
            return new IntervalLattice(n, n);
        }
        // If it's not a numeric constant, return TOP (unknown)
        return IntervalLattice.TOP;
    }

    // Evaluates binary expressions (like addition and subtraction) using interval arithmetic
    @Override
    public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left, IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        // Any operation involving an unreachable state (BOTTOM) results in BOTTOM
        if (left.isBottom() || right.isBottom()) return IntervalLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator) {
            // Addition: [l1, h1] + [l2, h2] = [l1 + l2, h1 + h2]
            // If any bound is null (infinity), the result bound remains null
            Double l = (left.getLow() == null || right.getLow() == null) ? null : left.getLow() + right.getLow();
            Double h = (left.getHigh() == null || right.getHigh() == null) ? null : left.getHigh() + right.getHigh();
            return new IntervalLattice(l, h);
            
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            // Subtraction: [l1, h1] - [l2, h2] = [l1 - h2, h1 - l2]
            Double l = (left.getLow() == null || right.getHigh() == null) ? null : left.getLow() - right.getHigh();
            Double h = (left.getHigh() == null || right.getLow() == null) ? null : left.getHigh() - right.getLow();
            return new IntervalLattice(l, h);
        }
        
        // For unsupported operators, safely over-approximate by returning TOP
        return IntervalLattice.TOP;
    }
}