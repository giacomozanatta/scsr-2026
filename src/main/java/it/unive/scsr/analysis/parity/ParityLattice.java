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
	public static final ParityLattice TOP = new ParityLattice(0);
	public static final ParityLattice BOTTOM = new ParityLattice(1);
	public static final ParityLattice EVEN = new ParityLattice(2);
	public static final ParityLattice ODD = new ParityLattice(3);



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
	public StructuredRepresentation representation() {
		// TODO: homework
			if(this == ParityLattice.BOTTOM)
				return Lattice.bottomRepresentation();
			else if(this == ParityLattice.TOP)
				return Lattice.topRepresentation();
			else if(this == ParityLattice.ODD)
				return new StringRepresentation("Odd");
			else if(this == ParityLattice.EVEN) 
				return new StringRepresentation("Even");
			else
				return new StringRepresentation("None");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// since EVEN ⊔ ODD = TOP, we return TOP
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		//EVEN and ODD are incomparable
		return false;
	}

	// TODO add missing components
		public ParityLattice(int e){
		element = e;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		return this.element == ((ParityLattice) obj).element;
	}

	@Override
	public int hashCode() {
		return Objects.hash(element);
	}

}