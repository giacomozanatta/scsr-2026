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
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class Parity implements
        BaseNonRelationalValueDomain<ParityLattice> {

    @Override
    public ParityLattice top() {
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice bottom() {
        return ParityLattice.BOTTOM;
    }

    //TODO add missing components
    @Override
    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (constant.getValue() instanceof Integer value) {
            return value % 2 == 0 ? ParityLattice.EVEN : ParityLattice.ODD;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
                                             SemanticOracle oracle) throws SemanticException {


        // One operand -> -
        // I should always know if - <some_value> is even or odd
        if (expression.getOperator() instanceof NumericNegation)
            return arg;
        // anything else
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
                                              ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if (left.isTop() || right.isTop())
            return ParityLattice.TOP;
        if (left.isBottom() || right.isBottom())
            return ParityLattice.BOTTOM;

        // Two operand -> +-*/
        if (expression.getOperator() instanceof AdditionOperator) {
            if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            if (left == ParityLattice.EVEN && right == ParityLattice.ODD)
                return ParityLattice.ODD;
            if (left == ParityLattice.ODD && right == ParityLattice.EVEN)
                return ParityLattice.ODD;
            if (left == ParityLattice.ODD && right == ParityLattice.ODD)
                return ParityLattice.EVEN;
        }
        if (expression.getOperator() instanceof SubtractionOperator) {
            if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            if (left == ParityLattice.EVEN && right == ParityLattice.ODD)
                return ParityLattice.ODD;
            if (left == ParityLattice.ODD && right == ParityLattice.EVEN)
                return ParityLattice.ODD;
            if (left == ParityLattice.ODD && right == ParityLattice.ODD)
                return ParityLattice.EVEN;
        }
        if (expression.getOperator() instanceof MultiplicationOperator) {
            if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            if (left == ParityLattice.EVEN && right == ParityLattice.ODD)
                return ParityLattice.EVEN;
            if (left == ParityLattice.ODD && right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            if (left == ParityLattice.ODD && right == ParityLattice.ODD)
                return ParityLattice.ODD;
        }
        if (expression.getOperator() instanceof DivisionOperator) {
            return ParityLattice.TOP; // we can never be sure (IntelliJ screams about deleting it, I don't want)
        }

        return ParityLattice.TOP;
    }

}