package it.unive.scsr.analysis.extendedSign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
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
        }
    }
}
