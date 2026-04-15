package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;


public class ParityLattice implements BaseLattice<ParityLattice> {

	public static final ParityLattice even = new ParityLattice("EVEN");
    public static final ParityLattice odd = new ParityLattice("ODD");
    public static final ParityLattice top = new ParityLattice("TOP");
    public static final ParityLattice bottom = new ParityLattice("BOTTOM");
	private final String parity;

	public ParityLattice() {this("TOP");}

	public ParityLattice(String parity) {this.parity = parity;}

	@Override
	public ParityLattice top() {return top;}

	@Override
	public ParityLattice bottom() {return bottom;}

	public String getParity() {return parity;}

	@Override
	public StructuredRepresentation representation() {
		if (this.equals(top)) return Lattice.topRepresentation();
		if (this.equals(bottom)) return Lattice.bottomRepresentation();
		return new StringRepresentation(this.parity);
	}


	// Least upper bound is TOP
	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {return new ParityLattice("TOP");}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {return false;}


	 @Override
		public int hashCode() {return parity.hashCode();}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (getClass() != o.getClass()) return false;
		ParityLattice ob = (ParityLattice) o;
		return parity.equals(ob.parity);
	}

}