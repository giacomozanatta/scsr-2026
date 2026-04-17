package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;

import java.util.Objects;

//Work done by Elia Stevanato 895598 and Francesco Pasqualato 897778
public class ParityLattice implements BaseLattice<ParityLattice> {

	private int element;

	public static ParityLattice TOP = new ParityLattice(0);
	public static ParityLattice EVEN = new ParityLattice(1);
	public static ParityLattice ODD = new ParityLattice(2);
	public static ParityLattice BOTTOM = new ParityLattice(3);


	public ParityLattice(int i){
		this.element = i;
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
		if (this == ParityLattice.BOTTOM) return Lattice.bottomRepresentation();
		else if (this == ParityLattice.TOP) return Lattice.topRepresentation();
		else if (this == ParityLattice.EVEN) return new StringRepresentation("EVEN");
		return new StringRepresentation("ODD");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		if(this == BOTTOM) return other;
		if(other == BOTTOM) return this;
		if(this == other) return this;
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		if (this == BOTTOM)
			return true;
		if (other == TOP)
			return true;
		if (this == other)
			return true;
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