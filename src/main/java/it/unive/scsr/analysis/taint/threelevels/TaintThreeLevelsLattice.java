package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.*;
import it.unive.lisa.analysis.Lattice;
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

	static public TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(3);
	static public TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(2);
	static public TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(1);
	static public TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(0);

	public TaintThreeLevelsLattice(int e) {
		this.element = e;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		return TaintThreeLevelsLattice.Top;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other) return true;
		if (this == Bottom) return true;
		if (other == Top) return true;
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
		if (this == Bottom) return Lattice.bottomRepresentation();
		if (this == Top) return Lattice.topRepresentation();
		if (this == Taint) return new StringRepresentation("T");
		return new StringRepresentation("C");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {return TaintThreeLevelsLattice.Taint;}

	@Override
	public TaintThreeLevelsLattice clean() {return TaintThreeLevelsLattice.Clean;}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == TaintThreeLevelsLattice.Taint || other == TaintThreeLevelsLattice.Taint)
			return TaintThreeLevelsLattice.Taint;
		if (this == TaintThreeLevelsLattice.Clean && other == TaintThreeLevelsLattice.Clean)
			return TaintThreeLevelsLattice.Clean;
		return TaintThreeLevelsLattice.Bottom;
	}

	@Override
	public boolean isAlwaysTainted() {return this == TaintThreeLevelsLattice.Taint;}

	@Override
	public boolean isPossiblyTainted() {return (this == TaintThreeLevelsLattice.Top) || (this == TaintThreeLevelsLattice.Taint);}


	@Override
	public int hashCode() {
		return Integer.hashCode(element);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaintThreeLevelsLattice that = (TaintThreeLevelsLattice) o;
		return element == that.element;
	}
}
