package it.unive.scsr.analysis.uninitialized;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class UninitializedLattice implements BaseLattice<UninitializedLattice> {

    public static final UninitializedLattice TOP = new UninitializedLattice(0);
    public static final UninitializedLattice UNINITIALIZED = new UninitializedLattice(1);
    public static final UninitializedLattice INITIALIZED = new UninitializedLattice(2);
    public static final UninitializedLattice BOTTOM = new UninitializedLattice(3);

    private final Integer lvl;

    public UninitializedLattice(Integer lvl) {
        this.lvl = lvl;
    }

    public UninitializedLattice(UninitializedLattice lattice) {
        this.lvl = lattice.lvl;
    }

    public UninitializedLattice() {
        this.lvl = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        UninitializedLattice other = (UninitializedLattice) obj;
        return Objects.equals(this.lvl, other.lvl);
    }

    @Override
    public UninitializedLattice lubAux(UninitializedLattice other) throws SemanticException {
        if (this.equals(other))
            return this;
        if (this == BOTTOM)
            return other;
        if (other == BOTTOM)
            return this;
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(UninitializedLattice other) throws SemanticException {
        if (this.equals(other))
            return true;
        if (this == BOTTOM)
            return true;
        if (other == TOP)
            return true;
        return false;
    }

    @Override
    public UninitializedLattice top() {
        return TOP;
    }

    @Override
    public UninitializedLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM) return new StringRepresentation("B");
        if (this == TOP) return new StringRepresentation("T");
        if (this == INITIALIZED) return new StringRepresentation("I");
        if (this == UNINITIALIZED) return new StringRepresentation("U");
        return new StringRepresentation("");
    }

    @Override
    public int hashCode() {
        return lvl.hashCode();
    }
}