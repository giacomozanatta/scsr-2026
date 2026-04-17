package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/**
 * Lattice of parity abstractions: even, odd, top, and bottom.
 */
public class ParityLattice implements BaseLattice<ParityLattice> {

	public static final ParityLattice EVEN = new ParityLattice((byte) 3);

	public static final ParityLattice ODD = new ParityLattice((byte) 2);

	public static final ParityLattice TOP = new ParityLattice((byte) 0);

	public static final ParityLattice BOTTOM = new ParityLattice((byte) 1);

	private final byte parity;

	public ParityLattice() {
		this((byte) 0);
	}

	private ParityLattice(byte parity) {
		this.parity = parity;
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
		if (isBottom())
			return Lattice.bottomRepresentation();
		if (isTop())
			return Lattice.topRepresentation();

		String repr = this == EVEN ? "Even" : "Odd";
		return new StringRepresentation(repr);
	}

	public boolean isEven() {
		return this == EVEN;
	}

	public boolean isOdd() {
		return this == ODD;
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + parity;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParityLattice other = (ParityLattice) obj;
		return parity == other.parity;
	}

	@Override
	public String toString() {
		if (isBottom())
			return "BOTTOM";
		if (isTop())
			return "TOP";
		return isEven() ? "EVEN" : "ODD";
	}
}
