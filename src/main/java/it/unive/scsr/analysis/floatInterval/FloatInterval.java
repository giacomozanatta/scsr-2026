package it.unive.scsr.analysis.floatInterval;

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
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.scsr.analysis.interval.IntervalLattice;

/**
 * Abstract domain for floating-point interval analysis.
 *
 * Extends the integer Interval domain to support real-valued constants
 * (float / double). Each variable is mapped to a closed interval [low, high]
 * over ℝ ∪ {-∞, +∞}.
 *
 * Key differences from the integer domain:
 *   1. Constants of type Float and Double are now handled precisely.
 *   2. Division properly manages the case where the divisor interval
 *      contains or touches zero (see FloatIntervalLattice.div).
 *   3. The widening operator uses a threshold-based strategy that avoids
 *      immediately jumping to ±∞ for real-valued bounds (the interval [0,1]
 *      contains infinitely many points, so the naive integer widening would
 *      never terminate for shrinking sequences; our approach stabilises
 *      after a bounded number of steps).
 *   4. assumeBinaryExpression performs interval-based refinement compatible
 *      with real-valued bounds.
 */
public class FloatInterval implements BaseNonRelationalValueDomain<FloatIntervalLattice> {

    // -----------------------------------------------------------------------
    // BaseNonRelationalValueDomain
    // -----------------------------------------------------------------------

    @Override
    public FloatIntervalLattice top() {
        return FloatIntervalLattice.TOP;
    }

    @Override
    public FloatIntervalLattice bottom() {
        return FloatIntervalLattice.BOTTOM;
    }

    // -----------------------------------------------------------------------
    // Constant evaluation
    // -----------------------------------------------------------------------

    /**
     * Handles Integer, Long, Float, and Double constants.
     * Each concrete value c is mapped to the singleton interval [c, c].
     */
    @Override
    public FloatIntervalLattice evalConstant(
            Constant constant,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        Object val = constant.getValue();

        if (val instanceof Integer) {
            double v = ((Integer) val).doubleValue();
            return new FloatIntervalLattice(v, v);
        }
        if (val instanceof Long) {
            double v = ((Long) val).doubleValue();
            return new FloatIntervalLattice(v, v);
        }
        if (val instanceof Float) {
            double v = ((Float) val).doubleValue();
            return new FloatIntervalLattice(v, v);
        }
        if (val instanceof Double) {
            double v = (Double) val;
            return new FloatIntervalLattice(v, v);
        }

        // Non-numeric constants (strings, booleans, …) → TOP
        return FloatIntervalLattice.TOP;
    }

    // -----------------------------------------------------------------------
    // Unary expression evaluation
    // -----------------------------------------------------------------------

    @Override
    public FloatIntervalLattice evalUnaryExpression(
            UnaryExpression expression,
            FloatIntervalLattice arg,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        if (arg.isBottom()) return FloatIntervalLattice.BOTTOM;

        if (expression.getOperator() == NumericNegation.INSTANCE)
            return negate(arg);

        return FloatIntervalLattice.TOP;
    }

    // -----------------------------------------------------------------------
    // Binary expression evaluation
    // -----------------------------------------------------------------------

    @Override
    public FloatIntervalLattice evalBinaryExpression(
            BinaryExpression expression,
            FloatIntervalLattice left,
            FloatIntervalLattice right,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        if (left.isBottom() || right.isBottom())
            return FloatIntervalLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator)
            return add(left, right);

        if (expression.getOperator() instanceof SubtractionOperator)
            return sub(left, right);

        if (expression.getOperator() instanceof MultiplicationOperator)
            return mul(left, right);

        if (expression.getOperator() instanceof DivisionOperator)
            return div(left, right);

