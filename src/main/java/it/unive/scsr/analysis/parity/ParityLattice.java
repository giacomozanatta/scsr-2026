package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

/**
 * @author Mattia Acquilesi 896827
 * @author Alan Dal Col 895879
 */
public class ParityLattice implements BaseLattice<ParityLattice> {

    private final int element;

    public static final ParityLattice TOP    = new ParityLattice(0);
    public static final ParityLattice EVEN   = new ParityLattice(1);
    public static final ParityLattice ODD    = new ParityLattice(2);
    public static final ParityLattice BOTTOM = new ParityLattice(3);

    public ParityLattice(int e) {
        this.element = e;
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
    public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
        return false;
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        return TOP;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParityLattice that = (ParityLattice) o;
        return element == that.element;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM) return Lattice.bottomRepresentation();
        if (this == TOP)    return Lattice.topRepresentation();
        if (this == EVEN)   return new StringRepresentation("EVEN");
        return new StringRepresentation("ODD");
    }
}