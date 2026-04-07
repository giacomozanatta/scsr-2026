package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    private final String parity;

    private static ParityLattice EVEN = new ParityLattice("EVEN");
    private static ParityLattice ODD = new ParityLattice("ODD");
    private static ParityLattice TOP = new ParityLattice("TOP");
    private static ParityLattice BOTTOM = new ParityLattice("BOTTOM");

    public ParityLattice(String parity) {
        this.parity = parity;
    }

    @Override
    public ParityLattice top() {
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice bottom() {
        return ParityLattice.BOTTOM;
    }

    public static ParityLattice getEVEN() {
        return EVEN;
    }

    public static ParityLattice getODD() {
        return ODD;
    }

    public static ParityLattice getTOP() {
        return TOP;
    }

    public static ParityLattice getBOTTOM() {
        return BOTTOM;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ParityLattice that = (ParityLattice) o;
        return Objects.equals(parity, that.parity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parity);
    }

    @Override
    public StructuredRepresentation representation() {
       if(this == TOP)
           return Lattice.topRepresentation();
       else if(this == BOTTOM)
           return Lattice.bottomRepresentation();
       else if(this == EVEN)
           return new StringRepresentation("EVEN");

       return new StringRepresentation("ODD");
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        if(this.isTop() || other.isTop() || (this == EVEN && other == ODD) || (this == ODD && other == EVEN))
            return ParityLattice.TOP;
        else if(this.equals(other))
            return this;
        else if(this.isBottom())
            return other;
        else if(other.isBottom())
            return this;
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
        return false;
    }

    // TODO add missing components
}