package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Lattice of  taint with three levels
 *
 *        ⊤  (Top: might be Taint or Clean)
 *       / \
 *      T   C
 *       \ /
 *        ⊥  (Bottom: unreachable)
 *
 * Order: ⊥ <= T <= ⊤,  ⊥ <= C <= ⊤,  T and C are incomparable.
 *
 * Join (lub):
 *   ⊥ ⊔ x = x
 *   T ⊔ T = T
 *   C ⊔ C = C
 *   T ⊔ C = ⊤
 *   x ⊔ ⊤ = ⊤
 *
 * isAlwaysTainted()  -> true only for T
 * isPossiblyTainted() -> true for T and ⊤
 */

public class TaintThreeLevelsLattice
        implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

    // Definitely tainted
    public static final TaintThreeLevelsLattice TAINT  = new TaintThreeLevelsLattice(1);

    // Definitely clean
    public static final TaintThreeLevelsLattice CLEAN  = new TaintThreeLevelsLattice(2);

    // Top, might be Taint or Clean, join of T and C
    public static final TaintThreeLevelsLattice TOP    = new TaintThreeLevelsLattice(3);

    // Bottom, unreachable or no information
    public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(0);

    // 0=bottom, 1=taint, 2=clean, 3=top
    private final int level;

    private TaintThreeLevelsLattice(int level) {
        this.level = level;
    }

    @Override
    public TaintThreeLevelsLattice top() {
        return TOP;
    }

    @Override
    public TaintThreeLevelsLattice bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isTop() {
        return this == TOP;
    }

    @Override
    public boolean isBottom() {
        return this == BOTTOM;
    }

    // Called by the framework when neither {@code this} nor {@code other} is already bottom
    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        // T ⊔ T = T,  C ⊔ C = C,  T ⊔ C = ⊤,  C ⊔ T = ⊤
        if (this == other) {
            return this;
        }
        return TOP; // incomparable elements (T and C) -> join is ⊤
    }

    // Called by the framework when {@code this} is not bottom and {@code other} is not top
    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        // T <= T = true,  C <= C = true,  T <= C = false,  C <= T = false
        return this == other;
    }

    @Override
    public TaintThreeLevelsLattice tainted() {
        return TAINT;
    }

    @Override
    public TaintThreeLevelsLattice clean() {
        return CLEAN;
    }

    /**
     * Propagates taint across binary expressions (like x + y)
     * Rules:
     *   ⊥ or ⊥ -> ⊥  (expression is unreachable)
     *   T or _ (non-⊥) -> T  (at least one operand is definitely tainted)
     *   _ or T (non-⊥) -> T
     *   ⊤ or _ (non-⊥,non-T) -> ⊤  (might be tainted)
     *   _ or ⊤ (non-⊥,non-T) -> ⊤
     *   C or C -> C
     */
    @Override
    public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
        if (this == BOTTOM || other == BOTTOM) {
            return BOTTOM;
        }
        if (this == TAINT || other == TAINT) {
            return TAINT;
        }
        if (this == TOP || other == TOP) {
            return TOP;
        }
        return CLEAN;
    }

    @Override
    public boolean isAlwaysTainted() {
        return this == TAINT;
    }

    @Override
    public boolean isPossiblyTainted() {
        return this == TAINT || this == TOP;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM) {
            return Lattice.bottomRepresentation();
        }
        if (this == TOP) {
            return Lattice.topRepresentation();
        }
        if (this == TAINT) {
            return new StringRepresentation("T");
        }
        return new StringRepresentation("C");
    }

    @Override
    public String toString() {
        return representation().toString();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj; // because singleton
    }

    @Override
    public int hashCode() {
        return level;
    }
}