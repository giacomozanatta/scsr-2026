package it.unive.scsr.analysis.extendedSign;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/**
 * Lattice for the Extended Sign domain.
 *
 * The Hasse diagram is:
 *
 *              Z  (TOP)
 *           /  |  \
 *         Z≤0  Z≠0  Z≥0
 *         | \ / \ / |
 *        Z<0  Z=0  Z>0
 *           \  |  /
 *              ∅  (BOTTOM)
 *
 * Ordering (bottom-up):
 *   BOTTOM < NEG < NON_POS < TOP
 *   BOTTOM < ZERO < NON_POS < TOP
 *   BOTTOM < ZERO < NON_NEG < TOP
 *   BOTTOM < POS  < NON_NEG < TOP
 *   BOTTOM < NEG  < NON_ZERO < TOP
 *   BOTTOM < POS  < NON_ZERO < TOP
 */
public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    // -----------------------------------------------------------------------
    // Element encoding
    // -----------------------------------------------------------------------
    //  0 = TOP      (Z)
    //  1 = NON_NEG  (Z≥0)
    //  2 = NON_POS  (Z≤0)
    //  3 = NON_ZERO (Z≠0)
    //  4 = POS      (Z>0)
    //  5 = ZERO     (Z=0)
    //  6 = NEG      (Z<0)
    //  7 = BOTTOM   (∅)

    private final int element;

    // --- Singleton instances ---
    public static final ExtendedSignLattice TOP      = new ExtendedSignLattice(0);
    public static final ExtendedSignLattice NON_NEG  = new ExtendedSignLattice(1); // Z≥0
    public static final ExtendedSignLattice NON_POS  = new ExtendedSignLattice(2); // Z≤0
    public static final ExtendedSignLattice NON_ZERO = new ExtendedSignLattice(3); // Z≠0
    public static final ExtendedSignLattice POS      = new ExtendedSignLattice(4); // Z>0
    public static final ExtendedSignLattice ZERO     = new ExtendedSignLattice(5); // Z=0
    public static final ExtendedSignLattice NEG      = new ExtendedSignLattice(6); // Z<0
    public static final ExtendedSignLattice BOTTOM   = new ExtendedSignLattice(7);

    public ExtendedSignLattice(int element) {
        this.element = element;
    }

    // -----------------------------------------------------------------------
    // BaseLattice interface
    // -----------------------------------------------------------------------

    @Override
    public ExtendedSignLattice top() {
        return TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return BOTTOM;
    }

    /**
     * Least Upper Bound (join).
     *
     * Called only when neither this nor other is TOP/BOTTOM (handled by BaseLattice).
     * We implement the full table according to the Hasse diagram.
     */
    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        // Symmetric cases: if same element lubAux would not be called (BaseLattice handles it)
        // but we add the check for safety.
        if (this.equals(other))
            return this;

        // POS lub ZERO  = NON_NEG  (Z>0 ∪ Z=0 = Z≥0)
        if (isPosAndZero(this, other))
            return NON_NEG;

        // NEG lub ZERO  = NON_POS  (Z<0 ∪ Z=0 = Z≤0)
        if (isNegAndZero(this, other))
            return NON_POS;

        // POS lub NEG   = NON_ZERO (Z>0 ∪ Z<0 = Z≠0)
        if (isPosAndNeg(this, other))
            return NON_ZERO;

        // NON_NEG lub NON_POS   → TOP  (covers all integers)
        if (isElements(this, other, NON_NEG, NON_POS))
            return TOP;

        // NON_NEG lub NON_ZERO  → TOP  (needs negative too)
        if (isElements(this, other, NON_NEG, NON_ZERO))
            return TOP;

        // NON_POS lub NON_ZERO  → TOP
        if (isElements(this, other, NON_POS, NON_ZERO))
            return TOP;

        // NON_NEG lub NEG  → TOP
        if (isElements(this, other, NON_NEG, NEG))
            return TOP;

        // NON_POS lub POS  → TOP
        if (isElements(this, other, NON_POS, POS))
            return TOP;

        // NON_ZERO lub ZERO → TOP
        if (isElements(this, other, NON_ZERO, ZERO))
            return TOP;

        // POS lub NON_NEG  → NON_NEG  (POS is already below NON_NEG)
        if (isElements(this, other, POS, NON_NEG))
            return NON_NEG;

        // ZERO lub NON_NEG → NON_NEG
        if (isElements(this, other, ZERO, NON_NEG))
            return NON_NEG;

        // NEG lub NON_POS  → NON_POS
        if (isElements(this, other, NEG, NON_POS))
            return NON_POS;

        // ZERO lub NON_POS → NON_POS
        if (isElements(this, other, ZERO, NON_POS))
            return NON_POS;

        // POS lub NON_ZERO → NON_ZERO
        if (isElements(this, other, POS, NON_ZERO))
            return NON_ZERO;

        // NEG lub NON_ZERO → NON_ZERO
        if (isElements(this, other, NEG, NON_ZERO))
            return NON_ZERO;

        // Any remaining combination goes to TOP (safe over-approximation)
        return TOP;
    }

    /**
     * Less-or-equal relation according to the Hasse diagram.
     *
     * {@code this <= other} means "this is more precise than or equal to other".
     * Called only when neither is TOP/BOTTOM.
     */
    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        // POS <= NON_NEG, POS <= NON_ZERO
        if (this.equals(POS))
            return other.equals(NON_NEG) || other.equals(NON_ZERO);

        // NEG <= NON_POS, NEG <= NON_ZERO
        if (this.equals(NEG))
            return other.equals(NON_POS) || other.equals(NON_ZERO);

        // ZERO <= NON_NEG, ZERO <= NON_POS
        if (this.equals(ZERO))
            return other.equals(NON_NEG) || other.equals(NON_POS);

        // NON_NEG, NON_POS, NON_ZERO are directly below TOP (handled by BaseLattice)
        return false;
    }

    // -----------------------------------------------------------------------
    // Satisfiability helpers (used by the domain for assume / satisfy)
    // -----------------------------------------------------------------------

    /**
     * Returns whether this element is definitely equal to zero.
     */
    public boolean isDefinitelyZero() {
        return this.equals(ZERO);
    }

    /**
     * Returns whether this element can possibly be zero.
     */
    public boolean canBeZero() {
        return this.equals(ZERO)
                || this.equals(NON_NEG)
                || this.equals(NON_POS)
                || this.equals(TOP);
    }

    /**
     * Returns whether this element is definitely positive (strictly > 0).
     */
    public boolean isDefinitelyPositive() {
        return this.equals(POS);
    }

    /**
     * Returns whether this element can possibly be positive (strictly > 0).
     */
    public boolean canBePositive() {
        return this.equals(POS)
                || this.equals(NON_NEG)
                || this.equals(NON_ZERO)
                || this.equals(TOP);
    }

    /**
     * Returns whether this element is definitely negative (strictly < 0).
     */
    public boolean isDefinitelyNegative() {
        return this.equals(NEG);
    }

    /**
     * Returns whether this element can possibly be negative (strictly < 0).
     */
    public boolean canBeNegative() {
        return this.equals(NEG)
                || this.equals(NON_POS)
                || this.equals(NON_ZERO)
                || this.equals(TOP);
    }

    /**
     * Satisfiability of {@code this == other}.
     */
    public Satisfiability eq(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        // Both are ZERO: definitely equal
        if (this.equals(ZERO) && other.equals(ZERO))
            return Satisfiability.SATISFIED;
        // If neither can overlap
        if (!canOverlap(this, other))
            return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    /**
     * Satisfiability of {@code this > other}.
     *
     * Cases covered:
     *
     *  SATISFIED (all values in `this` are strictly greater than all values in `other`):
     *    POS     > NEG      ✓  (every x>0 is > every y<0)
     *    POS     > ZERO     ✓  (every x>0 is > 0)
     *    POS     > NON_POS  ✓  (every x>0 is > every y<=0)
     *    NON_NEG > NEG      ✓  (every x>=0 is > every y<0)
     *    ZERO    > NEG      ✓  (0 > every y<0)
     *
     *  NOT_SATISFIED (no value in `this` can be strictly greater than any value in `other`):
     *    NEG     > ZERO     ✗  (every x<0 is not > 0)
     *    NEG     > POS      ✗
     *    NEG     > NON_NEG  ✗  (every x<0 is not > any y>=0)
     *    ZERO    > ZERO     ✗  (0 is not > 0)
     *    ZERO    > POS      ✗
     *    ZERO    > NON_NEG  ✗  (0 is not > any y>=0, since 0>=0)
     *    NON_POS > POS      ✗  (every x<=0 is not > any y>0)
     *
     *  Everything else → UNKNOWN (the sets overlap or the relation holds for some but not all)
     */
    public Satisfiability gt(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;

        // ── SATISFIED cases ──────────────────────────────────────────────
        // POS > NEG, POS > ZERO, POS > NON_POS  (all values > 0 beat ≤ 0)
        if (this.equals(POS) && (other.equals(NEG) || other.equals(ZERO) || other.equals(NON_POS)))
            return Satisfiability.SATISFIED;
        // ZERO > NEG  (0 is strictly greater than any negative)
        if (this.equals(ZERO) && other.equals(NEG))
            return Satisfiability.SATISFIED;
        // NON_NEG > NEG  (every value ≥ 0 is strictly greater than any value < 0)
        if (this.equals(NON_NEG) && other.equals(NEG))
            return Satisfiability.SATISFIED;

        // ── NOT_SATISFIED cases ──────────────────────────────────────────
        // NEG > anything non-negative (ZERO, POS, NON_NEG)
        if (this.equals(NEG) && (other.equals(ZERO) || other.equals(POS) || other.equals(NON_NEG)))
            return Satisfiability.NOT_SATISFIED;
        // ZERO > ZERO, ZERO > POS, ZERO > NON_NEG (0 is not > any value ≥ 0)
        if (this.equals(ZERO) && (other.equals(ZERO) || other.equals(POS) || other.equals(NON_NEG)))
            return Satisfiability.NOT_SATISFIED;
        // NON_POS > POS  (every value ≤ 0 is never > any strictly positive value)
        if (this.equals(NON_POS) && other.equals(POS))
            return Satisfiability.NOT_SATISFIED;

        return Satisfiability.UNKNOWN;
    }

    // -----------------------------------------------------------------------
    // Representation
    // -----------------------------------------------------------------------

    @Override
    public StructuredRepresentation representation() {
        if (this.equals(BOTTOM))   return Lattice.bottomRepresentation();
        if (this.equals(TOP))      return Lattice.topRepresentation();
        if (this.equals(POS))      return new StringRepresentation("+");
        if (this.equals(NEG))      return new StringRepresentation("-");
        if (this.equals(ZERO))     return new StringRepresentation("0");
        if (this.equals(NON_NEG))  return new StringRepresentation(">=0");
        if (this.equals(NON_POS))  return new StringRepresentation("<=0");
        if (this.equals(NON_ZERO)) return new StringRepresentation("!=0");
        return Lattice.topRepresentation();
    }

    // -----------------------------------------------------------------------
    // equals / hashCode
    // -----------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return element == ((ExtendedSignLattice) obj).element;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static boolean isPosAndZero(ExtendedSignLattice a, ExtendedSignLattice b) {
        return (a.equals(POS) && b.equals(ZERO)) || (a.equals(ZERO) && b.equals(POS));
    }

    private static boolean isNegAndZero(ExtendedSignLattice a, ExtendedSignLattice b) {
        return (a.equals(NEG) && b.equals(ZERO)) || (a.equals(ZERO) && b.equals(NEG));
    }

    private static boolean isPosAndNeg(ExtendedSignLattice a, ExtendedSignLattice b) {
        return (a.equals(POS) && b.equals(NEG)) || (a.equals(NEG) && b.equals(POS));
    }

    private static boolean isElements(ExtendedSignLattice a, ExtendedSignLattice b,
                                      ExtendedSignLattice x, ExtendedSignLattice y) {
        return (a.equals(x) && b.equals(y)) || (a.equals(y) && b.equals(x));
    }

    /**
     * Returns true if the two elements share at least one integer value.
     * Used to determine satisfiability of equality.
     */
    private static boolean canOverlap(ExtendedSignLattice a, ExtendedSignLattice b) {
        // Elements that contain Z>0 : POS, NON_NEG, NON_ZERO, TOP
        // Elements that contain Z=0 : ZERO, NON_NEG, NON_POS, TOP
        // Elements that contain Z<0 : NEG, NON_POS, NON_ZERO, TOP
        if (a.canBePositive() && b.canBePositive()) return true;
        if (a.canBeZero() && b.canBeZero())         return true;
        if (a.canBeNegative() && b.canBeNegative()) return true;
        return false;
    }
}