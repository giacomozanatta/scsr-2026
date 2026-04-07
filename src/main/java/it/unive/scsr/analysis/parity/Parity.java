package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.scsr.analysis.sign.SignLattice;


public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {



    @Override
    public ParityLattice top() {
        return ParityLattice.getTOP();
    }

    @Override
    public ParityLattice bottom() {
        return ParityLattice.getBOTTOM();
    }

    @Override
    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle){
        if(constant.getValue() instanceof Integer){
            Integer c = (Integer) constant.getValue();
            if(c % 2 == 0)
                return ParityLattice.getEVEN();
            else
                return ParityLattice.getODD();
        }
        return ParityLattice.getTOP();
    }

    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle){
        if(expression.getOperator() == NumericNegation.INSTANCE)
            return arg;
        return top();
    }

    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle){
        if(left.isTop() || right.isTop())
            return ParityLattice.getTOP();
        if(left.isBottom() || right.isBottom())
            return ParityLattice.getBOTTOM();
        if(expression.getOperator() instanceof AdditionOperator || expression.getOperator() instanceof SubtractionOperator){
            if(right.equals(left))
                return ParityLattice.getEVEN();
            else
                return ParityLattice.getODD();
        }
        else if(expression.getOperator() instanceof MultiplicationOperator){
            if(left == ParityLattice.getEVEN() || right == ParityLattice.getEVEN())
                return ParityLattice.getEVEN();
            else
                return ParityLattice.getODD();
        }
        else if(expression.getOperator() instanceof ModuloOperator || expression.getOperator() instanceof RemainderOperator){
            if(right == ParityLattice.getEVEN())
                return left;
            else
                return ParityLattice.getTOP();
        }
        return ParityLattice.getTOP();
    }
}
