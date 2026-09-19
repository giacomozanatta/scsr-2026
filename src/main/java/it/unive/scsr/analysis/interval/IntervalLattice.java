package it.unive.scsr.analysis.interval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/**
 * The mathematical lattice structure for Interval Analysis.
 * Note: A bound set to 'null' represents infinity (negative or positive depending on position).
 */
public class IntervalLattice implements BaseLattice<IntervalLattice> {

    // TOP represents the interval [-infinity, +infinity]
    public static final IntervalLattice TOP = new IntervalLattice(null, null);
    
    // BOTTOM represents an empty interval (unreachable code path)
    public static final IntervalLattice BOTTOM = new IntervalLattice(Double.NaN, Double.NaN);

    // The lower bound of the interval
    private final Double low;
    
    // The upper bound of the interval
    private final Double high;

    // Constructor initializing the bounds
    public IntervalLattice(Double low, Double high) {
        this.low = low;
        this.high = high;
    }

    public Double getLow() { return low; }
    public Double getHigh() { return high; }

    @Override
    public IntervalLattice top() { return TOP; }

    @Override
    public IntervalLattice bottom() { return BOTTOM; }

    // Least Upper Bound (LUB) operator: merges two intervals by taking the widest bounds
    @Override
    public IntervalLattice lubAux(IntervalLattice other) throws SemanticException {
        Double newLow = (low == null || other.low == null) ? null : Math.min(low, other.low);
        Double newHigh = (high == null || other.high == null) ? null : Math.max(high, other.high);
        return new IntervalLattice(newLow, newHigh);
    }

    // Widening operator: ensures the analysis converges and doesn't loop infinitely
    // If a bound is growing (or shrinking), we safely jump it straight to infinity (null)
    @Override
    public IntervalLattice wideningAux(IntervalLattice other) throws SemanticException {
        Double newLow = (other.low == null || (low != null && other.low < low)) ? null : low;
        Double newHigh = (other.high == null || (high != null && other.high > high)) ? null : high;
        return new IntervalLattice(newLow, newHigh);
    }

    // Partial order relation: checks if 'this' interval is fully contained within the 'other' interval
    @Override
    public boolean lessOrEqualAux(IntervalLattice other) throws SemanticException {
        boolean lowOk = (other.low == null) || (low != null && low >= other.low);
        boolean highOk = (other.high == null) || (high != null && high <= other.high);
        return lowOk && highOk;
    }

    // Formats the interval for output in the generated HTML graphs
    @Override
    public StructuredRepresentation representation() {
        if (isBottom()) return Lattice.bottomRepresentation();
        if (isTop()) return Lattice.topRepresentation();
        
        // Use "-inf" and "+inf" when bounds are null
        String l = low == null ? "-inf" : low.toString();
        String h = high == null ? "+inf" : high.toString();
        
        return new StringRepresentation("[" + l + ", " + h + "]");
    }
}