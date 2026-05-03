package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.taint.TaintLattice;

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

    Boolean element;

    static public TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(true);
    static public TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(false);
    static public TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(null);
    static public TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(null);

    public TaintThreeLevelsLattice(Boolean e)
    {
        element = e;
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
        if(this == BOTTOM)
            return Lattice.bottomRepresentation();
        else if(this == TOP)
            return Lattice.topRepresentation();
        else if(this == TAINT)
            return new StringRepresentation("T");
        else if(this == CLEAN)
            return new StringRepresentation("C");
        throw new IllegalStateException("Unknown taint value: " + this);
    }

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if((this == TAINT && other == CLEAN) || (this == CLEAN && other == TAINT))
            return TOP;

        throw new IllegalStateException("Unexpected lub case: " + this + " ⊔ " + other);
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		return false;
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
		if(this == BOTTOM || other == BOTTOM)
            return BOTTOM;
        if(this == TAINT || other == TAINT)
            return TAINT;
        if(this == TOP || other == TOP)
            return TOP;
        return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINT;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TAINT || this == TOP;
	}
}
