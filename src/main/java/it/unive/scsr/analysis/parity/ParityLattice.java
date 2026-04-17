package it.unive.scsr.analysis.parity;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    private final int id;

    public static final ParityLattice TOP = new ParityLattice(0);
    public static final ParityLattice BOTTOM = new ParityLattice(1);
    public static final ParityLattice EVEN = new ParityLattice(2);
    public static final ParityLattice ODD = new ParityLattice(3);

    public ParityLattice(int id) {
        this.id = id;
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
        if (this == TOP)
            return Lattice.topRepresentation();
        if (this == BOTTOM)
            return Lattice.bottomRepresentation();
        if (this == EVEN)
            return new StringRepresentation("EVEN");

        return new StringRepresentation("ODD");
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ParityLattice other = (ParityLattice) obj;
        return id == other.id;
    }
}