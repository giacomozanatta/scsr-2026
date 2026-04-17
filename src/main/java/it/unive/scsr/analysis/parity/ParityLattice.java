package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ParityLattice implements BaseLattice<ParityLattice> {

	private final int element;

	public static final ParityLattice TOP = new ParityLattice(0);
	public static final ParityLattice EVEN = new ParityLattice(1);
	public static final ParityLattice ODD = new ParityLattice(2);
	public static final ParityLattice BOTTOM = new ParityLattice(3);

	public ParityLattice(int element) {
		this.element = element;
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
		if (this == BOTTOM)
			return Lattice.bottomRepresentation();
		if (this == TOP)
			return Lattice.topRepresentation();
		if (this == EVEN)
			return new StringRepresentation("EVEN");
		return new StringRepresentation("ODD");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		if (this == other)
			return this;
		if (this == BOTTOM)
			return other;
		if (other == BOTTOM)
			return this;
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return this == other || this == BOTTOM || other == TOP;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(element);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		ParityLattice other = (ParityLattice) obj;
		return element == other.element;
	}
}
