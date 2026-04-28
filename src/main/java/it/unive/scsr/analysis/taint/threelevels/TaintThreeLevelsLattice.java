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

	public int element;

	public static TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(0);
	public static TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(1);
	public static TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(2);
	public static TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(3);

	public TaintThreeLevelsLattice(int e) {
		this.element = e;
	}

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

		if(this == Bottom)
			return Lattice.bottomRepresentation();

		if(this == Clean)
			return new StringRepresentation("C");

		if(this == Taint)
			return new StringRepresentation("T");

		return Lattice.topRepresentation();
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

		if (this == Taint || other == Taint)
			return Taint;

		if (this == Top || other == Top)
			return Top;

		return Clean;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == Taint;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == Top;
	}

	@Override
	public int hashCode() {
		return Objects.hash(element);
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

		return element == other.element;
	}

}
