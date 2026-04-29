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
    private final Integer lvl;
    public static TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(0);
    public static TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(1);
    public static TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(2);
    public static TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(3);

    public TaintThreeLevelsLattice(Integer lvl) {
        this.lvl = lvl;
    }

    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        if (this.equals(other))
            return this;
        // TODO: check if i need to add this == bottom && other == bottom
        return TaintThreeLevelsLattice.Top;
    }

    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        if (this.equals(other))
            return true;
        if (this.equals(TaintThreeLevelsLattice.Bottom))
            return true;
        if (other.equals(TaintThreeLevelsLattice.Top))
            return true;
        return false;
    }

    @Override
    public TaintThreeLevelsLattice top() {
        return TaintThreeLevelsLattice.Top;
    }

    @Override
    public TaintThreeLevelsLattice bottom() {
        return TaintThreeLevelsLattice.Bottom;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == TaintThreeLevelsLattice.Bottom)
            return Lattice.bottomRepresentation();
        if (this == TaintThreeLevelsLattice.Top)
            return Lattice.topRepresentation();
        if (this == TaintThreeLevelsLattice.Clean)
            return new StringRepresentation("C");
        if (this == TaintThreeLevelsLattice.Taint)
            return new StringRepresentation("T");
        return new StringRepresentation("");
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
        // TODO
        // if a != b and not top or bottom, return top

        if (this.equals(TaintThreeLevelsLattice.Bottom) || other.equals(TaintThreeLevelsLattice.Bottom))
            return TaintThreeLevelsLattice.Bottom;
        if (this.equals(TaintThreeLevelsLattice.Top) || other.equals(TaintThreeLevelsLattice.Top))
            return TaintThreeLevelsLattice.Top;
        if (this.equals(other) && this.equals(TaintThreeLevelsLattice.Taint))
            return TaintThreeLevelsLattice.Taint;
        if (this.equals(other) && this.equals(TaintThreeLevelsLattice.Clean))
            return TaintThreeLevelsLattice.Clean;

        if ((!this.equals(TaintThreeLevelsLattice.Bottom) && !other.equals(TaintThreeLevelsLattice.Bottom))
                && (!this.equals(TaintThreeLevelsLattice.Top) && !other.equals(TaintThreeLevelsLattice.Top))
        ) {
            if (!this.equals(other))
                return TaintThreeLevelsLattice.Top;
        }
        // If we are here big trouble
        return TaintThreeLevelsLattice.Bottom;
    }

    @Override
    public boolean isAlwaysTainted() {
        return this.equals(TaintThreeLevelsLattice.Taint);
    }

    @Override
    public boolean isPossiblyTainted() {
        return this.equals(TaintThreeLevelsLattice.Top);
    }

}
