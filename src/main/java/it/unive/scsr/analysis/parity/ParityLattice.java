package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;

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

	public boolean equals(Object obj) {
		if (obj == null) {

		}
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
		return null;
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		return null;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		return false;
	}

	// TODO add missing components
}