package it.unive.scsr.analysis.extendedSign;

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

/**
 * Extended Sign abstract domain.
 *
 * Tracks whether integer variables are:
 *   Z<0  (NEG), Z=0 (ZERO), Z>0 (POS),
 *   Z≤0  (NON_POS), Z≥0 (NON_NEG), Z≠0 (NON_ZERO),
 *   Z    (TOP),  ∅   (BOTTOM)
 *
 * Compared to the basic Sign domain, the extended version is more precise
 * because it can express, e.g., that a value is non-negative after a max(0,x)
 * pattern, or that a divisor is non-zero, enabling better detection of
 * division-by-zero warnings.
 */
public class ExtendedSign
        implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

    // -----------------------------------------------------------------------
    // BaseNonRelationalValueDomain – top / bottom
    // -----------------------------------------------------------------------

    @Override
    public ExtendedSignLattice top() {
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return ExtendedSignLattice.BOTTOM;
    }

    // -----------------------------------------------------------------------
    // Constant evaluation
    // -----------------------------------------------------------------------

    @Override
    public ExtendedSignLattice evalConstant(
            Constant constant,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        Object val = constant.getValue();
        if (val instanceof Integer) {
            int n = (Integer) val;
            if (n > 0)  return ExtendedSignLattice.POS;
            if (n < 0)  return ExtendedSignLattice.NEG;
            return ExtendedSignLattice.ZERO;
        }
        if (val instanceof Long) {
            long n = (Long) val;
            if (n > 0)  return ExtendedSignLattice.POS;
            if (n < 0)  return ExtendedSignLattice.NEG;
            return ExtendedSignLattice.ZERO;
        }
        // For other constant types (strings, floats, …) we over-approximate
        return ExtendedSignLattice.TOP;
    }

    // -----------------------------------------------------------------------
    // Unary expression evaluation
    // -----------------------------------------------------------------------

    @Override
    public ExtendedSignLattice evalUnaryExpression(
            UnaryExpression expression,
            ExtendedSignLattice arg,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            // -POS = NEG, -NEG = POS, -ZERO = ZERO
            // -NON_NEG = NON_POS, -NON_POS = NON_NEG, -NON_ZERO = NON_ZERO
            if (arg.equals(ExtendedSignLattice.POS))      return ExtendedSignLattice.NEG;
            if (arg.equals(ExtendedSignLattice.NEG))      return ExtendedSignLattice.POS;
            if (arg.equals(ExtendedSignLattice.ZERO))     return ExtendedSignLattice.ZERO;
            if (arg.equals(ExtendedSignLattice.NON_NEG))  return ExtendedSignLattice.NON_POS;
            if (arg.equals(ExtendedSignLattice.NON_POS))  return ExtendedSignLattice.NON_NEG;
            if (arg.equals(ExtendedSignLattice.NON_ZERO)) return ExtendedSignLattice.NON_ZERO;
            if (arg.equals(ExtendedSignLattice.TOP))      return ExtendedSignLattice.TOP;
            if (arg.equals(ExtendedSignLattice.BOTTOM))   return ExtendedSignLattice.BOTTOM;
        }

        return ExtendedSignLattice.TOP;
    }

    // -----------------------------------------------------------------------
    // Binary expression evaluation
    // -----------------------------------------------------------------------

    @Override
    public ExtendedSignLattice evalBinaryExpression(
            BinaryExpression expression,
            ExtendedSignLattice left,
            ExtendedSignLattice right,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        if (left.isBottom() || right.isBottom())
            return ExtendedSignLattice.BOTTOM;

        BinaryOperator op = expression.getOperator();

        if (op instanceof AdditionOperator)
            return evalAdd(left, right);
        if (op instanceof SubtractionOperator)
            return evalSub(left, right);
        if (op instanceof MultiplicationOperator)
            return evalMul(left, right);
        if (op instanceof DivisionOperator)
            return evalDiv(left, right);
        if (op instanceof ModuloOperator)
            return right; // result has same sign as divisor
        if (op instanceof RemainderOperator)
            return left;  // result has same sign as dividend

        return ExtendedSignLattice.TOP;
    }

    // -----------------------------------------------------------------------
    // Arithmetic helpers
    // -----------------------------------------------------------------------

    private ExtendedSignLattice evalAdd(ExtendedSignLattice l, ExtendedSignLattice r) {
        // POS + POS = POS
        if (l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.POS))
            return ExtendedSignLattice.POS;
        // NEG + NEG = NEG
        if (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.NEG))
            return ExtendedSignLattice.NEG;
        // ZERO + x = x
        if (l.equals(ExtendedSignLattice.ZERO)) return r;
        if (r.equals(ExtendedSignLattice.ZERO)) return l;
        // POS + NEG or NEG + POS → unknown sign
        if ((l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.NEG))
                || (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.POS)))
            return ExtendedSignLattice.TOP;
        // NON_NEG + NON_NEG = NON_NEG  (>=0 + >=0 >= 0)
        if (l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.NON_NEG))
            return ExtendedSignLattice.NON_NEG;
        // NON_POS + NON_POS = NON_POS
        if (l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.NON_POS))
            return ExtendedSignLattice.NON_POS;
        // POS + NON_NEG = POS, NON_NEG + POS = POS
        if ((l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.NON_NEG))
                || (l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.POS)))
            return ExtendedSignLattice.POS;
        // NEG + NON_POS = NEG, NON_POS + NEG = NEG
        if ((l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.NON_POS))
                || (l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.NEG)))
            return ExtendedSignLattice.NEG;
        return ExtendedSignLattice.TOP;
    }

    private ExtendedSignLattice evalSub(ExtendedSignLattice l, ExtendedSignLattice r) {
        // a - b = a + (-b)
        ExtendedSignLattice negR = negate(r);
        return evalAdd(l, negR);
    }

    private ExtendedSignLattice evalMul(ExtendedSignLattice l, ExtendedSignLattice r) {
        // Anything * ZERO = ZERO  (and vice versa)
        if (l.equals(ExtendedSignLattice.ZERO) || r.equals(ExtendedSignLattice.ZERO))
            return ExtendedSignLattice.ZERO;
        // POS * POS = POS, NEG * NEG = POS
        if ((l.equals(ExtendedSignLattice.POS)  && r.equals(ExtendedSignLattice.POS))
                || (l.equals(ExtendedSignLattice.NEG)  && r.equals(ExtendedSignLattice.NEG)))
            return ExtendedSignLattice.POS;
        // POS * NEG = NEG, NEG * POS = NEG
        if ((l.equals(ExtendedSignLattice.POS)  && r.equals(ExtendedSignLattice.NEG))
                || (l.equals(ExtendedSignLattice.NEG)  && r.equals(ExtendedSignLattice.POS)))
            return ExtendedSignLattice.NEG;
        // NON_NEG * NON_NEG = NON_NEG
        if (l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.NON_NEG))
            return ExtendedSignLattice.NON_NEG;
        // NON_POS * NON_POS = NON_NEG  (non-pos * non-pos >= 0)
        if (l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.NON_POS))
            return ExtendedSignLattice.NON_NEG;
        // NON_NEG * NON_POS = NON_POS
        if ((l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.NON_POS))
                || (l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.NON_NEG)))
            return ExtendedSignLattice.NON_POS;
        // NON_NEG * POS = NON_NEG
        if ((l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.POS))
        || (l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.NON_NEG)))
            return ExtendedSignLattice.NON_NEG;
        // NON_POS * NEG = NON_NEG
        if ((l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.NEG))
                || (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.NON_POS)))
            return ExtendedSignLattice.NON_NEG;
        // NON_POS * POS = NON_POS
        if ((l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.POS))
                || (l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.NON_POS)))
            return ExtendedSignLattice.NON_POS;
        // NON_NEG * NEG = NON_POS
        if ((l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.NEG))
                || (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.NON_NEG)))
            return ExtendedSignLattice.NON_POS;
        // NON_ZERO * NON_ZERO = NON_ZERO
        if ((l.equals(ExtendedSignLattice.NON_ZERO) && r.equals(ExtendedSignLattice.NON_ZERO))
                || (l.equals(ExtendedSignLattice.NON_ZERO) && r.equals(ExtendedSignLattice.POS))
                || (l.equals(ExtendedSignLattice.NON_ZERO) && r.equals(ExtendedSignLattice.NEG))
                || (l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.NON_ZERO))
                || (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.NON_ZERO)))
            return ExtendedSignLattice.NON_ZERO;
        return ExtendedSignLattice.TOP;
    }

    private ExtendedSignLattice evalDiv(ExtendedSignLattice l, ExtendedSignLattice r) {
        // Division by zero → BOTTOM (unreachable / undefined)
        if (r.equals(ExtendedSignLattice.ZERO))
            return ExtendedSignLattice.BOTTOM;
        // 0 / anything = ZERO
        if (l.equals(ExtendedSignLattice.ZERO))
            return ExtendedSignLattice.ZERO;
        // If divisor cannot be zero we can give a precise result
        if (!r.canBeZero()) {
            // same-sign division → POS
            if ((l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.POS))
                    || (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.NEG))
                    || (l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.NON_ZERO)
                    && r.isDefinitelyPositive())
                    || (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.NON_ZERO)
                    && r.isDefinitelyNegative()))
                return ExtendedSignLattice.NON_NEG; // integer division may give 0 (3/7 = 0)
            // opposite-sign → NEG
            if ((l.equals(ExtendedSignLattice.POS) && r.equals(ExtendedSignLattice.NEG))
                    || (l.equals(ExtendedSignLattice.NEG) && r.equals(ExtendedSignLattice.POS)))
                return ExtendedSignLattice.NON_POS;
            // NON_NEG / NON_ZERO (non-zero positive divisor) → NON_NEG
            if (l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.POS))
                return ExtendedSignLattice.NON_NEG;
            // NON_NEG / NEG (non-zero positive divisor) → NON_NEG
            if (l.equals(ExtendedSignLattice.NON_NEG) && r.equals(ExtendedSignLattice.NEG))
                return ExtendedSignLattice.NON_POS;
            // NON_POS / NEG (non-zero positive divisor) → NON_NEG
            if (l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.NEG))
                return ExtendedSignLattice.NON_NEG;
            // NON_POS / positive → NON_POS
            if (l.equals(ExtendedSignLattice.NON_POS) && r.equals(ExtendedSignLattice.POS))
                return ExtendedSignLattice.NON_POS;
        }
        return ExtendedSignLattice.TOP;
    }

    /**
     * Negates an abstract sign element (used for subtraction rewriting).
     */
    private static ExtendedSignLattice negate(ExtendedSignLattice s) {
        if (s.equals(ExtendedSignLattice.POS))      return ExtendedSignLattice.NEG;
        if (s.equals(ExtendedSignLattice.NEG))      return ExtendedSignLattice.POS;
        if (s.equals(ExtendedSignLattice.ZERO))     return ExtendedSignLattice.ZERO;
        if (s.equals(ExtendedSignLattice.NON_NEG))  return ExtendedSignLattice.NON_POS;
        if (s.equals(ExtendedSignLattice.NON_POS))  return ExtendedSignLattice.NON_NEG;
        if (s.equals(ExtendedSignLattice.NON_ZERO)) return ExtendedSignLattice.NON_ZERO;
        return s; // TOP or BOTTOM unchanged
    }

    // -----------------------------------------------------------------------
    // Satisfiability
    // -----------------------------------------------------------------------

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
        else if (operator == ComparisonNe.INSTANCE)
            return left.eq(right).negate();
        else if (operator == ComparisonGt.INSTANCE)
            return left.gt(right);
        else if (operator == ComparisonGe.INSTANCE)
            return left.gt(right).or(left.eq(right));
        else if (operator == ComparisonLt.INSTANCE)
            return left.gt(right).negate().and(left.eq(right).negate());
        else if (operator == ComparisonLe.INSTANCE)
            return left.gt(right).negate();

        return Satisfiability.UNKNOWN;
    }

    // -----------------------------------------------------------------------
    // Assume (refine environment from guard conditions)
    // -----------------------------------------------------------------------

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

        BinaryOperator operator = expression.getOperator();
        ValueExpression left  = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();

        Identifier id;
        ExtendedSignLattice eval;
        boolean rightIsExpr;

        if (left instanceof Identifier) {
            eval        = eval(environment, right, src, oracle);
            id          = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            eval        = eval(environment, left, src, oracle);
            id          = (Identifier) right;
            rightIsExpr = false;
        } else {
            return environment;
        }

        ExtendedSignLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom())
            return environment.bottom();

        ExtendedSignLattice update = null;

        // All concrete sign elements for exhaustive refinement
        ExtendedSignLattice[] concrete = {
                ExtendedSignLattice.NEG,
                ExtendedSignLattice.ZERO,
                ExtendedSignLattice.POS
        };

        if (operator == ComparisonEq.INSTANCE) {
            update = starting.glb(eval);

        } else if (operator == ComparisonGt.INSTANCE) {
            // id > eval  →  keep only values v in starting s.t. v > eval is possible
            for (ExtendedSignLattice s : concrete) {
                if (rightIsExpr ? s.gt(eval).mightBeTrue() : eval.gt(s).mightBeTrue())
                    update = update == null
                            ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonGe.INSTANCE) {
            for (ExtendedSignLattice s : concrete) {
                boolean cond = rightIsExpr
                        ? s.gt(eval).or(s.eq(eval)).mightBeTrue()
                        : eval.gt(s).or(eval.eq(s)).mightBeTrue();
                if (cond)
                    update = update == null
                            ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonLt.INSTANCE) {
            for (ExtendedSignLattice s : concrete) {
                boolean cond = rightIsExpr
                        ? s.gt(eval).or(s.eq(eval)).mightBeFalse()
                        : eval.gt(s).or(eval.eq(s)).mightBeFalse();
                if (cond)
                    update = update == null
                            ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonLe.INSTANCE) {
            for (ExtendedSignLattice s : concrete) {
                boolean cond = rightIsExpr
                        ? s.gt(eval).mightBeFalse()
                        : eval.gt(s).mightBeFalse();
                if (cond)
                    update = update == null
                            ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonNe.INSTANCE) {
            // id != eval → refine: if eval is ZERO, result is NON_ZERO, etc.
            if (eval.equals(ExtendedSignLattice.ZERO))
                update = starting.glb(ExtendedSignLattice.NON_ZERO);
            else if (eval.equals(ExtendedSignLattice.POS))
                update = starting.glb(ExtendedSignLattice.NON_POS).lub(
                        starting.glb(ExtendedSignLattice.NEG));
            else if (eval.equals(ExtendedSignLattice.NEG))
                update = starting.glb(ExtendedSignLattice.NON_NEG).lub(
                        starting.glb(ExtendedSignLattice.POS));
        }

        if (update == null)
            return environment;
        if (update.isBottom())
            return environment.bottom();
        return environment.putState(id, update);
    }
}