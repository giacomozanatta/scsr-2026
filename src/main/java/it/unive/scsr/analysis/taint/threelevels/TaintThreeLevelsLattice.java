package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.SemanticException;
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
	private final byte state;

	public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice((byte) 3);
	public static final TaintThreeLevelsLattice TAINTED = new TaintThreeLevelsLattice((byte) 2);
	public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice((byte) 1);
	public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice((byte) 0);

	public TaintThreeLevelsLattice() {
		this((byte) 3); // top by default
	}

	private TaintThreeLevelsLattice(byte state) {
		this.state = state;
	}

	@Override
	public int hashCode() {
		return state;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;
		return state == other.state;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other || other == BOTTOM)
			return this;
		if (this == BOTTOM)
			return other;
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other || other == TOP || this == BOTTOM)
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
	public it.unive.lisa.util.representation.StructuredRepresentation representation() {
		if (this == BOTTOM)
			return it.unive.lisa.analysis.Lattice.bottomRepresentation();
		if (this == TOP)
			return it.unive.lisa.analysis.Lattice.topRepresentation();
		if (this == TAINTED)
			return new it.unive.lisa.util.representation.StringRepresentation("T");
		return new it.unive.lisa.util.representation.StringRepresentation("C");
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
		if (this == BOTTOM)
			return other;
		if (other == BOTTOM)
			return this;
		if (this == TAINTED || other == TAINTED)
			return TAINTED;
		if (this == TOP || other == TOP)
			return TOP;
		return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINTED;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TAINTED || this == TOP;
	}

}
