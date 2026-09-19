package it.unive.scsr.analysis.taint;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Standard 2-level Taint Domain Lattice:
 * T (Tainted - Dirty data)
 * |
 * C (Clean - Safe data)
 * |
 * BOTTOM (Unreachable code)
 */
public class TaintLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintLattice> {

    Boolean element;

    // Define the basic states
    static public TaintLattice Taint = new TaintLattice(true);
    static public TaintLattice Clean = new TaintLattice(false);
    static public TaintLattice Bottom = new TaintLattice(null);

    public TaintLattice(Boolean e) {
        this.element = e;
    }

    // The highest, most general state is Taint
    @Override
    public TaintLattice top() {
        return Taint;
    }

    // The lowest, impossible state is Bottom
    @Override
    public TaintLattice bottom() {
        return Bottom;
    }

    // Generates the 'T' or 'C' letters for the HTML graphs
    @Override
    public StructuredRepresentation representation() {
        if (this == Bottom)
            return Lattice.bottomRepresentation();
        
        return this == Taint ? new StringRepresentation("T") : new StringRepresentation("C");
    }

    // Merges two paths in the code (e.g., if one path is Clean and one is Taint, the result is Taint)
    @Override
    public TaintLattice lubAux(TaintLattice other) throws SemanticException {
        // If they are the same, return any of them
        if (this == other) return this;
        // If one is unreachable (Bottom), return the other
        if (this == Bottom) return other;
        if (other == Bottom) return this;
        // In any other case (mixing Clean and Tainted), assume the worst: it's Tainted
        return Taint;
    }

    // Checks the strict order of states (Bottom -> Clean -> Taint)
    @Override
    public boolean lessOrEqualAux(TaintLattice other) throws SemanticException {
        // Bottom is smaller than everything
        if (this == Bottom) return true;
        // Everything is equal to itself
        if (this == other) return true;
        // Clean is smaller (safer) than Taint
        if (this == Clean && other == Taint) return true;
        
        return false;
    }

    @Override
    public TaintLattice tainted() {
        return TaintLattice.Taint;
    }

    @Override
    public TaintLattice clean() {
        return TaintLattice.Clean;
    }

    // Simulates an OR operation between taints (essentially takes the worst case)
    @Override
    public TaintLattice or(TaintLattice other) throws SemanticException {
        if (this == Bottom || other == Bottom)
            return TaintLattice.Bottom;
        
        if (this == Taint || other == Taint)
            return TaintLattice.Taint;
        
        return TaintLattice.Clean;
    }

    @Override
    public boolean isAlwaysTainted() {
        return this == Taint;
    }

    @Override
    public boolean isPossiblyTainted() {
        return this == Taint;
    }
}