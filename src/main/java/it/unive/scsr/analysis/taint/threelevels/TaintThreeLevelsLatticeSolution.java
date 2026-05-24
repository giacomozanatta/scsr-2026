package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.informationFlow.TaintLattice;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class TaintThreeLevelsLatticeSolution implements
        TaintLattice<TaintThreeLevelsLatticeSolution> {

    /**
     * The top instance of this taint lattice, representing values that are
     * possibly tainted.
     */
    public static final TaintThreeLevelsLatticeSolution TOP = new TaintThreeLevelsLatticeSolution((byte) 3);

    /**
     * The tainted instance of this taint lattice, representing values that are
     * always tainted.
     */
    public static final TaintThreeLevelsLatticeSolution TAINTED = new TaintThreeLevelsLatticeSolution((byte) 2);

    /**
     * The clean instance of this taint lattice, representing values that are
     * always clean.
     */
    public static final TaintThreeLevelsLatticeSolution CLEAN = new TaintThreeLevelsLatticeSolution((byte) 1);

    /**
     * The bottom instance of this taint lattice.
     */
    public static final TaintThreeLevelsLatticeSolution BOTTOM = new TaintThreeLevelsLatticeSolution((byte) 0);

    private final byte taint;

    /**
     * Builds a new instance of taint.
     */
    public TaintThreeLevelsLatticeSolution() {
        this((byte) 3);
    }

    private TaintThreeLevelsLatticeSolution(byte v) {
        this.taint = v;
    }

    @Override
    public TaintThreeLevelsLatticeSolution tainted() {
        return TAINTED;
    }

    @Override
    public TaintThreeLevelsLatticeSolution clean() {
        return CLEAN;
    }

    @Override
    public boolean isAlwaysTainted() {
        return this == TAINTED;
    }

    @Override
    public boolean isPossiblyTainted() {
        return this == TOP;
    }

    @Override
    public StructuredRepresentation representation() {
        return this == BOTTOM ? Lattice.bottomRepresentation()
                : this == CLEAN ? new StringRepresentation("_")
                  : this == TAINTED ? new StringRepresentation("#") : Lattice.topRepresentation();
    }

    @Override
    public TaintThreeLevelsLatticeSolution top() {
        return TOP;
    }

    @Override
    public TaintThreeLevelsLatticeSolution bottom() {
        return BOTTOM;
    }

    @Override
    public TaintThreeLevelsLatticeSolution lubAux(TaintThreeLevelsLatticeSolution other) throws SemanticException { // only happens with clean and tainted, that are not comparable
        return TOP;
    }

    @Override
    public TaintThreeLevelsLatticeSolution wideningAux(TaintThreeLevelsLatticeSolution other) throws SemanticException { // only happens with clean and tainted, that are not comparable
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLatticeSolution other) throws SemanticException { // only happens with clean and tainted, that are not comparable
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + taint;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaintThreeLevelsLatticeSolution other = (TaintThreeLevelsLatticeSolution) obj;
        if (taint != other.taint)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return representation().toString();
    }

    @Override
    public TaintThreeLevelsLatticeSolution or(TaintThreeLevelsLatticeSolution other) throws SemanticException {
        if (this == TAINTED || other == TAINTED)
            return TAINTED;

        if (this == TOP || other == TOP)
            return TOP;

        return CLEAN;
    }

}
