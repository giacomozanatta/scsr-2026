package it.unive.scsr.analysis.extendedsignXthreetaint;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.Objects;

/**
 * Cartesian Product lattice: Sign × ThreeTaint.
 *
 * Each element is a pair (sign, taint).
 * Operations are applied component-wise:
 *   (s1, t1) ⊔ (s2, t2) = (s1 ⊔ s2,  t1 ⊔ t2)
 *   (s1, t1) ⊓ (s2, t2) = (s1 ⊓ s2,  t1 ⊓ t2)
 *   (s1, t1) ≤ (s2, t2) iff s1 ≤ s2  AND  t1 ≤ t2
 *
 * TOP    = (Sign.TOP,    Taint.Top)
 * BOTTOM = (Sign.BOTTOM, Taint.Bottom)
 *
 * Note: if either component is ⊥ the whole pair is ⊥
 * (unreachable state), so isBottom() checks both.
 */
public class ExtendedSignXTaintLattice implements BaseLattice<ExtendedSignXTaintLattice> {

    public static final ExtendedSignXTaintLattice TOP =
            new ExtendedSignXTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.Top);
    public static final ExtendedSignXTaintLattice BOTTOM =
            new ExtendedSignXTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);

    private final ExtendedSignLattice sign;
    private final TaintThreeLevelsLattice taint;

    public ExtendedSignXTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign  = sign;
        this.taint = taint;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public ExtendedSignLattice getSign()  { return sign;  }
    public TaintThreeLevelsLattice getTaint() { return taint; }

    // ── Lattice interface ─────────────────────────────────────────────────────

    @Override
    public ExtendedSignXTaintLattice top() { return TOP; }

    @Override
    public ExtendedSignXTaintLattice bottom() { return BOTTOM; }

    /**
     * A pair is TOP iff both components are at their top.
     * Overriding the default so the framework uses our definition.
     */
    @Override
    public boolean isTop() {
        return sign.isTop() && taint.isTop();
    }

    /**
     * A pair is BOTTOM if either component is ⊥.
     * If the sign is ⊥, the variable is unreachable regardless of taint.
     */
    @Override
    public boolean isBottom() {
        return sign.isBottom() || taint.isBottom();
    }

    @Override
    public ExtendedSignXTaintLattice lubAux(ExtendedSignXTaintLattice other) throws SemanticException {
        // Called only when neither is TOP/BOTTOM (framework guarantees this).
        // Component-wise lub.
        return new ExtendedSignXTaintLattice(
                sign.lub(other.sign),
                taint.lub(other.taint)
        );
    }

    @Override
    public ExtendedSignXTaintLattice glbAux(ExtendedSignXTaintLattice other) throws SemanticException {
        return new ExtendedSignXTaintLattice(
                sign.glb(other.sign),
                taint.glb(other.taint)
        );
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignXTaintLattice other) throws SemanticException {
        // Component-wise ordering.
        return sign.lessOrEqual(other.sign) && taint.lessOrEqual(other.taint);
    }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExtendedSignXTaintLattice)) return false;
        ExtendedSignXTaintLattice o = (ExtendedSignXTaintLattice) obj;
        return Objects.equals(sign, o.sign) && Objects.equals(taint, o.taint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, taint);
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom()) return Lattice.bottomRepresentation();
        if (isTop())    return Lattice.topRepresentation();
        return new StringRepresentation(
                "(" + sign.representation() + ", " + taint.representation() + ")"
        );
    }
}