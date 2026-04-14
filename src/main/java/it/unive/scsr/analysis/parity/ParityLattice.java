package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {
    private final String parity;

    public static ParityLattice TOP = new ParityLattice("TOP");
    public static ParityLattice BOTTOM = new ParityLattice("BOTTOM");
    public static ParityLattice EVEN = new ParityLattice("EVEN");
    public static ParityLattice ODD = new ParityLattice("ODD");

    public ParityLattice() {
        this.parity = "TOP";
    }

    public ParityLattice(String parity) {
        this.parity = parity;
    }

    @Override
    public int hashCode() {
        return parity.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        ParityLattice other = (ParityLattice) obj;
        return Objects.equals(this.parity, other.parity);
    }

    @Override
    public ParityLattice top() {
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice bottom() {
        return ParityLattice.BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        return new StringRepresentation(parity);
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        return ParityLattice.TOP;
    }

    @Override
    public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
        return false;
    }

    // almost copying SignLattice
    // For glb in this case we use default LiSA implementation
    // Other method implementations to use in Parity domain to assume and check satisfability of some stuff
    public Satisfiability eq(
            ParityLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        else if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        else if (!this.equals(other))
            return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    /**
     * Tests if this instance is greater than the given one, returning a
     * {@link Satisfiability} element.
     *
     * @param other the instance
     * @return the satisfiability of {@code this > other}
     */
    public Satisfiability gt(
            ParityLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        return Satisfiability.UNKNOWN; // depends on actual values, an even number can be bigger than an odd number and vice versa
    }
}