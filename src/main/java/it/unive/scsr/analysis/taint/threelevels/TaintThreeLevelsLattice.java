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

    private int level;

    public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(0);
    public static final TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(1);
    public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(2);
    public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(3);

    public TaintThreeLevelsLattice(int level) {
        this.level = level;
    }

    @Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        if(other.level == this.level) return this;
        else return top();
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        return other.level == this.level;
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
        if (this.level == 0) {
            return Lattice.topRepresentation();
        } else if (this.level == 1) {
            return new StringRepresentation("TAINT");
        } else if (this.level == 2) {
            return new StringRepresentation("CLEAN");
        }
        //else if (this.currentState == 3)
        return Lattice.bottomRepresentation();
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
		return this.lub(other);
	}

	@Override
	public boolean isAlwaysTainted() {
		return this.level == TAINT.level;
	}

	@Override
	public boolean isPossiblyTainted() {
        return this.level == TOP.level;
	}

    @Override
    public int hashCode() {
        return java.util.Objects.hash(level);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;
        return this.level == other.level;
    }

}
