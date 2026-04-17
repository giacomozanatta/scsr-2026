package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	private int element;

	public static ParityLattice TOP = new ParityLattice(0);
	public static ParityLattice EVEN = new ParityLattice(1);
	public static ParityLattice ODD = new ParityLattice(2);
	public static ParityLattice BOTTOM = new ParityLattice(3);

	/**
	 * Constructor for creating a ParityLattice element
	 *
	 * @param element encoding: 0=TOP, 1=EVEN, 2=ODD, 3=BOTTOM
	 */
	public ParityLattice(int element) {
		this.element = element;
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
		if (this == ParityLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if (this == ParityLattice.TOP)
			return Lattice.topRepresentation();
		else if (this == ParityLattice.EVEN)
			return new StringRepresentation("EVEN");
		else
			return new StringRepresentation("ODD");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		if(this == null)
			return ParityLattice.BOTTOM;

		// BOTTOM is identity for join (least informative)
		if (this == ParityLattice.BOTTOM)
			return other;
		if (other == ParityLattice.BOTTOM)
			return this;

		// TOP absorbs everything
		if (this == ParityLattice.TOP || other == ParityLattice.TOP)
			return ParityLattice.TOP;

		// If we reach here: {this, other} = {EVEN, ODD}
		// Joining different parities gives TOP (unknown)
		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {

		if(this == null)
			return false;

		// BOTTOM is less than or equal to everything
		if (this == ParityLattice.BOTTOM)
			return true;

		// TOP is only less than or equal to itself
		if (this == ParityLattice.TOP)
			return false;

		// EVEN and ODD are only less than TOP
		if (other == ParityLattice.TOP)
			return true;

		// EVEN/ODD not comparable with different parities
		return false;
	}

	/**
	 * Checks if this lattice element represents even parity
	 */
	public boolean isEven() {
		return this == ParityLattice.EVEN;
	}

	/**
	 * Checks if this lattice element represents odd parity
	 */
	public boolean isOdd() {
		return this == ParityLattice.ODD;
	}

	/**
	 * Checks if this lattice element is the top element (unknown parity)
	 */
	public boolean isTop() {
		return this == ParityLattice.TOP;
	}

	/**
	 * Checks if this lattice element is the bottom element (no information)
	 */
	public boolean isBottom() {
		return this == ParityLattice.BOTTOM;
	}

	@Override
	public int hashCode() {
		return Objects.hash(element);
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
		return element == other.element;
	}

	@Override
	public String toString() {
		if (this == ParityLattice.BOTTOM)
			return "⊥";
		else if (this == ParityLattice.TOP)
			return "⊤";
		else if (this == ParityLattice.EVEN)
			return "EVEN";
		else
			return "ODD";
	}
}