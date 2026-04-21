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
	private final String element;

	public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice("TOP");
	public static final TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice("TAINT");
	public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice("CLEAN");
	public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice("BOTTOM");

	public TaintThreeLevelsLattice(String e) {
		this.element = e;
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
	public TaintThreeLevelsLattice tainted() {
		return TAINT;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return CLEAN;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		return prime * ((element != null) ? element.hashCode() : 0);
	}

	@Override
    public StructuredRepresentation representation() {
		// As TaintLattice
		if (this == BOTTOM) {
			return Lattice.bottomRepresentation();
		} 

		if (this == TOP) {
			return Lattice.topRepresentation();
		} 

		return this == TAINT ? new StringRepresentation("TAINT") : new StringRepresentation("CLEAN");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}

		if (obj == null || getClass() != obj.getClass()){
			return false;
		}

		// This and Object is not null so we verify if they are equals based on the element
		TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;

		// If the element is null, we verify if the other element is also null
		// otherwise we verify if the element is equals to the other element
		if (element == null) {
			if (other.element != null) {
				return false;
			}
		} 
		else if (!element.equals(other.element)){
			return false;
		}

		return true;
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
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		// If any part is in an error state (BOTTOM), the result is BOTTOM.
		if (this == BOTTOM || other == BOTTOM) {
            return BOTTOM;
        }

		// If either is TAINT, the result is TAINT
        if (this == TAINT || other == TAINT) {
            return TAINT;
        }

		// If either is TOP, the result is TOP
        if (this == TOP || other == TOP) {
            return TOP;
        }

		// Otherwise, both are CLEAN, so the result is CLEAN
		return CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// LiSA framework already handles the base cases (this == other, bottoms, etc.)
        // The only remaining case is combining two different non-bottom elements.
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// LiSA framework already handles the base cases (this == other, bottoms, etc.)
        // The only remaining case is checking if this is less than or equal to other.
		return false;
	}

}
