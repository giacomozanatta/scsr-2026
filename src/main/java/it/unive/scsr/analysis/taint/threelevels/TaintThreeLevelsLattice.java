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
    public static TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(1);
    public static TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(2);
    public static TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(3);

    public TaintThreeLevelsLattice() {
        this(3); //evaluated to TOP
    }

    public TaintThreeLevelsLattice(Integer element) {
        if(element < 0 || element > 3)
            throw new IllegalArgumentException("element must be between 0 and 3");
        this.element = element;
    }

    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        if(this.equals(other))
            return this;

        if(this.equals(BOTTOM) && other.equals(BOTTOM))
            return BOTTOM;

        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        //this <= other
        if(other.equals(TOP))
            return true;

        if(this.equals(BOTTOM))
            return true;

        return this.equals(other);
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
        if(this.equals(BOTTOM))
            return Lattice.bottomRepresentation();
        if(this.equals(TOP))
            return Lattice.topRepresentation();
        return new StringRepresentation(this.equals(Taint) ? "Taint" : "Clean");
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
        if(this.equals(BOTTOM) || other.equals(BOTTOM))
            return BOTTOM;
        else if(this.equals(Taint) || other.equals(Taint))
            return Taint;
        else if(this.equals(TOP) || other.equals(TOP))
            return TOP;
        return Clean;
    }

    @Override
    public boolean isAlwaysTainted() {
        return this.equals(Taint);
    }

    @Override
    public boolean isPossiblyTainted() {
        return this.equals(Taint) || this.equals(TOP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaintThreeLevelsLattice that = (TaintThreeLevelsLattice) o;
        return Objects.equals(element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(element);
    }
}
