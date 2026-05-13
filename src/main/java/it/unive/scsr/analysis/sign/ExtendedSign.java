package it.unive.scsr.analysis.sign;

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
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
//TASK REQUEST: to implement two domains in LiSA :Extended Sign
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
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            Integer n = (Integer) constant.getValue();
            if (n == 0)
                return ExtendedSignLattice.ZERO;
            else if (n > 0)
                return ExtendedSignLattice.POS;
            return ExtendedSignLattice.NEG;
        }
        return ExtendedSignLattice.TOP;
    }

    // negate the current value
    private ExtendedSignLattice negate(ExtendedSignLattice sign) {
        if (sign == ExtendedSignLattice.NEG) {
            return ExtendedSignLattice.POS;
        }
        if (sign == ExtendedSignLattice.POS) {
            return ExtendedSignLattice.NEG;

        }
        if (sign == ExtendedSignLattice.ZERO) {
            return ExtendedSignLattice.ZERO;

        }
        if (sign == ExtendedSignLattice.GEQ_ZERO) {
            return ExtendedSignLattice.LEQ_ZERO;
        }
        if (sign == ExtendedSignLattice.LEQ_ZERO) {
            return ExtendedSignLattice.GEQ_ZERO;

        }
        //NEQ_ZERO,TOP,BOTTOM are the same (closed)
        return sign;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE) {
            return negate(arg);
        }
        return ExtendedSignLattice.TOP;

    }

    /*
     * helper method for addition and subtraction
     */
    private ExtendedSignLattice add(ExtendedSignLattice left, ExtendedSignLattice right) throws SemanticException {

        // bottom + any = bottom
        if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
            return ExtendedSignLattice.BOTTOM;
        }
        // zero + any = any
        if (left == ExtendedSignLattice.ZERO) {
            return right;
        }
        if (right == ExtendedSignLattice.ZERO) {
            return left;
        }
        // top + any = top
        if (right == ExtendedSignLattice.TOP || left == ExtendedSignLattice.TOP) {
            return ExtendedSignLattice.TOP;
        }
        // neg + neg = neg
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG) {
            return ExtendedSignLattice.NEG;
        }
        // neg + leq_zero (zero or negative) = negative
        if ((left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.LEQ_ZERO)
                || (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.NEG)) {
            return ExtendedSignLattice.NEG;
        }
        // pos + pos = pos
        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS) {
            return ExtendedSignLattice.POS;
        }
        // pos + geq_zero (zero or positive) = pos
        if ((left == ExtendedSignLattice.POS && right == ExtendedSignLattice.GEQ_ZERO)
                || (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.POS)) {
            return ExtendedSignLattice.POS;
        }
        // geq_zero + geq_zero = geq_zero
        if (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO) {
            return ExtendedSignLattice.GEQ_ZERO;
        }
        // leq_zero + leq_zero = leq_zero
        if (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO) {
            return ExtendedSignLattice.LEQ_ZERO;
        }

        // all the other combinations result in TOP
        return ExtendedSignLattice.TOP;
    }

    /*
     * helper method for multiplication
     */
    private ExtendedSignLattice mul(ExtendedSignLattice left, ExtendedSignLattice right) throws SemanticException {
        if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
            return ExtendedSignLattice.BOTTOM;
        }
        // 0 * any = 0
        if (left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO) {
            return ExtendedSignLattice.ZERO;
        }
        if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
            return ExtendedSignLattice.TOP;
        }
        // BASIC PAIRS
        // pos * pos = pos
        if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS) {
            return ExtendedSignLattice.POS;
        }
        // neg * neg = pos
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG) {
            return ExtendedSignLattice.POS;
        }
        // neg * pos = neg
        if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS
                || left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG) {
            return ExtendedSignLattice.NEG;
        }

        // COMPOUNT OPERATIONS WITH NEG
        // neg * leq_zero = geq_zero
        if ((left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.LEQ_ZERO)
                || (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.NEG)) {
            return ExtendedSignLattice.GEQ_ZERO;
        }
        // neg * geq_zero = leq_zero
        if ((left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.GEQ_ZERO)
                || (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.NEG)) {
            return ExtendedSignLattice.LEQ_ZERO;
        }
        // neg *neq_zero = neq_zero
        if ((left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEQ_ZERO)
                || (left == ExtendedSignLattice.NEQ_ZERO && right == ExtendedSignLattice.NEG)) {
            return ExtendedSignLattice.NEQ_ZERO;
        }

        // COMPOUND OPERATIONS WITH POS
        // pos * leq_zero = leq_zero
        if ((left == ExtendedSignLattice.POS && right == ExtendedSignLattice.LEQ_ZERO)
                || (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.POS)) {
            return ExtendedSignLattice.LEQ_ZERO;
        }
        // pos * geq_zero = geq_zero
        if ((left == ExtendedSignLattice.POS && right == ExtendedSignLattice.GEQ_ZERO)
                || (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.POS)) {
            return ExtendedSignLattice.GEQ_ZERO;
        }
        // pos * neq_zero = neq_zero
        if ((left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEQ_ZERO)
                || (left == ExtendedSignLattice.NEQ_ZERO && right == ExtendedSignLattice.POS)) {
            return ExtendedSignLattice.NEQ_ZERO;
        }
        // COMPOUND PAIRS
        // leq_zero * leq_zero = geq_zero
        if (left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO) {
            return ExtendedSignLattice.GEQ_ZERO;
        }
        // geq_zero * geq_zero = geq_zero
        if (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO) {
            return ExtendedSignLattice.GEQ_ZERO;
        }
        // leq_zero * geq_zero = leq_zero
        if ((left == ExtendedSignLattice.LEQ_ZERO && right == ExtendedSignLattice.GEQ_ZERO)
                || (left == ExtendedSignLattice.GEQ_ZERO && right == ExtendedSignLattice.LEQ_ZERO)) {
            return ExtendedSignLattice.LEQ_ZERO;
        }
        // neq_zero * neq_zero = neq_zero
        if (left == ExtendedSignLattice.NEQ_ZERO && right == ExtendedSignLattice.NEQ_ZERO) {
            return ExtendedSignLattice.NEQ_ZERO;
        }
        // all the other combinations result in TOP
        return ExtendedSignLattice.TOP;
    }

    private ExtendedSignLattice div(ExtendedSignLattice left, ExtendedSignLattice right) throws SemanticException {
        // division by zero
        if (right == ExtendedSignLattice.ZERO) {
            return ExtendedSignLattice.BOTTOM;
        }
        if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
            return ExtendedSignLattice.BOTTOM;
        }
        // 0 / any (except 0) = 0
        if (left == ExtendedSignLattice.ZERO) {
            return ExtendedSignLattice.ZERO;
        }
        if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
            return ExtendedSignLattice.TOP;
        }
        // Integer division: x/pos may be 0, so sign is weakened
         if (right == ExtendedSignLattice.POS) {
            // pos / pos = geq_zero example: 1/1 = 1 but 1/2 = 0 since it's integer
            if (left == ExtendedSignLattice.POS) {
                return ExtendedSignLattice.GEQ_ZERO;
            }
            // neg / pos = leq_zero
            if (left == ExtendedSignLattice.NEG) {
                return ExtendedSignLattice.LEQ_ZERO;
            }
            // we know that the result is not negative but it can be zero
            if (left == ExtendedSignLattice.GEQ_ZERO) {
                return ExtendedSignLattice.GEQ_ZERO;
            }

            if (left == ExtendedSignLattice.LEQ_ZERO) {
                return ExtendedSignLattice.LEQ_ZERO;
            }
        }

        if (right == ExtendedSignLattice.NEG) {
            if (left == ExtendedSignLattice.POS) {
                return ExtendedSignLattice.LEQ_ZERO;
            }
            if (left == ExtendedSignLattice.NEG) {
                return ExtendedSignLattice.GEQ_ZERO;
            }
            if (left == ExtendedSignLattice.GEQ_ZERO) {
                return ExtendedSignLattice.LEQ_ZERO;
            }
            if (left == ExtendedSignLattice.LEQ_ZERO) {
                return ExtendedSignLattice.GEQ_ZERO;
            }
        }
        if (right == ExtendedSignLattice.NEQ_ZERO) {
            if (left == ExtendedSignLattice.ZERO) {
                return ExtendedSignLattice.ZERO;
            }
        }
        //for the other combinations we cannot be sure of the result so we return top
        //for example:
        //geq_zero/geq_zero can be either pos or zero depending on the value of the numerator and denominator
        //we return top becasue it's a sound over-approximation of the result
        // also for NEQ_ZERO we return top since it includes both pos and neg, so the result can be either pos, neg or zero depending on the actual value of the denominator
        //i also created a DivideByZeroExtendedSignChecker that warns about if zero is in the range of the denominator
        return ExtendedSignLattice.TOP;
    }

    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
            ExtendedSignLattice right,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() instanceof AdditionOperator) {
            return add(left, right);
        }
        if (expression.getOperator() instanceof SubtractionOperator) {
            // a - b = a + (-b)
            return add(left, negate(right));
        }
        if (expression.getOperator() instanceof MultiplicationOperator) {
            return mul(left, right);
        }
        if (expression.getOperator() instanceof DivisionOperator) {
            return div(left, right);
        }
        return ExtendedSignLattice.TOP;
    }

    // copied from sign for completion
    @Override
    public Satisfiability satisfiesBinaryExpression(
            BinaryExpression expression,
            ExtendedSignLattice left,
            ExtendedSignLattice right,
            ProgramPoint pp,
            SemanticOracle oracle) {
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
            // e1 <= e2 same as !(e1 > e2)
            return left.gt(right).negate();
        else if (operator == ComparisonLt.INSTANCE)
            // e1 < e2 -> !(e1 >= e2) && !(e1 == e2)
            return left.gt(right).negate().and(left.eq(right).negate());
        else if (operator == ComparisonNe.INSTANCE)
            return left.eq(right).negate();
        else
            return Satisfiability.UNKNOWN;
    }

    // copied from sign for completion
    @Override
    public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(
            ValueEnvironment<ExtendedSignLattice> environment,
            BinaryExpression expression,
            ProgramPoint src,
            ProgramPoint dest,
            SemanticOracle oracle)
            throws SemanticException {
        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED)
            return environment.bottom();
        if (sat == Satisfiability.SATISFIED)
            return environment;

        Identifier id;
        ExtendedSignLattice eval;
        boolean rightIsExpr;
        BinaryOperator operator = expression.getOperator();
        ValueExpression left = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();
        if (left instanceof Identifier) {
            eval = eval(environment, right, src, oracle);
            id = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            eval = eval(environment, left, src, oracle);
            id = (Identifier) right;
            rightIsExpr = false;
        } else
            return environment;

        ExtendedSignLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom())
            return environment.bottom();

        ExtendedSignLattice update = null;
        if (operator == ComparisonEq.INSTANCE)
            update = starting.glb(eval);
        else {
            // the rule for an operator op is:
            // - if `start op eval`, `update = U { start n v | v op eval, v in {
            // +, 0, -} }`
            // - if `eval op start`, `update = U { start n v | eval op v, v in {
            // +, 0, -} }`

            ExtendedSignLattice[] all = new ExtendedSignLattice[] { ExtendedSignLattice.NEG, ExtendedSignLattice.ZERO,
                    ExtendedSignLattice.POS };
            if (operator == ComparisonGe.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        if (s.gt(eval).or(s.eq(eval)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        if (eval.gt(s).or(eval.eq(s)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLe.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (s.gt(eval).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (eval.gt(s).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLt.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (eval.gt(s).or(eval.eq(s)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonGt.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        if (s.gt(eval).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        if (eval.gt(s).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
        }

        if (update == null)
            return environment;
        else if (update.isBottom())
            return environment.bottom();
        else
            return environment.putState(id, update);
    }

}
