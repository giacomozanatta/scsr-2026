package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ParityLattice implements BaseLattice<ParityLattice> {

	private final int parity;

	public static ParityLattice TOP = new ParityLattice(0);
	public static ParityLattice ODD = new ParityLattice(1);
	public static ParityLattice EVEN = new ParityLattice(2);
	public static ParityLattice BOTTOM = new ParityLattice(3);

	private ParityLattice(int p) { parity = p; }

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
		if(this == ParityLattice.TOP)
			return Lattice.topRepresentation();
		if(this == ParityLattice.ODD)
			return new StringRepresentation("odd");
		if(this == ParityLattice.EVEN)
			return new StringRepresentation("even");

		return Lattice.bottomRepresentation();
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return false;
	}
}