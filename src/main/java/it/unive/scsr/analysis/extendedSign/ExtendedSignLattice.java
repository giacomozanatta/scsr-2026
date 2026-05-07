package it.unive.scsr.analysis.extendedSign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

/**
 * Extended Sign lattice with 8 elements:
 *
 *            Z (TOP)
 *          /  |    \
 *       Z≤0  Z≠0   Z≥0
 *        |  \ X  /  |
 *       Z<0  Z=0  Z>0
 *          \  |  /
 *            ∅ (BOTTOM)
 *
 * Concrete interpretations:
 *   LT0  (<0)  = { ..., -2, -1 }
 *   EQ0  (=0)  = { 0 }
 *   GT0  (>0)  = { 1, 2, ... }
 *   LEQ0 (≤0)  = { ..., -1, 0 }
 *   NEQ0 (≠0)  = { ..., -1, 1, 2, ... }
 *   GEQ0 (≥0)  = { 0, 1, 2, ... }
 *   TOP  (Z)   = all integers
 *   BOTTOM(∅)  = empty set
 */
public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    private final int element;

    // Encoding: 0=BOTTOM, 1=LT0, 2=EQ0, 3=GT0, 4=LEQ0, 5=NEQ0, 6=GEQ0, 7=TOP
    public static final ExtendedSignLattice BOTTOM = new ExtendedSignLattice(0);
    public static final ExtendedSignLattice LT0    = new ExtendedSignLattice(1); // <0
    public static final ExtendedSignLattice EQ0    = new ExtendedSignLattice(2); // =0
    public static final ExtendedSignLattice GT0    = new ExtendedSignLattice(3); // >0
    public static final ExtendedSignLattice LEQ0   = new ExtendedSignLattice(4); // ≤0
    public static final ExtendedSignLattice NEQ0   = new ExtendedSignLattice(5); // ≠0
    public static final ExtendedSignLattice GEQ0   = new ExtendedSignLattice(6); // ≥0
    public static final ExtendedSignLattice TOP    = new ExtendedSignLattice(7);

    public ExtendedSignLattice(int e) {
        this.element = e;
    }

    @Override
    public ExtendedSignLattice top() { return TOP; }

    @Override
    public ExtendedSignLattice bottom() { return BOTTOM; }

    // -------------------------------------------------------------------------
    // Lattice operations
    // -------------------------------------------------------------------------

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        // Called only when neither is TOP/BOTTOM and this != other.
// Use sorted pair (a <= b) to halve the case count.
        int a = Math.min(this.element, other.element);
        int b = Math.max(this.element, other.element);

        // LT0 (1) with others
        if (a == 1 && b == 2) return LEQ0;   // LT0 ∪ EQ0  = ≤0
        if (a == 1 && b == 3) return NEQ0;   // LT0 ∪ GT0  = ≠0
        if (a == 1 && b == 4) return LEQ0;   // LT0 ∪ LEQ0 = ≤0
        if (a == 1 && b == 5) return NEQ0;   // LT0 ∪ NEQ0 = ≠0
        if (a == 1 && b == 6) return TOP;    // LT0 ∪ GEQ0 = Z

        // EQ0 (2) with others
        if (a == 2 && b == 3) return GEQ0;   // EQ0 ∪ GT0  = ≥0
        if (a == 2 && b == 4) return LEQ0;   // EQ0 ∪ LEQ0 = ≤0
        if (a == 2 && b == 5) return TOP;    // EQ0 ∪ NEQ0 = Z
        if (a == 2 && b == 6) return GEQ0;   // EQ0 ∪ GEQ0 = ≥0

        // GT0 (3) with others
        if (a == 3 && b == 4) return TOP;    // GT0 ∪ LEQ0 = Z
        if (a == 3 && b == 5) return NEQ0;   // GT0 ∪ NEQ0 = ≠0
        if (a == 3 && b == 6) return GEQ0;   // GT0 ∪ GEQ0 = ≥0

        // Middle layer combinations: all produce TOP
        // LEQ0 ∪ NEQ0, LEQ0 ∪ GEQ0, NEQ0 ∪ GEQ0
        return TOP;
    }

    @Override
    public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
        // Called only when neither is TOP/BOTTOM and this != other.
        int a = Math.min(this.element, other.element);
        int b = Math.max(this.element, other.element);

        // LT0 (1) glb with others
        if (a == 1 && b == 4) return LT0;    // LT0 ∩ LEQ0 = <0  (LT0 ≤ LEQ0)
        if (a == 1 && b == 5) return LT0;    // LT0 ∩ NEQ0 = <0  (LT0 ≤ NEQ0)

        // EQ0 (2) glb with others
        if (a == 2 && b == 4) return EQ0;    // EQ0 ∩ LEQ0 = =0  (EQ0 ≤ LEQ0)
        if (a == 2 && b == 6) return EQ0;    // EQ0 ∩ GEQ0 = =0  (EQ0 ≤ GEQ0)

        // GT0 (3) glb with others
        if (a == 3 && b == 5) return GT0;    // GT0 ∩ NEQ0 = >0  (GT0 ≤ NEQ0)
        if (a == 3 && b == 6) return GT0;    // GT0 ∩ GEQ0 = >0  (GT0 ≤ GEQ0)

        // Middle layer glb
        if (a == 4 && b == 5) return LT0;    // LEQ0 ∩ NEQ0 = <0  ({≤0} ∩ {≠0} = {<0})
        if (a == 4 && b == 6) return EQ0;    // LEQ0 ∩ GEQ0 = =0  ({≤0} ∩ {≥0} = {0})
        if (a == 5 && b == 6) return GT0;    // NEQ0 ∩ GEQ0 = >0  ({≠0} ∩ {≥0} = {>0})

        return BOTTOM; // safe fallback
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this == BOTTOM)  return true;
        if (this == TOP)     return false;

        // LT0 is below LEQ0 and NEQ0
        if (this == LT0 && (other == LEQ0 || other == NEQ0)) return true;
        // EQ0 is below LEQ0 and GEQ0
        if (this == EQ0 && (other == LEQ0 || other == GEQ0)) return true;
        // GT0 is below NEQ0 and GEQ0
        if (this == GT0 && (other == NEQ0 || other == GEQ0)) return true;

        // Middle layer elements are incomparable with each other
        return false;
    }

    // -------------------------------------------------------------------------
    // Satisfiability helpers (used by ExtendedSign's assume/satisfies methods)
    // -------------------------------------------------------------------------

    /** Returns satisfiability of {@code this == other}. */
    public Satisfiability eq(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
        if (this.isTop()    || other.isTop())    return Satisfiability.UNKNOWN;

        // Disjoint concrete sets → definitely NOT equal
        if (this == GT0  && (other == LT0 || other == EQ0 || other == LEQ0)) return Satisfiability.NOT_SATISFIED;
        if (this == LT0  && (other == GT0 || other == EQ0 || other == GEQ0)) return Satisfiability.NOT_SATISFIED;
        if (this == EQ0  && (other == GT0 || other == LT0 || other == NEQ0)) return Satisfiability.NOT_SATISFIED;
        if (this == GEQ0 && other == LT0)  return Satisfiability.NOT_SATISFIED;
        if (this == LEQ0 && other == GT0)  return Satisfiability.NOT_SATISFIED;
        if (this == NEQ0 && other == EQ0)  return Satisfiability.NOT_SATISFIED;

        // The only case that is definitely equal: both are zero
        if (this == EQ0 && other == EQ0) return Satisfiability.SATISFIED;

        return Satisfiability.UNKNOWN;
    }

    /** Returns satisfiability of {@code this > other}. */
    public Satisfiability gt(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
        if (this.isTop()    || other.isTop())    return Satisfiability.UNKNOWN;

        // Definite NOT_SATISFIED
        if (this == LT0  && (other == EQ0 || other == GT0 || other == GEQ0)) return Satisfiability.NOT_SATISFIED;
        if (this == EQ0  && (other == EQ0 || other == GT0 || other == GEQ0)) return Satisfiability.NOT_SATISFIED;
        if (this == LEQ0 && other == GT0)  return Satisfiability.NOT_SATISFIED;
        if (this == LEQ0 && other == GEQ0) return Satisfiability.NOT_SATISFIED;

        // Definite SATISFIED
        if (this == GT0  && (other == LT0 || other == EQ0 || other == LEQ0)) return Satisfiability.SATISFIED;
        if (this == EQ0  && other == LT0)  return Satisfiability.SATISFIED;
        if (this == GEQ0 && other == LT0)  return Satisfiability.SATISFIED;

        return Satisfiability.UNKNOWN;
    }

    // -------------------------------------------------------------------------
    // Standard overrides
    // -------------------------------------------------------------------------

    @Override
    public int hashCode() { return Objects.hash(element); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return element == ((ExtendedSignLattice) obj).element;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM) return Lattice.bottomRepresentation();
        if (this == TOP)    return Lattice.topRepresentation();
        if (this == LT0)    return new StringRepresentation("<0");
        if (this == EQ0)    return new StringRepresentation("=0");
        if (this == GT0)    return new StringRepresentation(">0");
        if (this == LEQ0)   return new StringRepresentation("<=0");
        if (this == NEQ0)   return new StringRepresentation("!=0");
        /* GEQ0 */          return new StringRepresentation(">=0");
    }
}