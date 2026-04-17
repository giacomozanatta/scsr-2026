package it.unive.scsr.analysis.parity;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;


//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	public static final ParityLattice EVEN = new ParityLattice(0);
	public static final ParityLattice ODD = new ParityLattice(1);
	public static final ParityLattice TOP = new ParityLattice(2);
	public static final ParityLattice BOTTOM = new ParityLattice(3);

	public final Integer parity;

	public ParityLattice(Integer parity) {
		this.parity = parity;
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
		if(this == ParityLattice.TOP)
			return Lattice.topRepresentation();
		else if(this == ParityLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if(this == ParityLattice.EVEN)
			return new StringRepresentation("EVEN");
		else
			return new StringRepresentation("ODD");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {

		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		
		return false;
	}


	@Override
	public int hashCode() {
		return Objects.hash(this.parity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null) 
			return false;
		if(obj instanceof ParityLattice){
			ParityLattice other = (ParityLattice) obj;

			return Objects.equals(this.parity, other.parity);
		}	
		return false;
	}
}