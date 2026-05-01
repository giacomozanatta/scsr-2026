package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.taint.TaintLattice;

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

	private final Integer element;

	static final public TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(2);
	static final public TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(1);
	static final public TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(0);
	static final public TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(-1);

	public TaintThreeLevelsLattice(Integer e) {
		this.element = e;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// By assumptions, executed only when this is TAINT and
		// other is CLEAN (and vice versa), so the LUB is always TOP
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// By assumptions, executed only when this is TAINT and
		// other is CLEAN (and vice versa), so it is always false
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
		if(this.isTop())
			return Lattice.topRepresentation();
		if(this.isBottom())
			return Lattice.bottomRepresentation();
		if(this == TAINT)
			return new StringRepresentation("Tainted");

		return new StringRepresentation("Clean");
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

		if(this.isBottom() || other.isBottom())
			return BOTTOM;

		if(this == TAINT || other == TAINT)
			return tainted();

		if(this.isTop() || other.isTop())
			return TOP;

		return clean();
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINT;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this.isTop();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		TaintThreeLevelsLattice that = (TaintThreeLevelsLattice) o;
		return Objects.equals(element, that.element);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(element);
	}
}
