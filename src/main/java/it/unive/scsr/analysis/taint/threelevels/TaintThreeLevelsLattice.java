package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

    private final Integer element;

    public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(0);
    public static final TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(1);
    public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(2);
    public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(3);

    private TaintThreeLevelsLattice(Integer element) {
        this.element = element;
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
        if (this == BOTTOM)
            return Lattice.bottomRepresentation();
        else if (this == TOP)
            return Lattice.topRepresentation();
        else if (this == TAINT)
            return new StringRepresentation("T");
        else if (this == CLEAN)
            return new StringRepresentation("C");

        throw new IllegalStateException("Unknown value");
    }

    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        if ((this == TAINT && other == CLEAN) || (this == CLEAN && other == TAINT))
            return TOP;

        throw new IllegalStateException("Unexpected lub: " + this + " ⊔ " + other);
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
        if (this == BOTTOM || other == BOTTOM)
            return BOTTOM;
        if (this == TAINT || other == TAINT)
            return TAINT;
        if (this == TOP || other == TOP)
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TaintThreeLevelsLattice other = (TaintThreeLevelsLattice) obj;
        return this.element.equals(other.element);
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }
}