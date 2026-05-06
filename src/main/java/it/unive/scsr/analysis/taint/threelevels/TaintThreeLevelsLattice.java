package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.parity.ParityLattice;
import it.unive.scsr.analysis.sign.SignLattice;

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

	private int element;

	// declaration of lattice elements
	// 0, 1, 2, 3, 4 are just an encoding
	public static TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(0);
	public static TaintThreeLevelsLattice TAINTED = new TaintThreeLevelsLattice(1);
	public static TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(2);
	public static TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(3);

	public TaintThreeLevelsLattice(int e) {
		element = e;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// DONE
		if (this == TaintThreeLevelsLattice.TOP || other == TOP)
			return TOP;

		if (this == TaintThreeLevelsLattice.BOTTOM)
			return other;

		if (other == BOTTOM)
			return this;

		if (this.element == other.element)
			return this;

		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// DOne
		if (this == other)
			return true;

		if (this == TaintThreeLevelsLattice.BOTTOM)
			return true;

		if (other == TaintThreeLevelsLattice.TOP)
			return true;

		// CLEAN and TAINTED are incomparable
		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		// DONE
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		// DONE
		return TaintThreeLevelsLattice.BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		// DONE
		if(this == TaintThreeLevelsLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if(this == TaintThreeLevelsLattice.TOP)
			return Lattice.topRepresentation();
		else if(this == TaintThreeLevelsLattice.TAINTED)
			return new StringRepresentation("tainted");
		else if(this == TaintThreeLevelsLattice.CLEAN)
			return new StringRepresentation("clean");
		return Lattice.topRepresentation();
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		// DONe
		return TaintThreeLevelsLattice.TAINTED;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		// DONE
		return TaintThreeLevelsLattice.CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		// DONE
		return lubAux(other);
	}

	@Override
	public boolean isAlwaysTainted() {
		// DONE
		return this == TaintThreeLevelsLattice.TAINTED;
	}

	@Override
	public boolean isPossiblyTainted() {
		// DONE
		return  this == TaintThreeLevelsLattice.TAINTED || this == TaintThreeLevelsLattice.TOP;
	}

}
