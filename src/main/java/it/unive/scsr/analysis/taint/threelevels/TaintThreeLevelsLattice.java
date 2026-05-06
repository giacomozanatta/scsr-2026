package it.unive.scsr.analysis.taint.threelevels;

import java.util.Objects;

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

	public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(0);
	public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(1);
	public static final TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(2);
	public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(3);

	public final Integer tlevel;

	public TaintThreeLevelsLattice(Integer tlevel) {
		this.tlevel = tlevel;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if(this.equals(TaintThreeLevelsLattice.BOTTOM))
			return other;

		if(other.equals(TaintThreeLevelsLattice.BOTTOM) || this.equals(other))
			return this;

		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if(this.equals(TaintThreeLevelsLattice.BOTTOM))
			return true;

		if(other.equals(TaintThreeLevelsLattice.TOP))
			return true;

		return this.equals(other);
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
		if (this.equals(TaintThreeLevelsLattice.TOP)) 
        	return Lattice.topRepresentation();

    	if (this.equals(TaintThreeLevelsLattice.BOTTOM)) 
        	return Lattice.bottomRepresentation();

    	if (this.equals(TaintThreeLevelsLattice.CLEAN)) 
       		return new StringRepresentation("CLEAN");

    	return new StringRepresentation("TAINT");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.TAINT;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this.equals(BOTTOM) || other.equals(BOTTOM))
			return BOTTOM;

		if (this.equals(TAINT) || other.equals(TAINT))
			return TAINT;

		if (this.equals(CLEAN) && other.equals(CLEAN))
			return CLEAN;

		return TOP;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this.equals(TaintThreeLevelsLattice.TAINT);
	}

	@Override
	public boolean isPossiblyTainted() {
		return this.equals(TaintThreeLevelsLattice.TOP);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof TaintThreeLevelsLattice))
			return false;

		TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;

		return Objects.equals(this.tlevel, other.tlevel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tlevel);
	}
}
