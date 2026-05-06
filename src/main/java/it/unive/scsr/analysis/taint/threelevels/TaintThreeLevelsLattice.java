package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Lattice of  taint with three levels
 *	 Top 
 * 	/	\
 * C	 T	 
 *  \	/
 *  BOTTOM
 * 
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

	private final int element;

	public static TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(3);
	public static TaintThreeLevelsLattice TAINTED = new TaintThreeLevelsLattice(2);
	public static TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(1);
	public static TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(0);

	public TaintThreeLevelsLattice(int e) {this.element = e;}


	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if(this == other || other == BOTTOM)
			return this;

		if(this == BOTTOM)
			return other;

		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if(this == other || other == TOP ||this == BOTTOM)
			return true;

		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == TOP) return Lattice.topRepresentation();
		if (this == BOTTOM) return Lattice.bottomRepresentation();
		if (this == TAINTED) return new StringRepresentation("T");
		return new StringRepresentation("C");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TAINTED;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if(this == BOTTOM)
			return other;
		if (other == BOTTOM)
			return this;
		if(this == TOP || other == TOP)
			return	TOP;

		if (this == TAINTED || other == TAINTED)
			return TAINTED;

		return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINTED;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TOP;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaintThreeLevelsLattice that = (TaintThreeLevelsLattice) o;
		return this.element == that.element;
	}

	@Override
	public int hashCode() {
		return element;
	}

}
