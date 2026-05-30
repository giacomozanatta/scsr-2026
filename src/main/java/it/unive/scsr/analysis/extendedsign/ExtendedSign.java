package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;


public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

    @Override
    public ExtendedSignLattice top() {
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return ExtendedSignLattice.BOTTOM;
    }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant,
                                            ProgramPoint pp,
                                            SemanticOracle oracle) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int value = (Integer) constant.getValue();
            if (value < 0)
                return ExtendedSignLattice.LT_ZERO;
            if (value > 0)
                return ExtendedSignLattice.GT_ZERO;
            return ExtendedSignLattice.EQ_ZERO;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression,
                                                   ExtendedSignLattice arg,
                                                   ProgramPoint pp,
                                                   SemanticOracle oracle) throws SemanticException {
        if(expression.getOperator() == NumericNegation.INSTANCE) {
            if(arg == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.LT_ZERO;

            else if(arg == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.GT_ZERO;
            else if(arg == ExtendedSignLattice.GEQ_ZERO)
                return ExtendedSignLattice.LEQ_ZERO;
            else if(arg == ExtendedSignLattice.LEQ_ZERO)
                return ExtendedSignLattice.GEQ_ZERO;

            // otherwise return itself
            return arg;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right,
                                                    ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        // In any case if bottom(...)bottom = bottom
        if(left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM)
            return ExtendedSignLattice.BOTTOM;
        // In any case if top(...)top = top
        else if(left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP)
            return ExtendedSignLattice.TOP;

        if(expression.getOperator() instanceof AdditionOperator) {
            if(left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.LT_ZERO;
            else if(left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.GT_ZERO;
            else if(left == ExtendedSignLattice.EQ_ZERO)
                return right;
            else if(right == ExtendedSignLattice.EQ_ZERO)
                return left;

            else if((left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO)
                    || (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LT_ZERO)
                    || (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LEQ_ZERO))
                return ExtendedSignLattice.LEQ_ZERO;

            else if((left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO)
                    || (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GT_ZERO)
                    || (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GEQ_ZERO))
                return ExtendedSignLattice.GEQ_ZERO;
        }

        else if (expression.getOperator() instanceof MultiplicationOperator) {
            if (left == ExtendedSignLattice.EQ_ZERO || right == ExtendedSignLattice.EQ_ZERO )
                return ExtendedSignLattice.EQ_ZERO;

            else if ((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                    ||(left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO))
                return ExtendedSignLattice.LT_ZERO;

            else if ((left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO)
                    ||(left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO))
                return ExtendedSignLattice.LEQ_ZERO;

            else if ((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                    ||(left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GT_ZERO))
                return ExtendedSignLattice.GT_ZERO;

            else if ((left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO)
                    ||(left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO))
                return ExtendedSignLattice.GEQ_ZERO;

            else if ((left == ExtendedSignLattice.NON_ZERO && right == ExtendedSignLattice.NON_ZERO))
                return ExtendedSignLattice.NON_ZERO;
        }

        else if (expression.getOperator() instanceof SubtractionOperator) {
            if(left == ExtendedSignLattice.EQ_ZERO && right == ExtendedSignLattice.EQ_ZERO)
                return ExtendedSignLattice.EQ_ZERO;
            if (right == ExtendedSignLattice.EQ_ZERO)
                return left;
            if (left == ExtendedSignLattice.EQ_ZERO)
                return evalUnaryExpression(
                        new UnaryExpression(null, null, NumericNegation.INSTANCE, null),
                        right,
                        pp,
                        oracle);
            if (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.GT_ZERO;
            if (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.LT_ZERO;
            return ExtendedSignLattice.TOP;
        }

        else if (expression.getOperator() instanceof DivisionOperator) {
            if (left == ExtendedSignLattice.EQ_ZERO)
                return ExtendedSignLattice.EQ_ZERO;
            else if(right == ExtendedSignLattice.EQ_ZERO)
                return ExtendedSignLattice.BOTTOM;

            else if ((left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                    ||(left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LT_ZERO))
                return ExtendedSignLattice.GT_ZERO;

            else if ((left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO)
                    ||(left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO))
                return ExtendedSignLattice.GEQ_ZERO;

            else if ((left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO)
                    ||(left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO))
                return ExtendedSignLattice.LEQ_ZERO;

            else if ((left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                    ||(left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO))
                return ExtendedSignLattice.LT_ZERO;

            else if ((left == ExtendedSignLattice.NON_ZERO && right == ExtendedSignLattice.NON_ZERO))
                return ExtendedSignLattice.NON_ZERO;
        }
        else if (expression.getOperator() instanceof ModuloOperator)
            return right;

        else if (expression.getOperator() instanceof RemainderOperator)
            return left;

        return ExtendedSignLattice.TOP;
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression,
                                                    ExtendedSignLattice left,
                                                    ExtendedSignLattice right,
                                                    ProgramPoint pp,
                                                    SemanticOracle oracle) {

        if (left.isTop() || right.isTop())
            return Satisfiability.UNKNOWN;


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
    public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(
            ValueEnvironment<ExtendedSignLattice> environment,
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

        ExtendedSignLattice current =
                environment.getState(id);

        ExtendedSignLattice refinement = null;

        if (expression.getOperator() == ComparisonGt.INSTANCE)
            refinement = ExtendedSignLattice.GT_ZERO;

        else if (expression.getOperator() == ComparisonGe.INSTANCE)
            refinement = ExtendedSignLattice.GEQ_ZERO;

        else if (expression.getOperator() == ComparisonLt.INSTANCE)
            refinement = ExtendedSignLattice.LT_ZERO;

        else if (expression.getOperator() == ComparisonLe.INSTANCE)
            refinement = ExtendedSignLattice.LEQ_ZERO;

        else if (expression.getOperator() == ComparisonEq.INSTANCE)
            refinement = ExtendedSignLattice.EQ_ZERO;

        else if (expression.getOperator() == ComparisonNe.INSTANCE)
            refinement = ExtendedSignLattice.NON_ZERO;

        if (refinement == null)
            return environment;

        ExtendedSignLattice updated =
                current.glb(refinement);

        if (updated.isBottom())
            return environment.bottom();

        return environment.putState(id, updated);
    }

}