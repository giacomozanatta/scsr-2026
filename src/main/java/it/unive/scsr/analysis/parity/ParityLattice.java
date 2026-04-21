package it.unive.scsr.analysis.parity;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ParityLattice implements BaseLattice<ParityLattice> {

    private final int element;

    public static final ParityLattice TOP = new ParityLattice(0);
    public static final ParityLattice EVEN = new ParityLattice(1);
    public static final ParityLattice ODD = new ParityLattice(2);
    public static final ParityLattice BOTTOM = new ParityLattice(3);

    public ParityLattice() {
        this(0); // TOP by default
    }

    public ParityLattice(int element) {
        this.element = element;
    }

    @Override
    public ParityLattice top() {
        return TOP;
    }

    @Override
    public ParityLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM)
            return Lattice.bottomRepresentation();
        else if (this == TOP)
            return Lattice.topRepresentation();
        else if (this == EVEN)
            return new StringRepresentation("Even");
        return new StringRepresentation("Odd");
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParityLattice other = (ParityLattice) obj;
        return element == other.element;
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        return TOP;
    }

    // Explicitly implementing glb as requested
    public ParityLattice glb(ParityLattice other) throws SemanticException {
        if (this == BOTTOM || other == BOTTOM)
            return BOTTOM;
        if (this == TOP)
            return other;
        if (other == TOP)
            return this;
        if (this.equals(other))
            return this;
        // EVEN and ODD glb is BOTTOM
        return BOTTOM;
    }

    @Override
    public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
        return false;
    }
}