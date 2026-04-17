package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	private int element;

	// declaration of lattice elements
	// 0, 1, 2, 3, 4 are just an encoding
	public static ParityLattice TOP = new ParityLattice(0);
	public static ParityLattice ODD = new ParityLattice(1);
	public static ParityLattice EVEN = new ParityLattice(2);
	public static ParityLattice BOTTOM = new ParityLattice(3);

	public ParityLattice(int e) {
		element = e;
	}

    @Override
	public ParityLattice top() {
		// DONE: homework
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		// DONE: homework
		return ParityLattice.BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		// DONE: homework
		if(this == ParityLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if(this == ParityLattice.TOP)
			return Lattice.topRepresentation();
		else if(this == ParityLattice.EVEN)
			return new StringRepresentation("even");
		else if(this == ParityLattice.ODD)
			return new StringRepresentation("odd");

		return Lattice.topRepresentation();
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// DONE: homework
		if (this == ParityLattice.BOTTOM)
			return other;
		if (other == ParityLattice.BOTTOM)
			return this;

		if (this == ParityLattice.TOP || other == ParityLattice.TOP)
			return TOP;

		if (this == other)
			return this;
		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// DONE: homework
		if (this == other)
			return true;

		if(this == ParityLattice.BOTTOM)
			return true;
		if(other == ParityLattice.TOP)
			return true;

		return false;
	}

	// TODO add missing components


	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		ParityLattice that = (ParityLattice) o;
		//TODO
		return element == that.element;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(element);
	}


}