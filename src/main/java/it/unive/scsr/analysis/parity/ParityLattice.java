package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	public static final ParityLattice TOP = new ParityLattice("TOP");
	public static final ParityLattice EVEN = new ParityLattice("EVEN");
	public static final ParityLattice ODD = new ParityLattice("ODD");
	public static final ParityLattice BOTTOM = new ParityLattice("BOTTOM");
	private final String parity;

	public ParityLattice() {this("TOP");}

	public ParityLattice(String parity) {this.parity = parity;}

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
		if(this.equals(TOP)) return Lattice.topRepresentation();
		if(this.equals(BOTTOM)) return Lattice.bottomRepresentation();
		return new StringRepresentation(parity);
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		return this.equals(other) ? this : TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return this.equals(other);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (getClass() != o.getClass()) return false;
		ParityLattice ob = (ParityLattice) o;
		return parity.equals(ob.parity);
	}

	@Override
	public int hashCode() {
		return parity.hashCode();
	}


}