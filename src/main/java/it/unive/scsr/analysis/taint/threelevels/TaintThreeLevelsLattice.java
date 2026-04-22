package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.parity.ParityLattice;
import it.unive.scsr.analysis.taint.TaintLattice;

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

	int element;

	static public TaintThreeLevelsLattice Tainted = new TaintThreeLevelsLattice(1);
	static public TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(0);
	static public TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(-1);
	static public TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(1); // Top is Tainted in three-level domain

	public TaintThreeLevelsLattice(Integer e) {
		this.element = e;
	}
	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// Bottom is absorbed by any element
		if (this == TaintThreeLevelsLattice.Bottom) {
			return other;
		}
		if (other == TaintThreeLevelsLattice.Bottom) {
			return this;
		}

		if (this == TaintThreeLevelsLattice.Top || other == TaintThreeLevelsLattice.Top) {
			return TaintThreeLevelsLattice.Top;
		}

		if (this == other)
			return this;

		return TaintThreeLevelsLattice.Top;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other)
			return true;

		if (this == TaintThreeLevelsLattice.Bottom)
			return true;

		if (this == TaintThreeLevelsLattice.Top)
			return false;

		if (other == TaintThreeLevelsLattice.Top)
			return true;

		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return TaintThreeLevelsLattice.Top;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return TaintThreeLevelsLattice.Bottom;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == TaintThreeLevelsLattice.Bottom)
			return Lattice.bottomRepresentation();

		if (this == TaintThreeLevelsLattice.Top)
			return Lattice.topRepresentation();

		if (this == TaintThreeLevelsLattice.Tainted)
			return new StringRepresentation("T");

		return new StringRepresentation("C");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.Tainted;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.Clean;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {

		if(this == TaintThreeLevelsLattice.Bottom || other == TaintThreeLevelsLattice.Bottom)
			return TaintThreeLevelsLattice.Bottom;

		if(this == TaintThreeLevelsLattice.Tainted || other == TaintThreeLevelsLattice.Tainted)
			return TaintThreeLevelsLattice.Tainted;

		if (this == TaintThreeLevelsLattice.Top || other == TaintThreeLevelsLattice.Top)
			return TaintThreeLevelsLattice.Top;

		return TaintThreeLevelsLattice.Clean;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TaintThreeLevelsLattice.Tainted;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TaintThreeLevelsLattice.Top;
	}

}
