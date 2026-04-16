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

    @Override
    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        if(constant.getValue() instanceof Integer) {
            Integer n = (Integer) constant.getValue();
            if(n%2==0)
                return ParityLattice.EVEN;
            else
                return ParityLattice.ODD;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
                                                                           SemanticOracle oracle) throws SemanticException {
        if(expression.getOperator() == NumericNegation.INSTANCE) {
            if(arg == ParityLattice.ODD)
                return ParityLattice.ODD;
            else if(arg == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            else if(arg == ParityLattice.TOP)
                return ParityLattice.TOP;
            else if(arg == ParityLattice.BOTTOM)
                return ParityLattice.BOTTOM;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
                                                                            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(expression.getOperator() instanceof AdditionOperator
                || expression.getOperator() instanceof SubtractionOperator) {
            if(left == ParityLattice.ODD && right == ParityLattice.ODD
                    || left == ParityLattice.EVEN && right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            if(left == ParityLattice.ODD && right == ParityLattice.EVEN
                    || left == ParityLattice.EVEN && right == ParityLattice.ODD)
                return ParityLattice.ODD;
            if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
                return ParityLattice.BOTTOM;
            if(left == ParityLattice.TOP || right == ParityLattice.TOP)
                return ParityLattice.TOP;

        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            if(left == ParityLattice.ODD && right == ParityLattice.ODD)
                return ParityLattice.ODD;
            if(left == ParityLattice.ODD && right == ParityLattice.EVEN
                    || left == ParityLattice.EVEN && right == ParityLattice.ODD
                    || left == ParityLattice.EVEN && right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
                return ParityLattice.BOTTOM;
            if(left == ParityLattice.TOP || right == ParityLattice.TOP)
                return ParityLattice.TOP;

        } else if (expression.getOperator() instanceof DivisionOperator
                || expression.getOperator() instanceof ModuloOperator
                || expression.getOperator() instanceof RemainderOperator) {
            if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
                return ParityLattice.BOTTOM;
            return ParityLattice.TOP;

        }
        return ParityLattice.TOP;
    }
}