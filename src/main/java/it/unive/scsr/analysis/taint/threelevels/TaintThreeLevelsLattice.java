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

	private final int element;

	public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(0);
	public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(1);
	public static final TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(2);
	public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(3);

	public TaintThreeLevelsLattice(int element) {
		this.element = element;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this.isBottomElement())
			return other;
		if (other.isBottomElement())
			return this;
		if (this.isTopElement() || other.isTopElement())
			return TOP;
		if (this.element == other.element)
			return this;
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this.element == other.element || other.isTopElement() || this.isBottomElement())
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
	public StructuredRepresentation representation() {
		if (this.isTopElement())
			return Lattice.topRepresentation();
		if (this.isBottomElement())
			return Lattice.bottomRepresentation();
		if (this.isTaintElement())
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
		return lubAux(other);
	}

	@Override
	public boolean isAlwaysTainted() {
		return this.isTaintElement();
	}

	@Override
	public boolean isPossiblyTainted() {
		return this.isTaintElement() || this.isTopElement();
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(element);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;
		return element == other.element;
	}

	private boolean isTopElement() {
		return element == 0;
	}

	private boolean isCleanElement() {
		return element == 1;
	}

	private boolean isTaintElement() {
		return element == 2;
	}

	private boolean isBottomElement() {
		return element == 3;
	}

}
