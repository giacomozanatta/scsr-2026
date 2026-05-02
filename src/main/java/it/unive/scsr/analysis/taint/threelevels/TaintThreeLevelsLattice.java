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

/**
 * @author Mattia Acquilesi 896827
 * @author Alan Dal Col 895879
 */
public class TaintThreeLevelsLattice
        implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

    private enum Value { BOTTOM, CLEAN, TAINT, TOP }

    private final Value value;

    private static final TaintThreeLevelsLattice BOTTOM_INSTANCE = new TaintThreeLevelsLattice(Value.BOTTOM);
    private static final TaintThreeLevelsLattice CLEAN_INSTANCE  = new TaintThreeLevelsLattice(Value.CLEAN);
    private static final TaintThreeLevelsLattice TAINT_INSTANCE  = new TaintThreeLevelsLattice(Value.TAINT);
    private static final TaintThreeLevelsLattice TOP_INSTANCE    = new TaintThreeLevelsLattice(Value.TOP);

    public TaintThreeLevelsLattice() {
        this(Value.BOTTOM);
    }

    private TaintThreeLevelsLattice(Value value) {
        this.value = value;
    }


    @Override
    public TaintThreeLevelsLattice top() {
        return TOP_INSTANCE;
    }

    @Override
    public TaintThreeLevelsLattice bottom() {
        return BOTTOM_INSTANCE;
    }

    @Override
    public TaintThreeLevelsLattice tainted() {
        return TAINT_INSTANCE;
    }

    @Override
    public TaintThreeLevelsLattice clean() {
        return CLEAN_INSTANCE;
    }

    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        return false;
    }


    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        if (this.value == other.value) {
            return this;
        }
        return TOP_INSTANCE;
    }


    @Override
    public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
        if (this.value == Value.BOTTOM || other.value == Value.BOTTOM)
            return BOTTOM_INSTANCE;

        if (this.value == Value.TAINT || other.value == Value.TAINT)
            return TAINT_INSTANCE;

        if (this.value == Value.TOP || other.value == Value.TOP)
            return TOP_INSTANCE;

        return CLEAN_INSTANCE;
    }


    @Override
    public boolean isAlwaysTainted() {
        return this.value == Value.TAINT;
    }

    @Override
    public boolean isPossiblyTainted() {
        return this.value == Value.TAINT || this.value == Value.TOP;
    }

    @Override
    public StructuredRepresentation representation() {
        switch (this.value) {
            case BOTTOM: return Lattice.bottomRepresentation();
            case CLEAN:  return new StringRepresentation("C");
            case TAINT:  return new StringRepresentation("T");
            case TOP:    return Lattice.topRepresentation();
            default:     return new StringRepresentation("Unknown");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaintThreeLevelsLattice)) return false;
        return this.value == ((TaintThreeLevelsLattice) o).value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return representation().toString();
    }
}

