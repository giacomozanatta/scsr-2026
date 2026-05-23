package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice>{

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

        if(constant.getValue() instanceof Integer) {
            Integer n = (Integer) constant.getValue();
            if(n == 0)
                return ExtendedSignLattice.ZERO;
            else if(n > 0)
                return ExtendedSignLattice.POS;
            return ExtendedSignLattice.NEG;
        }

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp,
                                                   SemanticOracle oracle) throws SemanticException {

        if(expression.getOperator() == NumericNegation.INSTANCE) {
            if(arg == ExtendedSignLattice.NEG)
                return ExtendedSignLattice.POS;
            else if(arg == ExtendedSignLattice.POS)
                return ExtendedSignLattice.NEG;
            else if(arg == ExtendedSignLattice.ZERO)
                return ExtendedSignLattice.ZERO;
            else if(arg == ExtendedSignLattice.LE0)  // -(≤0) = ≥0
                return ExtendedSignLattice.GE0;
            else if(arg == ExtendedSignLattice.GE0)  // -(≥0) = ≤0
                return ExtendedSignLattice.LE0;
            else if(arg == ExtendedSignLattice.NEQ0) // -(≠0) = ≠0
                return ExtendedSignLattice.NEQ0;
            else if(arg == ExtendedSignLattice.TOP)
                return ExtendedSignLattice.TOP;
            else if(arg == ExtendedSignLattice.BOTTOM)
                return ExtendedSignLattice.BOTTOM;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right,
                                                    ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM)
            return ExtendedSignLattice.BOTTOM;
        else if(left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP)
            return ExtendedSignLattice.TOP;

        if(expression.getOperator() instanceof AdditionOperator) {
            return evalAddition(left, right);
        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            return evalMultiplication(left, right);
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            return evalSubtraction(left, right);
        } else if (expression.getOperator() instanceof DivisionOperator) {
            return evalDivision(left, right);
        } else if (expression.getOperator() instanceof ModuloOperator)
            return right;
        else if (expression.getOperator() instanceof RemainderOperator)
            return left;
        return ExtendedSignLattice.TOP;
    }


    /*
     *        | NEG  | ZERO | POS  | LE0  | GE0  | NEQ0
     * -------+------+------+------+------+------+------
     * NEG    | NEG  | NEG  | TOP  | NEG  | TOP  | TOP
     * ZERO   | NEG  | ZERO | POS  | LE0  | GE0  | NEQ0
     * POS    | TOP  | POS  | POS  | TOP  | POS  | TOP
     * LE0    | NEG  | LE0  | TOP  | LE0  | TOP  | TOP
     * GE0    | TOP  | GE0  | POS  | TOP  | GE0  | TOP
     * NEQ0   | TOP  | NEQ0 | TOP  | TOP  | TOP  | TOP
     */
    private ExtendedSignLattice evalAddition(ExtendedSignLattice left, ExtendedSignLattice right) {
        if (left == ExtendedSignLattice.ZERO) return right;
        if (right == ExtendedSignLattice.ZERO) return left;

        // NEG + NEG = NEG (sum of two negatives is always negative)
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG)
            return ExtendedSignLattice.NEG;
        // POS + POS = POS
        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS)
            return ExtendedSignLattice.POS;
        // NEG + POS or POS + NEG: could be NEG, ZERO, or POS
        if ((left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS) ||
                (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG))
            return ExtendedSignLattice.TOP;

        // LE0 -> (-∞, 0]
        // LE0 + LE0: (neg|0) + (neg|0) is always ≤ 0
        if (left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.LE0)
            return ExtendedSignLattice.LE0;
        // LE0 + NEG: (neg|0) + neg is always strictly negative
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.NEG) ||
                (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.NEG;
        // LE0 + POS: (-1)+1=0, (-2)+1=-1, 0+1=1 -> NEG, ZERO, or POS
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.POS) ||
                (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.TOP;
        // LE0 + GE0: (-1)+0=-1, 0+1=1, 0+0=0  -> NEG, ZERO, or POS
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.GE0) ||
                (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.TOP;
        // LE0 + NEQ0: (-1)+1=0, 0+1=1, (-1)+(-1)=-2 -> TOP
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.NEQ0) ||
                (left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.TOP;

        // GE0 -> [0, +∞)
        // GE0 + GE0: (pos|0) + (pos|0) is always ≥ 0
        if (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.GE0)
            return ExtendedSignLattice.GE0;
        // GE0 + POS: (pos|0) + pos is always strictly positive
        if ((left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.POS) ||
                (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.GE0))
            return ExtendedSignLattice.POS;
        // GE0 + NEG: 0+(-1)=-1, 1+(-1)=0, 1+(-2)=-1 -> NEG, ZERO, or POS
        if ((left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.NEG) ||
                (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.GE0))
            return ExtendedSignLattice.TOP;
        // GE0 + NEQ0: 0+1=1, 0+(-1)=-1, 1+(-1)=0 -> TOP
        if ((left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.NEQ0) ||
                (left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.GE0))
            return ExtendedSignLattice.TOP;

        // NEQ0 -> ≠0
        // NEQ0 + NEQ0: 1+(-1)=0 -> TOP
        if (left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.NEQ0)
            return ExtendedSignLattice.TOP;

        return ExtendedSignLattice.TOP;
    }

    /*
     *        | NEG  | ZERO | POS  | LE0  | GE0  | NEQ0
     * -------+------+------+------+------+------+------
     * NEG    | POS  | ZERO | NEG  | GE0  | LE0  | NEQ0
     * ZERO   | ZERO | ZERO | ZERO | ZERO | ZERO | ZERO
     * POS    | NEG  | ZERO | POS  | LE0  | GE0  | NEQ0
     * LE0    | GE0  | ZERO | LE0  | GE0  | LE0  | TOP
     * GE0    | LE0  | ZERO | GE0  | LE0  | GE0  | TOP
     * NEQ0   | NEQ0 | ZERO | NEQ0 | TOP  | TOP  | NEQ0
     */
    private ExtendedSignLattice evalMultiplication(ExtendedSignLattice left, ExtendedSignLattice right) {
        if (left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO)
            return ExtendedSignLattice.ZERO;

        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS)
            return ExtendedSignLattice.POS;
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG)
            return ExtendedSignLattice.POS;
        if ((left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG) ||
                (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS))
            return ExtendedSignLattice.NEG;

        // LE0 -> (-∞,0]   GE0 -> [0, +∞)
        // LE0 × POS: (neg|0)×pos = neg|0 = LE0
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.POS) ||
                (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.LE0;
        // LE0 × NEG: (neg|0)×neg = pos|0 = GE0
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.NEG) ||
                (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.GE0;
        // LE0 × LE0: (neg|0)×(neg|0) = pos|0 = GE0
        if (left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.LE0)
            return ExtendedSignLattice.GE0;
        // LE0 × GE0: (neg|0)×(pos|0) = neg|0 = LE0
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.GE0) ||
                (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.LE0;
        // LE0 × NEQ0: (neg|0)×(neg|pos): 0×anything=0, neg×pos=neg, neg×neg=pos -> NEG|ZERO|POS = TOP
        if ((left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.NEQ0) ||
                (left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.LE0))
            return ExtendedSignLattice.TOP;

        // GE0 -> [0, +∞)   LE0 -> (-∞,0]
        // GE0 × POS: (pos|0)×pos = pos|0 = GE0
        if ((left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.POS) ||
                (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.GE0))
            return ExtendedSignLattice.GE0;
        // GE0 × NEG: (pos|0)×neg = neg|0 = LE0
        if ((left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.NEG) ||
                (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.GE0))
            return ExtendedSignLattice.LE0;
        // GE0 × GE0: (pos|0)×(pos|0) = pos|0 = GE0
        if (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.GE0)
            return ExtendedSignLattice.GE0;
        // GE0 × NEQ0: (pos|0)×(neg|pos): 0×anything=0, pos×pos=pos, pos×neg=neg -> NEG|ZERO|POS = TOP
        if ((left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.NEQ0) ||
                (left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.GE0))
            return ExtendedSignLattice.TOP;

        // NEQ0 -> ≠0   (technically it's always NEQ0)
        // NEQ0 × NEQ0: (neg|pos)×(neg|pos): neg×neg=pos, pos×pos=pos, neg×pos=neg -> NEQ0 (never zero)
        if (left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.NEQ0)
            return ExtendedSignLattice.NEQ0;
        // NEQ0 × POS: (neg|pos)×pos = neg|pos = NEQ0
        if ((left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.POS) ||
                (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEQ0))
            return ExtendedSignLattice.NEQ0;
        // NEQ0 × NEG: (neg|pos)×neg = pos|neg = NEQ0
        if ((left == ExtendedSignLattice.NEQ0 && right == ExtendedSignLattice.NEG) ||
                (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEQ0))
            return ExtendedSignLattice.NEQ0;

        return ExtendedSignLattice.TOP;
    }

    private ExtendedSignLattice evalSubtraction(ExtendedSignLattice left, ExtendedSignLattice right) {
        ExtendedSignLattice negRight; // invert sign, then use addition
        if (right == ExtendedSignLattice.NEG)
            negRight = ExtendedSignLattice.POS;
        else if (right == ExtendedSignLattice.POS)
            negRight = ExtendedSignLattice.NEG;
        else if (right == ExtendedSignLattice.ZERO)
            negRight = ExtendedSignLattice.ZERO;
        else if (right == ExtendedSignLattice.LE0)
            negRight = ExtendedSignLattice.GE0;
        else if (right == ExtendedSignLattice.GE0)
            negRight = ExtendedSignLattice.LE0;
        else if (right == ExtendedSignLattice.NEQ0)
            negRight = ExtendedSignLattice.NEQ0;
        else
            return ExtendedSignLattice.TOP;

        return evalAddition(left, negRight);
    }

    /*
     *        | NEG  | POS  | LE0  | GE0  | NEQ0
     * -------+------+------+------+------+------
     * NEG    | GE0  | LE0  | GE0  | LE0  | TOP
     * ZERO   | ZERO | ZERO | ZERO | ZERO | ZERO
     * POS    | LE0  | GE0  | LE0  | GE0  | TOP
     * LE0    | GE0  | LE0  | GE0  | LE0  | TOP
     * GE0    | LE0  | GE0  | LE0  | GE0  | TOP
     * NEQ0   | TOP  | TOP  | TOP  | TOP  | TOP
     */
    private ExtendedSignLattice evalDivision(ExtendedSignLattice left, ExtendedSignLattice right) {
        // Division by 0
        if (right == ExtendedSignLattice.ZERO)
            return ExtendedSignLattice.BOTTOM;
        // ZERO at numerator
        if (left == ExtendedSignLattice.ZERO)
            return ExtendedSignLattice.ZERO;

        // NEQ0 as divisor, result NEG or POS so TOP
        if (right == ExtendedSignLattice.NEQ0)
            return ExtendedSignLattice.TOP;

        // NEQ0 as dividend, which can be NEG or POS so TOP
        if (left == ExtendedSignLattice.NEQ0)
            return ExtendedSignLattice.TOP;

        // NEG / NEG: negative/negative = positive or 0 (integer truncation) -> GE0
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG)
            return ExtendedSignLattice.GE0;
        // NEG / POS: negative/positive = negative or 0 -> LE0
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS)
            return ExtendedSignLattice.LE0;
        // NEG / LE0 (non-zero part is NEG): same as NEG/NEG -> GE0 (taking the bigger result like with intervals)
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.LE0)
            return ExtendedSignLattice.GE0;
        // NEG / GE0 (non-zero part is POS): same as NEG/POS -> LE0
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.GE0)
            return ExtendedSignLattice.LE0;

        // POS / POS: positive/positive = positive or 0 -> GE0 (e.g. 1/999 ≈ 0)
        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS)
            return ExtendedSignLattice.GE0;
        // POS / NEG: positive/negative = negative or 0 -> LE0
        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG)
            return ExtendedSignLattice.LE0;
        // POS / LE0 (non-zero part is NEG): same as POS/NEG -> LE0
        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.LE0)
            return ExtendedSignLattice.LE0;
        // POS / GE0 (non-zero part is POS): same as POS/POS -> GE0
        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.GE0)
            return ExtendedSignLattice.GE0;

        // LE0 -> (-∞,0]   GE0 -> [0, +∞)
        // LE0 // NEG: (neg|0)/neg = GE0|ZERO = GE0
        if (left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.NEG)
            return ExtendedSignLattice.GE0;
        // LE0 // POS: (neg|0)/pos = LE0|ZERO = LE0
        if (left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.POS)
            return ExtendedSignLattice.LE0;
        // LE0 / LE0: same as LE0/NEG (non-zero part of LE0 is NEG) -> GE0
        if (left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.LE0)
            return ExtendedSignLattice.GE0;
        // LE0 / GE0: same as LE0/POS (non-zero part of GE0 is POS) -> LE0
        if (left == ExtendedSignLattice.LE0 && right == ExtendedSignLattice.GE0)
            return ExtendedSignLattice.LE0;

        // GE0 / POS: (pos|0)/pos = GE0|ZERO = GE0
        if (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.POS)
            return ExtendedSignLattice.GE0;
        // GE0 / NEG: (pos|0)÷neg = LE0|ZERO = LE0
        if (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.NEG)
            return ExtendedSignLattice.LE0;
        // GE0 / GE0: same as GE0/POS -> GE0
        if (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.GE0)
            return ExtendedSignLattice.GE0;
        // GE0 / LE0: same as GE0/NEG -> LE0
        if (left == ExtendedSignLattice.GE0 && right == ExtendedSignLattice.LE0)
            return ExtendedSignLattice.LE0;

        return ExtendedSignLattice.TOP;
    }
}