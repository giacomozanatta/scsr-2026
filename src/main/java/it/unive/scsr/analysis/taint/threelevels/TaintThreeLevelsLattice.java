package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Taint with three levels (Diamond Lattice):
 * TOP (Maybe dirty - we don't know for sure)
 * /   \
 * CLEAN   TAINTED (100% dirty)
 * \   /
 * BOTTOM (Unreachable / Empty)
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

    // Define the 4 possible states
    private enum State {
        BOTTOM, CLEAN, TAINTED, TOP
    }

    private final State state;

    public static final TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(State.BOTTOM);
    public static final TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(State.CLEAN);
    public static final TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(State.TAINTED);
    public static final TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(State.TOP);

    private TaintThreeLevelsLattice(State state) {
        this.state = state;
    }

    @Override
    public TaintThreeLevelsLattice top() { return Top; }

    @Override
    public TaintThreeLevelsLattice bottom() { return Bottom; }

    // Merges two states into a safe, wider state
    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        if (this == other || other == Bottom) return this;
        if (this == Bottom) return other;
        // If we mix CLEAN and TAINTED, we can't be sure anymore, so we return TOP
        if (this == Top || other == Top) return Top;
        
        return Top;
    }

    // Checks if this state is more specific than the other state
    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        if (this == other || other == Top || this == Bottom) return true;
        return false;
    }

    // Creates the text to display on the HTML graphs
    @Override
    public StructuredRepresentation representation() {
        if (this == Bottom) return Lattice.bottomRepresentation();
        if (this == Top) return Lattice.topRepresentation();
        return new StringRepresentation(this == Taint ? "T" : "C");
    }

    @Override
    public TaintThreeLevelsLattice tainted() { return Taint; }

    @Override
    public TaintThreeLevelsLattice clean() { return Clean; }

    // Simulates an OR operation (same as merging states)
    @Override
    public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
        return lubAux(other);
    }

    // Returns true only if it is 100% tainted
    @Override
    public boolean isAlwaysTainted() {
        return this == Taint;
    }

    // Returns true if it is 100% tainted OR if we are not sure (TOP)
    @Override
    public boolean isPossiblyTainted() {
        return this == Taint || this == Top;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return state == ((TaintThreeLevelsLattice) obj).state;
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }
}