package it.unive.scsr.analysis.extendedSign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

    @Override
    public ExtendedSignLattice top() { return ExtendedSignLattice.TOP; }

    @Override
    public ExtendedSignLattice bottom() { return ExtendedSignLattice.BOTTOM; }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (constant.getValue() instanceof Integer n) {
            if (n == 0)
                return ExtendedSignLattice.ZERO;
            else if (n > 0)
                return ExtendedSignLattice.POS;
            return ExtendedSignLattice.NEG;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
         if (expression.getOperator() == NumericNegation.INSTANCE){
            if (arg.isBottom()) return ExtendedSignLattice.BOTTOM;
            if (arg.isTop()) return ExtendedSignLattice.TOP;
            if (arg.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
            if (arg.equals(ExtendedSignLattice.NON_ZERO)) return ExtendedSignLattice.NON_ZERO;
            if (arg.equals(ExtendedSignLattice.POS)) return ExtendedSignLattice.NEG;
            if (arg.equals(ExtendedSignLattice.NEG)) return ExtendedSignLattice.POS;
            if (arg.equals(ExtendedSignLattice.NON_POS)) return ExtendedSignLattice.NON_NEG;
            if (arg.equals(ExtendedSignLattice.NON_NEG)) return ExtendedSignLattice.NON_POS;
         }
         return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left.isBottom() || right.isBottom())
            return ExtendedSignLattice.BOTTOM;

        BinaryOperator op = expression.getOperator();

        if (op instanceof AdditionOperator){
            if (left.isTop() || right.isTop()) return ExtendedSignLattice.TOP;
            if (left.equals(ExtendedSignLattice.ZERO)) return right;
            if (right.equals(ExtendedSignLattice.ZERO)) return left;

            if ((left.equals(ExtendedSignLattice.POS) || left.equals(ExtendedSignLattice.NON_NEG)) && (right.equals(ExtendedSignLattice.POS) || right.equals(ExtendedSignLattice.NON_NEG)))
                return (left.equals(ExtendedSignLattice.POS) || right.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.POS : ExtendedSignLattice.NON_NEG;

            if ((left.equals(ExtendedSignLattice.NEG) || left.equals(ExtendedSignLattice.NON_POS)) && (right.equals(ExtendedSignLattice.NEG) || right.equals(ExtendedSignLattice.NON_POS)))
                return (left.equals(ExtendedSignLattice.NEG) || right.equals(ExtendedSignLattice.NEG)) ? ExtendedSignLattice.NEG : ExtendedSignLattice.NON_POS;

            return ExtendedSignLattice.TOP;
        } else if (op instanceof SubtractionOperator) {
            if (left.isTop() || right.isTop()) return ExtendedSignLattice.TOP;
            if (right.equals(ExtendedSignLattice.ZERO)) return left;
            if (left.equals(ExtendedSignLattice.ZERO)) {
                if (right.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
                if (right.equals(ExtendedSignLattice.NON_ZERO)) return ExtendedSignLattice.NON_ZERO;
                if (right.equals(ExtendedSignLattice.POS)) return ExtendedSignLattice.NEG;
                if (right.equals(ExtendedSignLattice.NEG)) return ExtendedSignLattice.POS;
                if (right.equals(ExtendedSignLattice.NON_POS)) return ExtendedSignLattice.NON_NEG;
                if (right.equals(ExtendedSignLattice.NON_NEG)) return ExtendedSignLattice.NON_POS;
            }
            if ((left.equals(ExtendedSignLattice.POS) || left.equals(ExtendedSignLattice.NON_NEG)) && (right.equals(ExtendedSignLattice.NON_POS) || right.equals(ExtendedSignLattice.NEG)))
                return (left.equals(ExtendedSignLattice.NON_NEG) && right.equals(ExtendedSignLattice.NON_POS)) ? ExtendedSignLattice.NON_NEG : ExtendedSignLattice.POS;
            if ((left.equals(ExtendedSignLattice.NEG) || left.equals(ExtendedSignLattice.NON_POS)) && (right.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.POS)))
                return (left.equals(ExtendedSignLattice.NON_POS) && right.equals(ExtendedSignLattice.NON_NEG)) ? ExtendedSignLattice.NON_POS : ExtendedSignLattice.NEG;
            return ExtendedSignLattice.TOP;
        } else if (op instanceof MultiplicationOperator) {
            if (left.equals(ExtendedSignLattice.ZERO) || right.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
            if (left.isTop() || right.isTop()) return ExtendedSignLattice.TOP;

            if ((right.equals(ExtendedSignLattice.NON_ZERO) && (left.equals(ExtendedSignLattice.NON_POS) || left.equals(ExtendedSignLattice.NON_NEG)))
                    || (left.equals(ExtendedSignLattice.NON_ZERO) && (right.equals(ExtendedSignLattice.NON_POS) || right.equals(ExtendedSignLattice.NON_NEG))) ) return ExtendedSignLattice.TOP;
            if (right.equals(ExtendedSignLattice.NON_ZERO) || left.equals(ExtendedSignLattice.NON_ZERO)) return ExtendedSignLattice.NON_ZERO;

            if (left.equals(ExtendedSignLattice.NON_NEG))
                return (right.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.NON_NEG : ExtendedSignLattice.NON_POS;
            if (right.equals(ExtendedSignLattice.NON_NEG))
                return (left.equals(ExtendedSignLattice.NON_NEG) || left.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.NON_NEG : ExtendedSignLattice.NON_POS;

            if (left.equals(ExtendedSignLattice.NON_POS))
                return (right.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.NON_POS : ExtendedSignLattice.NON_NEG;
            if (right.equals(ExtendedSignLattice.NON_POS))
                return (left.equals(ExtendedSignLattice.NON_NEG) || left.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.NON_POS : ExtendedSignLattice.NON_NEG;

            if(left.equals(right)) return ExtendedSignLattice.POS;
            else return ExtendedSignLattice.NEG;
        } else if (op instanceof DivisionOperator) {
            if (right.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.BOTTOM;
            if (left.equals(ExtendedSignLattice.TOP)) return ExtendedSignLattice.TOP;
            if (left.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
            if (right.equals(ExtendedSignLattice.TOP)) return ExtendedSignLattice.TOP;
            if (left.equals(ExtendedSignLattice.NON_ZERO)) return ExtendedSignLattice.NON_ZERO;
            if (right.equals(ExtendedSignLattice.NON_ZERO)) return (left.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.NON_POS)) ? ExtendedSignLattice.TOP : ExtendedSignLattice.NON_ZERO;
            if (left.equals(ExtendedSignLattice.NON_NEG)) return (right.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.NON_NEG : ExtendedSignLattice.NON_POS;
            if (left.equals(ExtendedSignLattice.NON_POS)) return (right.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.NON_POS : ExtendedSignLattice.NON_NEG;
            if (left.equals(ExtendedSignLattice.POS)) return (right.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.POS : ExtendedSignLattice.NEG;
            if (left.equals(ExtendedSignLattice.NEG)) return (right.equals(ExtendedSignLattice.NON_NEG) || right.equals(ExtendedSignLattice.POS)) ? ExtendedSignLattice.NEG : ExtendedSignLattice.POS;
        }else if (expression.getOperator() instanceof ModuloOperator) {
            if (right.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.BOTTOM;
            if (left.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
            if (right.equals(ExtendedSignLattice.POS) || right.equals(ExtendedSignLattice.NON_NEG)) return ExtendedSignLattice.NON_NEG;
            if (right.equals(ExtendedSignLattice.NEG) || right.equals(ExtendedSignLattice.NON_POS)) return ExtendedSignLattice.NON_POS;
            return ExtendedSignLattice.TOP;
        } else if (expression.getOperator() instanceof RemainderOperator) {
            if (right.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.BOTTOM;
            if (left.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
            if (left.equals(ExtendedSignLattice.POS) || left.equals(ExtendedSignLattice.NON_NEG)) return ExtendedSignLattice.NON_NEG;
            if (left.equals(ExtendedSignLattice.NEG) || left.equals(ExtendedSignLattice.NON_POS)) return ExtendedSignLattice.NON_POS;
            return ExtendedSignLattice.TOP;
        }
        return ExtendedSignLattice.TOP;

    }
}
