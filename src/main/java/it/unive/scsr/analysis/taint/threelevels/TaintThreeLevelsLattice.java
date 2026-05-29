package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.*;
import it.unive.lisa.analysis.Lattice;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.informationFlow.TaintLattice;	
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

	static public TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(3);
	static public TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(2);
	static public TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(1);
	static public TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(0);

	public TaintThreeLevelsLattice() {this(3);}

	public TaintThreeLevelsLattice(int e) {this.element = e;}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other) return true;
		if (this == BOTTOM) return true;
		if (other == TOP) return true;
		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return TaintThreeLevelsLattice.BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == BOTTOM) return Lattice.bottomRepresentation();
		if (this == TOP) return Lattice.topRepresentation();
		if (this == TAINT) return new StringRepresentation("T");
		return new StringRepresentation("C");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {return TaintThreeLevelsLattice.TAINT;}

	@Override
	public TaintThreeLevelsLattice clean() {return TaintThreeLevelsLattice.CLEAN;}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == TaintThreeLevelsLattice.TAINT || other == TaintThreeLevelsLattice.TAINT)
			return TaintThreeLevelsLattice.TAINT;
		if (this == TaintThreeLevelsLattice.BOTTOM && other == TaintThreeLevelsLattice.BOTTOM)
			return TaintThreeLevelsLattice.BOTTOM;
		if (this == TaintThreeLevelsLattice.TOP && other == TaintThreeLevelsLattice.TOP)
			return TaintThreeLevelsLattice.TOP;
		return TaintThreeLevelsLattice.CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {return this == TaintThreeLevelsLattice.TAINT;}

	@Override
	public boolean isPossiblyTainted() {return (this == TaintThreeLevelsLattice.TOP);}


	@Override
	public int hashCode() {
		return Objects.hashCode(element);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaintThreeLevelsLattice that = (TaintThreeLevelsLattice) o;
		return element == that.element;
	}
}
