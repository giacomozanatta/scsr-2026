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

    public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(0);
    public static final TaintThreeLevelsLattice TAINTED = new TaintThreeLevelsLattice(1);
    public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(2);
    public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(3);

    private final int level;
    private TaintThreeLevelsLattice(int level) { this.level = level; }

    @Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
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
		if (this == TOP)
            return Lattice.topRepresentation();
        else if (this == BOTTOM)
            return Lattice.bottomRepresentation();
        else if (this == TAINTED)
            return new StringRepresentation("TAINTED");
        else if (this == CLEAN)
            return new StringRepresentation("CLEAN");
        return new StringRepresentation("?");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TAINTED;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == BOTTOM || other == BOTTOM)
            return BOTTOM;
        if (this == TOP || other == TOP)
            return TOP;
        if (this == TAINTED || other == TAINTED)
            return TAINTED;
        return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINTED;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TAINTED || this == TOP;
	}

}
