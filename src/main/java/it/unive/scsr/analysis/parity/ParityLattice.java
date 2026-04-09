package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	private final int INTERNAL_VALUE;

	public static ParityLattice TOP = new ParityLattice(100);
	public static ParityLattice BOTTOM = new ParityLattice(-100);
	public static ParityLattice EVEN = new ParityLattice(2);
	public static ParityLattice ODD = new ParityLattice(1);

	public ParityLattice(int i) {
		INTERNAL_VALUE = i;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.INTERNAL_VALUE);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (this == obj) { return true; }
		if (getClass() != obj.getClass()) { return false; }
        return this.INTERNAL_VALUE == ((ParityLattice) obj).INTERNAL_VALUE;
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
		if (this == ParityLattice.TOP) {
			return Lattice.topRepresentation();
		} else if (this == ParityLattice.BOTTOM) {
			return Lattice.bottomRepresentation();
		} else if (this == ParityLattice.EVEN) {
			return new StringRepresentation("EVEN");
		}
		return new StringRepresentation("ODD");
	}

	public Satisfiability eq(ParityLattice other) {
		if (this.isBottom() || other.isBottom()) {
			return Satisfiability.BOTTOM;
		}
		else if (this.isTop() || other.isTop()) {
			return Satisfiability.UNKNOWN;
		}
		else if (!this.equals(other)) {
			return Satisfiability.NOT_SATISFIED;
		}
		else {
			return Satisfiability.UNKNOWN;
		}
	}

	public Satisfiability gt(ParityLattice other){
		if (this.isBottom() || other.isBottom()) {
			return Satisfiability.BOTTOM;
		}
		return Satisfiability.UNKNOWN;
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// Since we are guaranteed conditions by lub in BaseLattice, the only case is
		//     when this and other are EVEN and ODD or vice versa - always TOP
		return ParityLattice.TOP;
	}

	// glbAux uses default implementation, as the cases are guaranteed again
	//     by the BaseLattice interface

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// From conditions guaranteed by the BaseLattice interface,
		//     we have that this always is run when this and other
		//     are EVEN and ODD, or vice versa - always false
		return false;
	}
}