package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
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
        int value = (int) constant.getValue();
        if (value < 0)
            return ExtendedSignLattice.LT_ZERO;
        if (value > 0)
            return ExtendedSignLattice.GT_ZERO;
        return ExtendedSignLattice.EQ_ZERO;
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
            else if(arg == ExtendedSignLattice.EQ_ZERO)
                return ExtendedSignLattice.EQ_ZERO;
            else if(arg == ExtendedSignLattice.NON_ZERO)
                return ExtendedSignLattice.NON_ZERO;
            else if(arg == ExtendedSignLattice.TOP)
                return ExtendedSignLattice.TOP;
            else if(arg == ExtendedSignLattice.BOTTOM)
                return ExtendedSignLattice.BOTTOM;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right,
                                                    ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if(left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM)
            return ExtendedSignLattice.BOTTOM;
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

            else if((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                    || (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                    || (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO)
                    || (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO)
                    || (left == ExtendedSignLattice.NON_ZERO || right == ExtendedSignLattice.NON_ZERO))
                return ExtendedSignLattice.TOP;

            else if((left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO)
                    || (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LT_ZERO)
                    || (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LEQ_ZERO))
                return ExtendedSignLattice.LEQ_ZERO;

            else if((left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO)
                    || (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GT_ZERO)
                    || (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GEQ_ZERO))
                return ExtendedSignLattice.GEQ_ZERO;
            return ExtendedSignLattice.TOP;
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
}