package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;


import java.util.Objects;

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
	private final int level;

	static public TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(0);
	static public TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(1);
	static public TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(2);
	static public TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(3);


	public TaintThreeLevelsLattice(int level) {
		this.level = level;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// lub(x,x) = x; lub(⊥,x) = lub(x,⊥) = x; lub(x, Top) = lub(Top,x) = Top already handled by LiSA
		// remaining case : lub(TAINT,CLEAN) = lub(CLEAN,TAINT) = TOP
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
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
		if(this == BOTTOM)
			return Lattice.bottomRepresentation();
		if(this == TOP)
			return Lattice.topRepresentation();
		if(this == TAINT)
			return new StringRepresentation("T");

		return new StringRepresentation("C");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TAINT;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if(this == BOTTOM || other == BOTTOM)
			return BOTTOM;

		if (this == TAINT || other == TAINT)
			return TAINT;

		if (this == TOP || other == TOP)
			return TOP;

		return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINT;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TOP;
	}

	@Override
	public int hashCode() {
		return Objects.hash(level);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;
		return level == other.level;
	}
}