        return FloatIntervalLattice.TOP;
    }

    // -----------------------------------------------------------------------
    // Arithmetic operations (moved here from FloatIntervalLattice)
    // -----------------------------------------------------------------------

    /**
     * [a,b] + [c,d] = [a+c, b+d]
     */
    private FloatIntervalLattice add(FloatIntervalLattice l, FloatIntervalLattice r) {
        return new FloatIntervalLattice(l.getLow() + r.getLow(), l.getHigh() + r.getHigh());
    }

    /**
     * [a,b] - [c,d] = [a-d, b-c]
     */
    private FloatIntervalLattice sub(FloatIntervalLattice l, FloatIntervalLattice r) {
        return new FloatIntervalLattice(l.getLow() - r.getHigh(), l.getHigh() - r.getLow());
    }

    /**
     * [a,b] * [c,d] = [min(ac,ad,bc,bd), max(ac,ad,bc,bd)]
     */
    private FloatIntervalLattice mul(FloatIntervalLattice l, FloatIntervalLattice r) {
        double a = l.getLow(),  b = l.getHigh();
        double c = r.getLow(),  d = r.getHigh();

        double ac = a * c, ad = a * d;
        double bc = b * c, bd = b * d;

        double lo = Math.min(Math.min(ac, ad), Math.min(bc, bd));
        double hi = Math.max(Math.max(ac, ad), Math.max(bc, bd));
        return new FloatIntervalLattice(lo, hi);
    }

    private FloatIntervalLattice div(FloatIntervalLattice l, FloatIntervalLattice r) {
        double a = l.getLow();
        double b = l.getHigh();
        double c = r.getLow();
        double d = r.getHigh();

        if (c == 0.0 && d == 0.0)
            return FloatIntervalLattice.BOTTOM;

        if (c <= 0.0 && d >= 0.0)
            return FloatIntervalLattice.TOP;

        double ac = a/c;
        double ad = a/d;
        double bc = b/c;
        double bd = b/d;

        double lo = Math.min(Math.min(ac, ad), Math.min(bc, bd));
        double hi = Math.max(Math.max(ac, ad), Math.max(bc, bd));


        return new FloatIntervalLattice(Math.nextDown(lo), Math.nextUp(hi));
    }

    /**
     * -[a,b] = [-b, -a]
     */
    private FloatIntervalLattice negate(FloatIntervalLattice i) {
        return new FloatIntervalLattice(-i.getHigh(), -i.getLow());
    }

    // -----------------------------------------------------------------------
    // Satisfiability
    // -----------------------------------------------------------------------

