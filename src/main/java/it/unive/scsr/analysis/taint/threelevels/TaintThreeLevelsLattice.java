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

	private final Integer element;
	public static TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(0);
	public static TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(1);
	public static TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(2);
	public static TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(3);

	public TaintThreeLevelsLattice() {
		this(3);
	}

	public TaintThreeLevelsLattice(Integer element) {
		if(element < 0 || element > 3)
			throw new IllegalArgumentException(String.format("Number %d is an illegal level for ThreeLevelsLattice", element));
		this.element = element;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if(this.equals(other))
			return this;
		else if (this.equals(BOTTOM))
			return other;
		else if (other.equals(BOTTOM))
			return this;
		else
			return TOP;
		// TODO
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		//this <= other
		if(other.equals(TOP))
			return true;
		else if(this.equals(BOTTOM))
			return true;
		else
			return this.equals(other);
		// TODO
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return TOP;
		// TODO
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return BOTTOM;
		// TODO
	}

	@Override
	public StructuredRepresentation representation() {
		if(this.equals(BOTTOM))
			return Lattice.bottomRepresentation();
		else if(this.equals(TOP))
			return Lattice.topRepresentation();
		else
			return new StringRepresentation(this.equals(CLEAN) ? "Clean" : "Taint");
		// TODO
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TAINT;
		// TODO
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return CLEAN;
		// TODO
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if(this.equals(BOTTOM) || other.equals(BOTTOM))
			return BOTTOM;
		else if(this.equals(TAINT) || other.equals(TAINT))
			return TOP;
		else if(this.equals(TOP) || other.equals(TOP))
			return TOP;
		else
			return CLEAN;
		// TODO
	}

	@Override
	public boolean isAlwaysTainted() {
		// True if surely tainted
		return this.equals(TAINT);
		// TODO
	}

	@Override
	public boolean isPossiblyTainted() {
		// true if it might be tainted (i.e. tainted or top)
		return this.equals(TAINT) || this.equals(TOP);
		// TODO
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		else if (o == null || getClass() != o.getClass())
			return false;
		else
			return element.equals(((TaintThreeLevelsLattice) o).element);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(element);
	}

}
