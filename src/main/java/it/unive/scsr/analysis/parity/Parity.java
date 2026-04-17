package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

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
    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            Integer value = (Integer) constant.getValue();
            return (value % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD;
        }
        return top();
    }

    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE) {
            return arg;
        }
        return top();
    }

    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if (left.isBottom() || right.isBottom()) {
            return bottom();
        }
        if (left.isTop() || right.isTop()) {
            return top();
        }

        BinaryOperator op = expression.getOperator();

        if (op instanceof AdditionOperator || op instanceof SubtractionOperator) {
            return left.equals(right) ? ParityLattice.EVEN : ParityLattice.ODD;
        }

        else if (op instanceof MultiplicationOperator) {
            if (left == ParityLattice.EVEN || right == ParityLattice.EVEN) {
                return ParityLattice.EVEN;
            }
            return ParityLattice.ODD;
        }

        else if (op instanceof ModuloOperator || op instanceof RemainderOperator) {
            if (right == ParityLattice.EVEN) {
                return left;
            }
            return top();
        }

        return top();
    }
}