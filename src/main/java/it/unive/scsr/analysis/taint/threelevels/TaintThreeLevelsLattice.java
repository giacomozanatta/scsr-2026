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
 *  Bottom
 * 
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

    public static final TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(0);
    public static final TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(1);
    public static final TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(2);
    public static final TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(3);

    private final Integer id;
    public TaintThreeLevelsLattice(int id) {
        this.id = id;
    }

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO
		return TaintThreeLevelsLattice.Top;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO
        if(this == other || other == Bottom) {
            return true;
        }
        if(other == Top) {
            return true;
        }
		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		// TODO
		return TaintThreeLevelsLattice.Top;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		// TODO
		return TaintThreeLevelsLattice.Bottom;
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO
        if(this == TaintThreeLevelsLattice.Top) {
            return Lattice.topRepresentation();
        }
        if(this == TaintThreeLevelsLattice.Bottom) {
            return Lattice.bottomRepresentation();
        }
        if(this == TaintThreeLevelsLattice.Taint) {
            return new StringRepresentation("T");
        }
        if(this == TaintThreeLevelsLattice.Clean) {
            return new StringRepresentation("C");
        }
		return null;
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		// TODO
		return TaintThreeLevelsLattice.Taint;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		// TODO
		return TaintThreeLevelsLattice.Clean;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO
        if(this == TaintThreeLevelsLattice.Bottom || other == TaintThreeLevelsLattice.Bottom) {
            return TaintThreeLevelsLattice.Bottom;
        }
        if(this == TaintThreeLevelsLattice.Taint || other == TaintThreeLevelsLattice.Taint) {
            return TaintThreeLevelsLattice.Taint;
        }
        if(this == TaintThreeLevelsLattice.Top || other == TaintThreeLevelsLattice.Top) {
            return TaintThreeLevelsLattice.Top;
        }
		return TaintThreeLevelsLattice.Clean;
	}

	@Override
	public boolean isAlwaysTainted() {
		// TODO
		return this == TaintThreeLevelsLattice.Taint;
	}

	@Override
	public boolean isPossiblyTainted() {
		// TODO
		return (this ==TaintThreeLevelsLattice.Taint || this == TaintThreeLevelsLattice.Top);
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj==null)
            return false;
        if(this.getClass()!=obj.getClass())
            return false;
        TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;
        return (this.id.equals(other.id));
    }

}
