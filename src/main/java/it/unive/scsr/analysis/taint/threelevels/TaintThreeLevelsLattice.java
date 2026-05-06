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
//Work done by Elia Stevanato 895598 & Francesco Pasqualato 897778
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

	Integer element;

	static public TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(3);
	static public TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(2);
	static public TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(1);
	static public TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(0);

	public TaintThreeLevelsLattice(Integer e){
		this.element = e;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other) return this;
		if (this == Bottom) return other;
		if (other == Bottom) return this;
		return Top;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other) return true;
		if (this == Bottom) return true;
        return other == Top;
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
		if(this == Bottom) return Lattice.bottomRepresentation();
		else if(this == Top) return Lattice.topRepresentation();
		return this == Taint ? new StringRepresentation(("T")) : new StringRepresentation(("C"));
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.Taint;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.Clean;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == Taint;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == Top || this == Taint;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TaintThreeLevelsLattice that)) return false;
        return Objects.equals(element, that.element);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(element);
	}
}
