package it.unive.scsr.analysis.extendedinterval;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedInterval implements BaseNonRelationalValueDomain<ExtendedIntervalLattice> {

    @Override
    public ExtendedIntervalLattice top() {
        return ExtendedIntervalLattice.TOP;
    }

    @Override
    public ExtendedIntervalLattice bottom() {
        return ExtendedIntervalLattice.BOTTOM;
    }

    @Override
    public ExtendedIntervalLattice evalConstant(Constant constant,
                                             ProgramPoint pp,
                                             SemanticOracle oracle)
            throws SemanticException {

        Object value = constant.getValue();

        if (value instanceof Integer)
            return new ExtendedIntervalLattice(((Integer) value).doubleValue(),
                    ((Integer) value).doubleValue());

        if (value instanceof Double)
            return new ExtendedIntervalLattice((Double) value, (Double) value);

        if (value instanceof Float)
            return new ExtendedIntervalLattice(((Float) value).doubleValue(), ((Float) value).doubleValue());

        return ExtendedIntervalLattice.TOP;
    }

    @Override
    public ExtendedIntervalLattice evalUnaryExpression(UnaryExpression expression,
                                                    ExtendedIntervalLattice arg,
                                                    ProgramPoint pp,
                                                    SemanticOracle oracle)
            throws SemanticException {

        if (arg.isBottom())
            return ExtendedIntervalLattice.BOTTOM;

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            return new ExtendedIntervalLattice(
                    -arg.high,
                    -arg.low
            );
        }

        return ExtendedIntervalLattice.TOP;
    }

    @Override
    public ExtendedIntervalLattice evalBinaryExpression(BinaryExpression expression,
                                                     ExtendedIntervalLattice left,
                                                     ExtendedIntervalLattice right,
                                                     ProgramPoint pp,
                                                     SemanticOracle oracle)
            throws SemanticException {

        if (left.isBottom() || right.isBottom())
            return ExtendedIntervalLattice.BOTTOM;

        if (left.isTop() || right.isTop())
            return ExtendedIntervalLattice.TOP;

        double l1 = left.low;
        double u1 = left.high;
        double l2 = right.low;
        double u2 = right.high;

        if (expression.getOperator() instanceof AdditionOperator) {
            return new ExtendedIntervalLattice(l1 + l2, u1 + u2);
        }

        if (expression.getOperator() instanceof SubtractionOperator) {
            return new ExtendedIntervalLattice(l1 - u2, u1 - l2);
        }

        if (expression.getOperator() instanceof MultiplicationOperator) {
            double r1 = l1 * l2;
            double r2 = l1 * u2;
            double r3 = u1 * l2;
            double r4 = u1 * u2;

            double min = Math.min(Math.min(r1, r2), Math.min(r3, r4));
            double max = Math.max(Math.max(r1, r2), Math.max(r3, r4));

            return new ExtendedIntervalLattice(min, max);
        }

        if (expression.getOperator() instanceof DivisionOperator) {

            if (l2 <= 0 && u2 >= 0)
                return ExtendedIntervalLattice.TOP;

            double r1 = l1 / l2;
            double r2 = l1 / u2;
            double r3 = u1 / l2;
            double r4 = u1 / u2;

            double min = Math.min(Math.min(r1, r2), Math.min(r3, r4));
            double max = Math.max(Math.max(r1, r2), Math.max(r3, r4));

            return new ExtendedIntervalLattice(min, max);
        }

        return ExtendedIntervalLattice.TOP;
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression,
                                                    ExtendedIntervalLattice left,
                                                    ExtendedIntervalLattice right,
                                                    ProgramPoint pp,
                                                    SemanticOracle oracle) {

        if (left.isBottom() || right.isBottom())
            return Satisfiability.BOTTOM;

        if (expression.getOperator() == ComparisonEq.INSTANCE)
            return left.eq(right);

        else if (expression.getOperator() == ComparisonGe.INSTANCE)
            return left.eq(right).or(left.gt(right));

        else if (expression.getOperator() == ComparisonGt.INSTANCE)
            return left.gt(right);

        else if (expression.getOperator() == ComparisonLe.INSTANCE)
            return left.gt(right).negate();

        else if (expression.getOperator() == ComparisonLt.INSTANCE)
            return left.gt(right)
                    .negate()
                    .and(left.eq(right).negate());

        else if (expression.getOperator() == ComparisonNe.INSTANCE)
            return left.eq(right).negate();

        return Satisfiability.UNKNOWN;
    }

    @Override
    public ValueEnvironment<ExtendedIntervalLattice> assumeBinaryExpression(
            ValueEnvironment<ExtendedIntervalLattice> environment,
            BinaryExpression expression,
            ProgramPoint src,
            ProgramPoint dest,
            SemanticOracle oracle)
            throws SemanticException {

        Satisfiability sat =
                satisfies(environment, expression, src, oracle);

        if (sat == Satisfiability.NOT_SATISFIED)
            return environment.bottom();

        if (sat == Satisfiability.SATISFIED)
            return environment;

        if (!(expression.getLeft() instanceof Identifier))
            return environment;

        Identifier id = (Identifier) expression.getLeft();

        ExtendedIntervalLattice current =
                environment.getState(id);

        ExtendedIntervalLattice refinement = null;

        if (expression.getOperator() == ComparisonGt.INSTANCE)
            refinement = new ExtendedIntervalLattice(
                    0.0,
                    Double.POSITIVE_INFINITY);

        else if (expression.getOperator() == ComparisonGe.INSTANCE)
            refinement = new ExtendedIntervalLattice(
                    0.0,
                    Double.POSITIVE_INFINITY);

        else if (expression.getOperator() == ComparisonLt.INSTANCE)
            refinement = new ExtendedIntervalLattice(
                    Double.NEGATIVE_INFINITY,
                    0.0);

        else if (expression.getOperator() == ComparisonLe.INSTANCE)
            refinement = new ExtendedIntervalLattice(
                    Double.NEGATIVE_INFINITY,
                    0.0);

        else if (expression.getOperator() == ComparisonEq.INSTANCE)
            refinement = ExtendedIntervalLattice.ZERO;

        else if (expression.getOperator() == ComparisonNe.INSTANCE)
            return environment;

        if (refinement == null)
            return environment;

        ExtendedIntervalLattice updated =
                current.glb(refinement);

        if (updated.isBottom())
            return environment.bottom();

        return environment.putState(id, updated);
    }
}