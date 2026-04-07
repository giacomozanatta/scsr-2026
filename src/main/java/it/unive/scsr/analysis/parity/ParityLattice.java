package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    private final int element;

    public static final ParityLattice TOP = new ParityLattice(0);
    public static final ParityLattice EVEN = new ParityLattice(1);
    public static final ParityLattice ODD = new ParityLattice(2);
    public static final ParityLattice BOTTOM = new ParityLattice(3);

    public ParityLattice(int e) {
        element = e;
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
        if (this == ParityLattice.TOP) {
            return Lattice.topRepresentation();
        }  else if (this == ParityLattice.BOTTOM) {
            return Lattice.bottomRepresentation();
        } else if (this == ParityLattice.EVEN) {
            return new StringRepresentation("EVEN");
        } else if (this == ParityLattice.ODD) {
            return new StringRepresentation("ODD");
        }
        return new StringRepresentation("PARITY");
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        return top();
    }

    @Override
    public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
        return false;
    }

    public Satisfiability eq(ParityLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        if (!this.equals(other))
            return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(ParityLattice other) {
        return Satisfiability.UNKNOWN;
    }

}