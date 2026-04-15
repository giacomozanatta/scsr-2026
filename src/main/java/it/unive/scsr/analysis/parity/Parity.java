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

public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

    @Override
    public ParityLattice top(){
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice bottom() {
        return ParityLattice.BOTTOM;
    }

    @Override
    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(constant.getValue() instanceof Integer integer){
            if ( integer % 2 == 0)
                return ParityLattice.EVEN;
            else
                return ParityLattice.ODD;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE)
            if (arg == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            else if (arg == ParityLattice.ODD)
                return ParityLattice.ODD;
            else if (arg == ParityLattice.BOTTOM)
                return ParityLattice.BOTTOM;
            else if (arg == ParityLattice.TOP)
                return ParityLattice.TOP;
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() instanceof AdditionOperator)
            return evalAdditionParity(left,right);
        else if (expression.getOperator() instanceof MultiplicationOperator)
            return evalMultiplicationParity(left,right);
        else if (expression.getOperator() instanceof SubtractionOperator)
            return evalSubtractionParity(left,right);
        else if (expression.getOperator() instanceof DivisionOperator)
            return evalDivisionParity(left,right);
        else if (expression.getOperator() instanceof ModuloOperator)
            return evalModuloParity(left,right);
        else if (expression.getOperator() instanceof RemainderOperator)
            return evalRemainderOperator(left,right);

        return ParityLattice.TOP;
    }

    public ParityLattice evalAdditionParity(ParityLattice left, ParityLattice right){
        return evalAdditionSubtractionParity(left,right);
    }

    public ParityLattice evalMultiplicationParity(ParityLattice left, ParityLattice right){
        if ((left == ParityLattice.EVEN && right == ParityLattice.ODD) || (left == ParityLattice.ODD && right == ParityLattice.EVEN))
            return ParityLattice.EVEN;
        else if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
            return ParityLattice.EVEN;
        else if (left == ParityLattice.ODD && right == ParityLattice.ODD)
            return ParityLattice.ODD;
        else if ( (left == ParityLattice.EVEN && right == ParityLattice.TOP) || (left == ParityLattice.TOP && right == ParityLattice.EVEN))
            return ParityLattice.EVEN;
        else if ( (left == ParityLattice.ODD && right == ParityLattice.TOP) || (left == ParityLattice.TOP && right == ParityLattice.ODD))
            return ParityLattice.TOP;
        else if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
            return ParityLattice.BOTTOM;
        else if (left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;

        return ParityLattice.TOP;
    }

    public ParityLattice evalSubtractionParity(ParityLattice left, ParityLattice right){
        return evalAdditionSubtractionParity(left,right);
    }

    public ParityLattice evalAdditionSubtractionParity(ParityLattice left ,ParityLattice right){
        if ((left == ParityLattice.EVEN && right==ParityLattice.ODD) || (left == ParityLattice.ODD && right == ParityLattice.EVEN))
            return ParityLattice.ODD;
        else if ((left == ParityLattice.EVEN && right == ParityLattice.EVEN) || (left == ParityLattice.ODD && right == ParityLattice.ODD))
            return ParityLattice.EVEN;
        else if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
            return ParityLattice.BOTTOM;
        else if (left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;

        return ParityLattice.TOP;
    }

    public ParityLattice evalDivisionParity(ParityLattice left, ParityLattice right){
        if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
            return ParityLattice.BOTTOM;
        else if (left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;

        /*
        * EVEN : 2n
        * ODD : 2n+1
        *
        * If left and right even:       2n/2m = n/m     -> TOP
        * If left even and right odd:   2n/(2m+1)       -> TOP
        * If left and right odd:        (2n+1)/(2m+1)   -> TOP
        * If left odd and right even:   (2n+1)/2m       -> TOP
        * */
        return ParityLattice.TOP;
    }

    public ParityLattice evalModuloParity(ParityLattice left, ParityLattice right){
        // EVEN : 2n
        // ODD : 2n+1

        if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
            // If left and right even:      r = (2n)-(q*2m) = 2n-2mq = 2(n-mq)                  -> EVEN
            return ParityLattice.EVEN;
        else if (left == ParityLattice.ODD && right == ParityLattice.EVEN)
            // if left odd and right even : r = (2n+1)-(q*2m) = 2n+1-2mq = 2(n-mq)+1            -> ODD
            return ParityLattice.ODD;
        else if ((left == ParityLattice.EVEN && right == ParityLattice.ODD) || (left == ParityLattice.ODD && right == ParityLattice.ODD))
            // If left even and right odd: r = (2n)-(q*(2m+1)) = 2n-2mq-q = 2(n-mq)-q           -> TOP
            // if left and right odd:      r = (2n+1)-(q*(2m+1)) = 2n+1-2mq-q = 2(n-mq)-(q-1)   -> TOP
            return ParityLattice.TOP;
        else if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
            return ParityLattice.BOTTOM;
        else if (left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;

        return ParityLattice.TOP;
    }

    public ParityLattice evalRemainderOperator(ParityLattice left, ParityLattice right){
        if(left == ParityLattice.EVEN && right == ParityLattice.EVEN)
            return ParityLattice.EVEN;
        else if (left == ParityLattice.ODD && right == ParityLattice.EVEN)
            return ParityLattice.ODD;
        else if ((left == ParityLattice.EVEN && right == ParityLattice.ODD) || (left == ParityLattice.ODD && right == ParityLattice.ODD))
            return ParityLattice.TOP;
        else if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
            return ParityLattice.BOTTOM;
        else if (left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;

        return ParityLattice.TOP;
    }
}
