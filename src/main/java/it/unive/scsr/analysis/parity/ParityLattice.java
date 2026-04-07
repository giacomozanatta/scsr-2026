package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    private static ParityLattice EVEN = new ParityLattice("EVEN");
    private static ParityLattice ODD = new ParityLattice("ODD");
    private static ParityLattice TOP = new ParityLattice("TOP");
    private static ParityLattice BOTTOM = new ParityLattice("BOTTOM");

    private final String parity;

    public ParityLattice(String parity) {
        this.parity = parity;
    }

    public ParityLattice(){
        this("TOP");
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

    public String getParity() {
        return parity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParityLattice that = (ParityLattice) o;
        return Objects.equals(parity, that.parity);
    }

    @Override
    public int hashCode() {
        return parity.hashCode();
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
        if(this == TOP)
            return Lattice.topRepresentation();
        if(this == BOTTOM)
            return Lattice.bottomRepresentation();
        if(this == EVEN)
            return new StringRepresentation("EVEN");
        return new StringRepresentation("ODD");
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        if(this.isTop() || other.isTop() || (this == EVEN && other == ODD) || (this == ODD && other == EVEN))
            return TOP;
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
        
        return this.equals(other);
    }


}
