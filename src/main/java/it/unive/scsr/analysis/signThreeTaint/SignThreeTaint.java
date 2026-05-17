package it.unive.scsr.analysis.signThreeTaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.combination.LatticeProduct;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.scsr.analysis.extendedSign.ExtendedSign;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

/**
 * Cartesian Product domain: ExtendedSign × ThreeTaint.
 *
 * <p>We use LiSA's built-in {@link LatticeProduct} as the lattice element type.
 * This means we do NOT need a hand-written lattice class: LatticeProduct
 * already provides component-wise lub, glb, lessOrEqual, top, and bottom
 * for free, following the formal definition from the slides:
 *
 * <pre>
 *   (s1, t1) ⊔ (s2, t2) = (s1 ⊔_S s2, t1 ⊔_T t2)
 *   (s1, t1) ⊑ (s2, t2) iff s1 ⊑_S s2  AND  t1 ⊑_T t2
 * </pre>
 *
 * <p>The two components are accessed via the public fields inherited from
 * CartesianCombination:
 * <ul>
 *   <li>{@code pair.first}  — the {@link ExtendedSignLattice} component</li>
 *   <li>{@code pair.second} — the {@link TaintThreeLevelsLattice} component</li>
 * </ul>
 */
public class SignThreeTaint
        implements BaseNonRelationalValueDomain<LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice>> {

    // -----------------------------------------------------------------------
    // Sub-domain instances — stateless, used only for delegation
    // -----------------------------------------------------------------------

    private final ExtendedSign     signDomain  = new ExtendedSign();
    private final TaintThreeLevels taintDomain = new TaintThreeLevels();

    // -----------------------------------------------------------------------
    // Convenience factory
    // -----------------------------------------------------------------------

    private LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> pair(
            ExtendedSignLattice s, TaintThreeLevelsLattice t) {
        return new LatticeProduct<>(s, t);
    }

    // -----------------------------------------------------------------------
    // BaseNonRelationalValueDomain — top / bottom
    // -----------------------------------------------------------------------

    @Override
    public LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> top() {
        return pair(signDomain.top(), taintDomain.top());
    }

    @Override
    public LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> bottom() {
        return pair(signDomain.bottom(), taintDomain.bottom());
    }

    // -----------------------------------------------------------------------
    // Constant evaluation
    // -----------------------------------------------------------------------

    /**
     * Literal constants are always CLEAN (they come from the program text,
     * not from an external source) and carry a precise sign from
     * {@link ExtendedSign}.
     */
    @Override
    public LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> evalConstant(
            Constant constant,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        ExtendedSignLattice     s = signDomain.evalConstant(constant, pp, oracle);
        TaintThreeLevelsLattice t =  new TaintThreeLevelsLattice().clean();
        return pair(s, t);
    }

    // -----------------------------------------------------------------------
    // PushAny — models unknown / externally-provided values
    // -----------------------------------------------------------------------

    /**
     * Values read from external sources (__any__ in IMP) are TAINTED with
     * unknown sign (TOP). This is the primary taint source in the domain.
     */
    @Override
    public LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> evalPushAny(
            PushAny pushAny,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        return pair(signDomain.top(),  new TaintThreeLevelsLattice().tainted());
    }

    // -----------------------------------------------------------------------
    // Unary expression evaluation
    // -----------------------------------------------------------------------

    /**
     * Taint propagates unchanged through unary operations (no sanitisation).
     * Sign is computed by the {@link ExtendedSign} sub-domain.
     */
    @Override
    public LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> evalUnaryExpression(
            UnaryExpression expression,
            LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> arg,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        if (arg.isBottom()) return bottom();

        ExtendedSignLattice     s = signDomain.evalUnaryExpression(expression, arg.first, pp, oracle);
        TaintThreeLevelsLattice t = arg.second;  // taint unchanged
        return pair(s, t);
    }

    // -----------------------------------------------------------------------
    // Binary expression evaluation
    // -----------------------------------------------------------------------

    /**
     * Sign is computed component-wise by {@link ExtendedSign}.
     *
     * Taint propagates from either operand via {@code or()}:
     * <pre>
     *   T or C = T,   C or C = C,   T or T = T,   ⊤ or x = ⊤
     * </pre>
     * This correctly models that any operation involving a tainted operand
     * produces a tainted result.
     */
    @Override
    public LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> evalBinaryExpression(
            BinaryExpression expression,
            LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> left,
            LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> right,
            ProgramPoint pp,
            SemanticOracle oracle)
            throws SemanticException {

        if (left.isBottom() || right.isBottom()) return bottom();

        ExtendedSignLattice s = signDomain.evalBinaryExpression(
                expression, left.first, right.first, pp, oracle);

        TaintThreeLevelsLattice t;
        try {
            t = left.second.or(right.second);
        } catch (SemanticException e) {
            t = taintDomain.top();
        }

        return pair(s, t);
    }

    // -----------------------------------------------------------------------
    // Satisfiability
    // -----------------------------------------------------------------------

    /**
     * Satisfiability is driven by the Sign component only.
     * Taint does not influence whether a guard condition holds.
     */
    @Override
    public Satisfiability satisfiesBinaryExpression(
            BinaryExpression expression,
            LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> left,
            LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> right,
            ProgramPoint pp,
            SemanticOracle oracle) {

        try {
            return signDomain.satisfiesBinaryExpression(
                    expression, left.first, right.first, pp, oracle);
        } catch (Exception e) {
            return Satisfiability.UNKNOWN;
        }
    }

    // -----------------------------------------------------------------------
    // Assume — environment refinement from guard conditions
    // -----------------------------------------------------------------------

    /**
     * Guards refine only the Sign component. The Taint component of the
     * variable is preserved unchanged: a numeric guard such as
     * {@code if (x > 0)} does not sanitise a tainted value.
     */
    @Override
    public ValueEnvironment<LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice>> assumeBinaryExpression(
            ValueEnvironment<LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice>> environment,
            BinaryExpression expression,
            ProgramPoint src,
            ProgramPoint dest,
            SemanticOracle oracle)
            throws SemanticException {

        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED) return environment.bottom();
        if (sat == Satisfiability.SATISFIED)     return environment;

        BinaryOperator operator = expression.getOperator();
        ValueExpression left    = (ValueExpression) expression.getLeft();
        ValueExpression right   = (ValueExpression) expression.getRight();

        Identifier id;
        LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> eval;
        boolean leftIsId;

        if (left instanceof Identifier) {
            id       = (Identifier) left;
            eval     = this.eval(environment, right, src, oracle);
            leftIsId = true;
        } else if (right instanceof Identifier) {
            id       = (Identifier) right;
            eval     = this.eval(environment, left, src, oracle);
            leftIsId = false;
        } else {
            return environment;
        }

        LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice> current =
                environment.getState(id);

        if (current.isBottom() || eval.isBottom())
            return environment.bottom();

        // ── Refine the sign component (taint is preserved unchanged) ────────
        ExtendedSignLattice starting = current.first;
        ExtendedSignLattice evalSign = eval.first;
        ExtendedSignLattice[] concrete = {
                ExtendedSignLattice.NEG,
                ExtendedSignLattice.ZERO,
                ExtendedSignLattice.POS
        };
        ExtendedSignLattice update = null;

        if (operator == ComparisonEq.INSTANCE) {
            update = starting.glb(evalSign);

        } else if (operator == ComparisonGt.INSTANCE) {
            for (ExtendedSignLattice s : concrete) {
                boolean cond = leftIsId ? s.gt(evalSign).mightBeTrue()
                        : evalSign.gt(s).mightBeTrue();
                if (cond)
                    update = update == null ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonGe.INSTANCE) {
            for (ExtendedSignLattice s : concrete) {
                boolean cond = leftIsId ? s.gt(evalSign).or(s.eq(evalSign)).mightBeTrue()
                        : evalSign.gt(s).or(evalSign.eq(s)).mightBeTrue();
                if (cond)
                    update = update == null ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonLt.INSTANCE) {
            for (ExtendedSignLattice s : concrete) {
                boolean cond = leftIsId ? s.gt(evalSign).or(s.eq(evalSign)).mightBeFalse()
                        : evalSign.gt(s).or(evalSign.eq(s)).mightBeFalse();
                if (cond)
                    update = update == null ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonLe.INSTANCE) {
            for (ExtendedSignLattice s : concrete) {
                boolean cond = leftIsId ? s.gt(evalSign).mightBeFalse()
                        : evalSign.gt(s).mightBeFalse();
                if (cond)
                    update = update == null ? starting.glb(s)
                            : update.lub(starting.glb(s));
            }
        } else if (operator == ComparisonNe.INSTANCE) {
            // Key improvement over basic Sign: x != 0 → refine to NON_ZERO
            if (evalSign.equals(ExtendedSignLattice.ZERO))
                update = starting.glb(ExtendedSignLattice.NON_ZERO);
            else if (evalSign.equals(ExtendedSignLattice.POS))
                update = starting.glb(ExtendedSignLattice.NON_POS)
                        .lub(starting.glb(ExtendedSignLattice.NEG));
            else if (evalSign.equals(ExtendedSignLattice.NEG))
                update = starting.glb(ExtendedSignLattice.NON_NEG)
                        .lub(starting.glb(ExtendedSignLattice.POS));
        }

        if (update == null)        return environment;
        if (update.isBottom())     return environment.bottom();

        return environment.putState(id, pair(update, current.second));
    }
}