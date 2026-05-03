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

	int element;

	public static TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(0);
	public static TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(1);
	public static TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(2);
	public static TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(3);

	private TaintThreeLevelsLattice(int e) { this.element = e; }

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		return Top;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return Top;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return Bottom;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == Top) return Lattice.topRepresentation();
		if (this == Taint) return new StringRepresentation("taint");
		if (this == Clean) return new StringRepresentation("clean");
		return Lattice.bottomRepresentation();
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return Taint;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return Clean;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == Bottom || other == Bottom) return Bottom;
		if (this == other) return this;
		return Top;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == Taint;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == Top || this == Taint;
	}

}
