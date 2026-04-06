package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {
	private int elem;
	public static ParityLattice TOP = new ParityLattice(0);
	public static ParityLattice BOTTOM = new ParityLattice(1);
	public static ParityLattice EVEN = new ParityLattice(2);
	public static ParityLattice ODD = new ParityLattice(3);

	public ParityLattice(int e){ elem = e; }
	@Override
	public ParityLattice top() {
		// TODO: homework
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		// TODO: homework
		return ParityLattice.BOTTOM;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO: homework
		if(this == ParityLattice.TOP) return Lattice.topRepresentation();
		if(this == ParityLattice.BOTTOM) return Lattice.bottomRepresentation();
		if(this == ParityLattice.EVEN) return new StringRepresentation("EVEN");

		return new StringRepresentation("ODD");

	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		return false;
	}

	// TODO add missing components
}