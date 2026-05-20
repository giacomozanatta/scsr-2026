package it.unive.scsr.analysis.nullability;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Lattice of Nullability Domain
 *
 *     MaybeNull (Top)
 *      /       \
 *   Null      NonNull
 *      \       /
 *       Bottom
 */
public class NullabilityLattice implements it.unive.lisa.analysis.BaseLattice<NullabilityLattice> {

    public static final NullabilityLattice TOP      = new NullabilityLattice(State.MAYBE_NULL);
    public static final NullabilityLattice NULL     = new NullabilityLattice(State.NULL);
    public static final NullabilityLattice NON_NULL = new NullabilityLattice(State.NON_NULL);
    public static final NullabilityLattice BOTTOM   = new NullabilityLattice(State.BOTTOM);

    private enum State { MAYBE_NULL, NULL, NON_NULL, BOTTOM }

    private final State state;

    public NullabilityLattice(State state) {
        this.state = state;
    }

    @Override
    public NullabilityLattice top() { return TOP; }

    @Override
    public NullabilityLattice bottom() { return BOTTOM; }

    @Override
    public boolean isTop() { return this.state == State.MAYBE_NULL; }

    @Override
    public boolean isBottom() { return this.state == State.BOTTOM; }

    public boolean isNull()    { return this.state == State.NULL; }
    public boolean isNonNull() { return this.state == State.NON_NULL; }

    // definitely null
    public boolean isAlwaysNull() { return this.state == State.NULL; }

    // null or maybe null
    public boolean isPossiblyNull() {
        return this.state == State.NULL || this.state == State.MAYBE_NULL;
    }

    @Override
    public NullabilityLattice lubAux(NullabilityLattice other) throws SemanticException {
        // NULL lub NON_NULL = MAYBE_NULL (Top)
        // anything lub itself = itself
        if (this.state == other.state) return this;
        return TOP;
    }

    @Override
    public NullabilityLattice glbAux(NullabilityLattice other) throws SemanticException {
        if (this.state == other.state) return this;
        return BOTTOM;
    }

    @Override
    public boolean lessOrEqualAux(NullabilityLattice other) throws SemanticException {
        // BOTTOM <= everything, everything <= TOP handled by framework
        // NULL <= MAYBE_NULL, NON_NULL <= MAYBE_NULL
        if (other.isTop()) return true;
        return this.state == other.state;
    }

    @Override
    public StructuredRepresentation representation() {
        switch (state) {
            case MAYBE_NULL: return Lattice.topRepresentation();
            case BOTTOM:     return Lattice.bottomRepresentation();
            case NULL:       return new StringRepresentation("NULL");
            case NON_NULL:   return new StringRepresentation("NON_NULL");
            default:         return new StringRepresentation("?");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NullabilityLattice)) return false;
        return this.state == ((NullabilityLattice) obj).state;
    }

    @Override
    public int hashCode() { return state.hashCode(); }
}