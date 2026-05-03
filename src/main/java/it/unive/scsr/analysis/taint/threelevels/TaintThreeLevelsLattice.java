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
public class TaintThreeLevelsLattice
		implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {
	static public TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice("Top");
	static public TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice("Clean");
	static public TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice("Taint");
	static public TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice("Bottom");

	private String element;

	public TaintThreeLevelsLattice(String e) {
		this.element = e;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO

		if (this == TOP || other == TOP) {
			return TOP;
		}
		if (this == BOTTOM) {
			return other;
		}
		if (other == BOTTOM) {
			return this;
		}
		return TOP;
	}

	@Override

	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO
		if (other == TOP) {
			return true;
		}
		if (other == BOTTOM || this == TOP) {
			return false;
		}
		if (other == TAINT || other == CLEAN) {
			return this == BOTTOM || (this == other);
		}
		return this == other;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		// TODO
		return TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		// TODO
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO

		if (this == TOP) {
			return Lattice.topRepresentation();
		}
		if (this == CLEAN) {
			return new StringRepresentation("C");
		}
		if (this == TAINT) {
			return new StringRepresentation("T");
		}
		return Lattice.bottomRepresentation();

	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		// TODO
		return TAINT;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		// TODO
		return CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {

		if (this == BOTTOM || other == BOTTOM)
			return TaintThreeLevelsLattice.BOTTOM;

		if (this == TAINT || other == TAINT)
			return TaintThreeLevelsLattice.TAINT;

		if (this == TOP || other == TOP)
			return TaintThreeLevelsLattice.TOP;

		return TaintThreeLevelsLattice.CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		// TODO
		return this == TAINT;
	}

	@Override
	public boolean isPossiblyTainted() {
		// TODO
		return this == TOP;
	}
	//added for completness
	@Override
	public int hashCode() {
		return Objects.hash(element);
	}
	//added for completness
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;
		return Objects.equals(element, other.element);
	}
}
