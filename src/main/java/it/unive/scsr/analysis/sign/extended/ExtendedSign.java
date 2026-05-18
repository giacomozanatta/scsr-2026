package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
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
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        if (constant.getValue() instanceof Number) {
            Double n = ((Number) constant.getValue()).doubleValue();

            if (n == 0.0) return ExtendedSignLattice.EQ_ZERO;
            if (n < 0) return ExtendedSignLattice.LT_ZERO;
            return ExtendedSignLattice.GT_ZERO;
        }

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg,
                                                   ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE)
            return arg.negate();

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
                                                    ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() instanceof AdditionOperator)
            return left.add(right);
        else if (expression.getOperator() instanceof MultiplicationOperator)
            return left.mul(right);
        else if (expression.getOperator() instanceof SubtractionOperator)
            return left.add(right.negate());
        else if (expression.getOperator() instanceof DivisionOperator)
            return left.div(right);
        else if (expression.getOperator() instanceof ModuloOperator)
            return left.mod(right);
        else if (expression.getOperator() instanceof RemainderOperator)
            return left.rem(right);

        return ExtendedSignLattice.TOP;
    }

}