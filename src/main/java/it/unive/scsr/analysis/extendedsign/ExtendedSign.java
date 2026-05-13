package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.scsr.analysis.sign.SignLattice;

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
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            Integer n = (Integer) constant.getValue();
            if(n < 0)
                return ExtendedSignLattice.LT_ZERO;
            if(n == 0)
                return ExtendedSignLattice.ZERO;
            return ExtendedSignLattice.GT_ZERO;
        }

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE) {
            if (arg == ExtendedSignLattice.TOP)
                return ExtendedSignLattice.TOP;
            if (arg == ExtendedSignLattice.BOTTOM)
                return ExtendedSignLattice.BOTTOM;
            if (arg == ExtendedSignLattice.LT_ZERO)         // -(<0) = >0
                return ExtendedSignLattice.GT_ZERO;
            if (arg == ExtendedSignLattice.GT_ZERO)         // -(>0) = <0
                return ExtendedSignLattice.LT_ZERO;
            if (arg == ExtendedSignLattice.LE_ZERO)         // -(≤0) = ≥0
                return ExtendedSignLattice.GE_ZERO;
            if (arg == ExtendedSignLattice.GE_ZERO)         // -(≥0) = ≤0
                return ExtendedSignLattice.LE_ZERO;
            if (arg == ExtendedSignLattice.NE_ZERO)         // -(≠0) = ≠0
                return ExtendedSignLattice.NE_ZERO;
            if (arg == ExtendedSignLattice.ZERO)            // -(0) = 0
                return ExtendedSignLattice.ZERO;
            return ExtendedSignLattice.TOP;
        }

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP)
            return ExtendedSignLattice.TOP;
        if(left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM)
            return ExtendedSignLattice.BOTTOM;

        if(expression.getOperator() instanceof AdditionOperator) {
            if(left == ExtendedSignLattice.ZERO)    return right;
            if(right == ExtendedSignLattice.ZERO)   return left;

            // <0 + <0 -> always negative
            if(left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.LT_ZERO;

            // ≤0 + ≤0 -> zero or negative
            if(left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.LE_ZERO)
                return ExtendedSignLattice.LE_ZERO;

            // >0 + >0 -> always positive
            if(left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.GT_ZERO;

            // ≥0 + ≥0 -> zero or positive
            if(left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.GE_ZERO)
                return ExtendedSignLattice.GE_ZERO;

            // ≤0 + <0 -> always negative, never zero
            if((left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.LT_ZERO) || (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LE_ZERO))
                return ExtendedSignLattice.LT_ZERO;

            // ≥0 + >0 -> always positive, never zero
            if((left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.GT_ZERO) || (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GE_ZERO))
                return ExtendedSignLattice.GT_ZERO;

            // <0 + >0 -> unknown
            if((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO) || (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO))
                return ExtendedSignLattice.TOP;

            // ≠0 + ≠0 -> unknown
            if(left == ExtendedSignLattice.NE_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.TOP;

            return ExtendedSignLattice.TOP;
        }
        if(expression.getOperator() instanceof SubtractionOperator) {
            if(left == ExtendedSignLattice.ZERO) {
                // 0 - x; must negate x
                if(right == ExtendedSignLattice.LT_ZERO)    return ExtendedSignLattice.GT_ZERO;
                if(right == ExtendedSignLattice.GT_ZERO)    return ExtendedSignLattice.LT_ZERO;
                if(right == ExtendedSignLattice.LE_ZERO)    return ExtendedSignLattice.GE_ZERO;
                if(right == ExtendedSignLattice.GE_ZERO)    return ExtendedSignLattice.LE_ZERO;
                if(right == ExtendedSignLattice.NE_ZERO)    return ExtendedSignLattice.NE_ZERO;
                if(right == ExtendedSignLattice.ZERO)       return  ExtendedSignLattice.ZERO;
            }
            if(right == ExtendedSignLattice.ZERO)   return left;

            // <0 - <0 -> <0 + >0 → unknown
            if (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.TOP;

            // ≤0 - ≤0 → ≤0 + ≥0 -> unknown
            if (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.LE_ZERO)
                return ExtendedSignLattice.TOP;

            // >0 - >0 → >0 + <0 -> unknown
            if (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.TOP;

            // ≥0 - ≥0 → ≥0 + ≤0 -> unknown
            if (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.GE_ZERO)
                return ExtendedSignLattice.TOP;

            // <0 - >0 = <0 + <0 -> always negative, never zero
            if (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.LT_ZERO;

            // ≤0 - >0 = ≤0 + <0 -> always negative, never zero
            if (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.LT_ZERO;

            // <0 - ≥0 = <0 + ≤0 -> always negative, never zero
            if (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GE_ZERO)
                return ExtendedSignLattice.LT_ZERO;

            // ≤0 - ≥0 -> ≤0 + ≤0 -> always negative; could be zero
            if (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.GE_ZERO)
                return ExtendedSignLattice.LE_ZERO;

            // ≥0 - ≤0 -> ≥0 + ≥0 -> always positive; could be zero
            if (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.GT_ZERO;

            // ≥0 - <0 -> ≥0 + >0 -> always positive
            if (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.GT_ZERO;

            // >0 - ≤0 → >0 + ≥0 -> always positive
            if (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LE_ZERO)
                return ExtendedSignLattice.GT_ZERO;

            // ≥0 - ≤0 -> ≥0 + ≥0 -> always positive; could be zero
            if (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.LE_ZERO)
                return ExtendedSignLattice.GE_ZERO;

            // ≠0 + ≠0 -> unknown
            if (left == ExtendedSignLattice.NE_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.TOP;

            return ExtendedSignLattice.TOP;
        }
        if(expression.getOperator() instanceof MultiplicationOperator) {
            if (left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO)
                return ExtendedSignLattice.ZERO;

            // <0 * <0 -> always positive
            // >0 * >0 -> always positive
            if ((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LT_ZERO) || (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GT_ZERO))
                return ExtendedSignLattice.GT_ZERO;

            // <0 * >0 -> always negative
            // >0 * <0 -> always negative
            if ((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO) || (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO))
                return ExtendedSignLattice.LT_ZERO;

            // ≠0 * ≠0
            if (left == ExtendedSignLattice.NE_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.NE_ZERO;

            // ≥0 * ≥0
            if (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.GE_ZERO)
                return ExtendedSignLattice.GE_ZERO;

            // ≤0 * ≤0
            if (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.LE_ZERO)
                return ExtendedSignLattice.GE_ZERO;

            // ≤0 * ≥0
            if ((left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.GE_ZERO) ||
                    (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.LE_ZERO))
                return ExtendedSignLattice.LE_ZERO;

            // <0 * ≥0
            if ((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GE_ZERO) ||
                    (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.LT_ZERO))
                return ExtendedSignLattice.LE_ZERO;

            // >0 * ≤0
            if ((left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LE_ZERO) ||
                    (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.GT_ZERO))
                return ExtendedSignLattice.LE_ZERO;

            // >0 * ≥0
            if ((left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GE_ZERO) ||
                    (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.GT_ZERO))
                return ExtendedSignLattice.GE_ZERO;

            // <0 * ≤0
            if ((left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LE_ZERO) ||
                    (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.LT_ZERO))
                return ExtendedSignLattice.GE_ZERO;

            return ExtendedSignLattice.TOP;
        }
        if(expression.getOperator() instanceof DivisionOperator) {
            if(right == ExtendedSignLattice.ZERO)       return ExtendedSignLattice.BOTTOM;
            if(right == ExtendedSignLattice.LE_ZERO)    return ExtendedSignLattice.BOTTOM;
            if(right == ExtendedSignLattice.GE_ZERO)    return ExtendedSignLattice.BOTTOM;
            if(right == ExtendedSignLattice.TOP)        return ExtendedSignLattice.BOTTOM;

            if(left == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;

            // <0 / <0 -> always positive; could be zero
            if(left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.GE_ZERO;
            if (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.GT_ZERO;
            if (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.LT_ZERO;
            if (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.LT_ZERO;
            if (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.GE_ZERO;
            if (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.LE_ZERO;
            if (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.GE_ZERO;
            if (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.LE_ZERO;
            if (left == ExtendedSignLattice.LT_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.NE_ZERO;
            if (left == ExtendedSignLattice.GT_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.NE_ZERO;
            if (left == ExtendedSignLattice.LE_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.TOP;
            if (left == ExtendedSignLattice.GE_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.TOP;
            if (left == ExtendedSignLattice.NE_ZERO && right == ExtendedSignLattice.LT_ZERO)
                return ExtendedSignLattice.TOP;
            if (left == ExtendedSignLattice.NE_ZERO && right == ExtendedSignLattice.GT_ZERO)
                return ExtendedSignLattice.TOP;
            if (left == ExtendedSignLattice.NE_ZERO && right == ExtendedSignLattice.NE_ZERO)
                return ExtendedSignLattice.TOP;

            return ExtendedSignLattice.TOP;
        }
        if(expression.getOperator() instanceof ModuloOperator) {
            return right;
        }
        if(expression.getOperator() instanceof RemainderOperator) {
            return left;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) {
        if (left.isTop() || right.isTop())
            return Satisfiability.UNKNOWN;

        BinaryOperator operator = expression.getOperator();
        if (operator == ComparisonEq.INSTANCE)
            return left.eq(right);
        else if (operator == ComparisonGe.INSTANCE)
            return left.eq(right).or(left.gt(right));
        else if (operator == ComparisonGt.INSTANCE)
            return left.gt(right);
        else if (operator == ComparisonLe.INSTANCE)
            return left.gt(right).negate();
        else if (operator == ComparisonLt.INSTANCE)
            return left.gt(right).negate().and(left.eq(right).negate());
        else if (operator == ComparisonNe.INSTANCE)
            return left.eq(right).negate();
        else
            return Satisfiability.UNKNOWN;
    }
}
