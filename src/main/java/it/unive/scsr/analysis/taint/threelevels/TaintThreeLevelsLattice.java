package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
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

	int elem;
	public static TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(3);
	public static TaintThreeLevelsLattice CLEAN= new TaintThreeLevelsLattice(0);
	public static TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(1);
	public static TaintThreeLevelsLattice BOT = new TaintThreeLevelsLattice(2);


	public TaintThreeLevelsLattice(int elem){
		this.elem = elem;
	}
	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO
		if(this.equals(other)) return this;

		return TOP;

	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// Catches cases (X, TOP) and (X, BOT) which are trivially `true`
		if(other == TOP || this == BOT) return true;

		//Catch-all because C and T are not comparable. The remaining cases are (C,C) and (T,T)
		// .equals may be unnecessary because they are pointing to static objects. I think it would be a problem if they were
		// pointing to two different instances of equivalent objects
		return this.equals(other);

	}

	@Override
	public TaintThreeLevelsLattice top() {
		// TODO
		return TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		// TODO
		return BOT;
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO
		if(this == BOT) return Lattice.bottomRepresentation();
		if(this == TOP) return Lattice.topRepresentation();

		return this == TAINT ? new StringRepresentation("Tainted") : new StringRepresentation("Clean");
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
		// TODO
		// Can this be simplified to return whenever this equals other?
		if(this == TOP || other == TOP) return TOP;
		if(this == BOT || other == BOT) return BOT;
		if(this == TAINT || other == TAINT) return TAINT;
		return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		// TODO
		return this == TAINT;
	}

	@Override
	public boolean isPossiblyTainted() {
		// TODO
		return this == TOP || this == TAINT;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
