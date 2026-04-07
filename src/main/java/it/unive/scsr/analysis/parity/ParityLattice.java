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

	// The lattice is:
	//        T
	//      /   \
	//    EVEN   ODD
	//      \   /
	//        ⊥

	private int element;

	// declaration of lattice elements
	// 0, 1, 2, 3, 4 are just an encoding
	public static ParityLattice TOP = new ParityLattice(0); // any
	public static ParityLattice EVEN = new ParityLattice(1);
	public static ParityLattice ODD = new ParityLattice(2);
	public static ParityLattice BOTTOM = new ParityLattice(3); // no value (error state)

	public ParityLattice(int e) {
		element = e;
	}

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
	public StructuredRepresentation representation() {
		// TODO: homework
		if(this == ParityLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if(this == ParityLattice.TOP)
			return Lattice.topRepresentation();
		else if(this == ParityLattice.EVEN)
			return new StringRepresentation("E");
		return new StringRepresentation("O");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		// lub(x,x) = x; lub(⊥,x) = x; lub(x, T) = T already handled by LiSA
		// remaining case : lub(E,O) = lub(O,E) = T
		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		// if I understand correctly, LiSA already handles ⊥ ⊑ anything and anything ⊑ T + reflexivity
		// so we're left with the ordering of EVEN vs ODD, which are incomparable so we just return false
		return false;
	}

	// TODO add missing components

	public Satisfiability eq(ParityLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM; // propagate error state

		else if (this.isTop() || other.isTop()) // we don't know the actual value, could be equal or not
			return Satisfiability.UNKNOWN;

		else if (!this.equals(other)) // both are middle elements of the lattice (E,O) and they're different parity
			return Satisfiability.NOT_SATISFIED; // definitely not equal

		// Both are same parity : we can't conclude if values are equal or not since we don't know the actual values
		return Satisfiability.UNKNOWN;
	}
}