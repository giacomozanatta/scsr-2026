package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	private final int element;

	public static ParityLattice TOP = new ParityLattice(0);
	public static ParityLattice ODD = new ParityLattice(1);
	public static ParityLattice EVEN = new ParityLattice(2);
	public static ParityLattice BOTTOM = new ParityLattice(3);

	public ParityLattice(int e) {
		element = e;
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
		if(this == ParityLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if(this == ParityLattice.TOP)
			return Lattice.topRepresentation();
		else if(this == ParityLattice.EVEN)
			return new StringRepresentation("Even");
		else
			return new StringRepresentation("Odd");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		/*
		// Base cases are handled by parent class
		if ( this.isTop() || other.isTop())
			return ParityLattice.TOP;
		else if (this.isBottom())
			return other;
		else if (other.isBottom())
			return this;
		else if (this == other)
			return this;
		 */
		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		/*
		// Base cases are handled by parent class
		if (this == other)
			return true;
		else if (this.isBottom())
			return true;
		else if (other.isTop())
			return true;
		 */

		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ParityLattice that = (ParityLattice) o;
		return element == that.element;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(element);
	}
}