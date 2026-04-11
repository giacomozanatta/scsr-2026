package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;


/**
 * @author Mattia Acquilesi 896827
 * @author Alan Dal Col 895879
 */
public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

    @Override
    public ParityLattice top() {
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice bottom() {
        return ParityLattice.BOTTOM;
    }

    @Override
    public ParityLattice evalConstant(
            Constant constant,
            ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {

        if (constant.getValue() instanceof Integer n) {
            return (n % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD;
        }

        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalUnaryExpression(
            UnaryExpression expression,
            ParityLattice arg,
            ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            return arg;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalBinaryExpression(
            BinaryExpression expression,
            ParityLattice left,
            ParityLattice right,
            ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {

        if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
            return ParityLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator
                || expression.getOperator() instanceof SubtractionOperator) {
            return parityAdd(left, right);
        }

        if (expression.getOperator() instanceof MultiplicationOperator) {
            return parityMul(left, right);
        }

        if (expression.getOperator() instanceof DivisionOperator) {
            return ParityLattice.TOP;
        }

        if (expression.getOperator() instanceof ModuloOperator
                || expression.getOperator() instanceof RemainderOperator) {
            if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            if (left == ParityLattice.ODD && right == ParityLattice.EVEN)
                return ParityLattice.ODD;
            return ParityLattice.TOP;
        }

        return ParityLattice.TOP;
    }



    private ParityLattice parityAdd(ParityLattice left, ParityLattice right) {
        if (left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;
        if (left == right)
            return ParityLattice.EVEN;
        return ParityLattice.ODD;
    }



    private ParityLattice parityMul(ParityLattice left, ParityLattice right) {
        if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
            return ParityLattice.EVEN;
        if (left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;
        return ParityLattice.ODD;
    }
}