//    @Override
//    public Satisfiability satisfiesBinaryExpression(
//            BinaryExpression expression,
//            FloatIntervalLattice left,
//            FloatIntervalLattice right,
//            ProgramPoint pp,
//            SemanticOracle oracle) {
//
//        if (left.isBottom() || right.isBottom()) return Satisfiability.BOTTOM;
//        if (left.isTop()    || right.isTop())    return Satisfiability.UNKNOWN;
//
//        double ll = left.getLow(),  lh = left.getHigh();
//        double rl = right.getLow(), rh = right.getHigh();
//
//        BinaryOperator op = expression.getOperator();
//
//        if (op == ComparisonEq.INSTANCE) {
//            // [a,a] == [a,a] → SATISFIED
//            if (ll == lh && rl == rh && ll == rl) return Satisfiability.SATISFIED;
//            // Intervals disjoint → NOT_SATISFIED
//            if (lh < rl || rh < ll)               return Satisfiability.NOT_SATISFIED;
//            return Satisfiability.UNKNOWN;
//        }
//        if (op == ComparisonNe.INSTANCE) {
//            if (ll == lh && rl == rh && ll == rl) return Satisfiability.NOT_SATISFIED;
//            if (lh < rl || rh < ll)               return Satisfiability.SATISFIED;
//            return Satisfiability.UNKNOWN;
//        }
//        if (op == ComparisonLt.INSTANCE) {
//            if (lh < rl)  return Satisfiability.SATISFIED;
//            if (ll >= rh) return Satisfiability.NOT_SATISFIED;
//            return Satisfiability.UNKNOWN;
//        }
//        if (op == ComparisonLe.INSTANCE) {
//            if (lh <= rl) return Satisfiability.SATISFIED;
//            if (ll > rh)  return Satisfiability.NOT_SATISFIED;
//            return Satisfiability.UNKNOWN;
//        }
//        if (op == ComparisonGt.INSTANCE) {
//            if (ll > rh)  return Satisfiability.SATISFIED;
//            if (lh <= rl) return Satisfiability.NOT_SATISFIED;
//            return Satisfiability.UNKNOWN;
//        }
//        if (op == ComparisonGe.INSTANCE) {
//            if (ll >= rh) return Satisfiability.SATISFIED;
//            if (lh < rl)  return Satisfiability.NOT_SATISFIED;
//            return Satisfiability.UNKNOWN;
//        }
//
//        return Satisfiability.UNKNOWN;
//    }
//
//    // -----------------------------------------------------------------------
//    // Assume – environment refinement from guard conditions
//    //
//    // For real-valued intervals, the refinement is exactly:
//    //
//    //   assume(x < c)  → x ∈ [x.low, min(x.high, nextDown(c))]
//    //   assume(x <= c) → x ∈ [x.low, min(x.high, c)]
//    //   assume(x > c)  → x ∈ [max(x.low, nextUp(c)), x.high]
//    //   assume(x >= c) → x ∈ [max(x.low, c), x.high]
//    //   assume(x == c) → x ∈ x ∩ [c, c]
//    //   assume(x != c) → we cannot refine precisely; keep x unchanged
//    //                    (a more precise treatment would require disjunctions)
//    // -----------------------------------------------------------------------
//
//    @Override
//    public ValueEnvironment<FloatIntervalLattice> assumeBinaryExpression(
//            ValueEnvironment<FloatIntervalLattice> environment,
//            BinaryExpression expression,
//            ProgramPoint src,
//            ProgramPoint dest,
//            SemanticOracle oracle)
//            throws SemanticException {
//
//        Satisfiability sat = satisfies(environment, expression, src, oracle);
//        if (sat == Satisfiability.NOT_SATISFIED) return environment.bottom();
//        if (sat == Satisfiability.SATISFIED)     return environment;
//
//        BinaryOperator operator = expression.getOperator();
//        ValueExpression left    = (ValueExpression) expression.getLeft();
//        ValueExpression right   = (ValueExpression) expression.getRight();
//
//        Identifier id;
//        FloatIntervalLattice eval;
//        boolean leftIsId; // true if the identifier is on the left side
//
//        if (left instanceof Identifier) {
//            id       = (Identifier) left;
//            eval     = this.eval(environment, right, src, oracle);
//            leftIsId = true;
//        } else if (right instanceof Identifier) {
//            id       = (Identifier) right;
//            eval     = this.eval(environment, left, src, oracle);
//            leftIsId = false;
//        } else {
//            return environment;
//        }
//
//        FloatIntervalLattice current = environment.getState(id);
//        if (current.isBottom() || eval.isBottom()) return environment.bottom();
//
//        double cl = current.getLow(),  ch = current.getHigh();
//        double el = eval.getLow(),     eh = eval.getHigh();
//
//        FloatIntervalLattice refined = refine(operator, cl, ch, el, eh, leftIsId);
//
//        if (refined == null)           return environment;
//        if (refined.isBottom())        return environment.bottom();
//        return environment.putState(id, refined);
//    }
//
//    /**
//     * Computes the refined interval for the variable {@code id} after
//     * the guard {@code id op eval} (or {@code eval op id} when !leftIsId).
//     *
//     * @param op       comparison operator
//     * @param cl / ch  current bounds of the variable
//     * @param el / eh  bounds of the evaluated expression
//     * @param leftIsId true if the variable is on the left side of the operator
//     * @return the refined interval, or null if no refinement is possible
//     */
//    private FloatIntervalLattice refine(
//            BinaryOperator op,
//            double cl, double ch,
//            double el, double eh,
//            boolean leftIsId) {
//
//        // Normalise: we always compute the constraint on the left-hand variable.
//        // If the variable is on the right, we flip the operator.
//        if (!leftIsId) {
//            op = flip(op);
//            // Swap bounds for the eval side (we treat eval as the left operand now)
//            double tmp;
//            tmp = cl; cl = el; el = tmp;
//            tmp = ch; ch = eh; eh = tmp;
//        }
//
//        // Now: variable (bounds cl..ch)  op  expression (bounds el..eh)
//        if (op == ComparisonEq.INSTANCE) {
//            // x == [el,eh]  →  x ∩ [el,eh]
//            double lo = Math.max(cl, el);
//            double hi = Math.min(ch, eh);
//            return new FloatIntervalLattice(lo, hi); // BOTTOM if lo > hi
//        }
//        if (op == ComparisonLt.INSTANCE) {
//            // x < [el,eh]  →  x < eh (strictest upper bound)
//            // For reals: x ∈ [cl, min(ch, nextDown(eh))]
//            double hi = Math.min(ch, Math.nextDown(eh));
//            return new FloatIntervalLattice(cl, hi);
//        }
//        if (op == ComparisonLe.INSTANCE) {
//            // x <= [el,eh]  →  x ∈ [cl, min(ch, eh)]
//            double hi = Math.min(ch, eh);
//            return new FloatIntervalLattice(cl, hi);
//        }
//        if (op == ComparisonGt.INSTANCE) {
//            // x > [el,eh]  →  x > el (need x above the minimum of rhs)
//            double lo = Math.max(cl, Math.nextUp(el));
//            return new FloatIntervalLattice(lo, ch);
//        }
//        if (op == ComparisonGe.INSTANCE) {
//            // x >= [el,eh]  →  x ∈ [max(cl, el), ch]
//            double lo = Math.max(cl, el);
//            return new FloatIntervalLattice(lo, ch);
//        }
//        if (op == ComparisonNe.INSTANCE) {
//            // x != c: no interval refinement without disjunctions → keep as-is
//            return new FloatIntervalLattice(cl, ch);
//        }
//
//        return null; // unknown operator
//    }
//
//    /**
//     * Flips a comparison operator (used when the variable is on the right side).
//     * e.g. {@code eval < x}  becomes  {@code x > eval}.
//     */
//    private static BinaryOperator flip(BinaryOperator op) {
//        if (op == ComparisonLt.INSTANCE) return ComparisonGt.INSTANCE;
//        if (op == ComparisonLe.INSTANCE) return ComparisonGe.INSTANCE;
//        if (op == ComparisonGt.INSTANCE) return ComparisonLt.INSTANCE;
//        if (op == ComparisonGe.INSTANCE) return ComparisonLe.INSTANCE;
//        return op; // EQ and NE are symmetric
//    }
}