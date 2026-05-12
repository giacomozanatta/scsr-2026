package it.unive.scsr.analysis.floatInterval;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class IntervalFloatLattice
        implements BaseLattice<IntervalFloatLattice>, Comparable<IntervalFloatLattice> {

    // ---------------------------------------------------------------
    // Bounds stored as double. NaN signals BOTTOM.
    // Double.NEGATIVE_INFINITY / POSITIVE_INFINITY represent ±∞.
    // ---------------------------------------------------------------
    final double low;
    final double high;

    // Sentinel: bottom is represented by NaN in both fields.
    private static final double NaN = Double.NaN;

    public static final IntervalFloatLattice TOP    = new IntervalFloatLattice(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    public static final IntervalFloatLattice BOTTOM = new IntervalFloatLattice(NaN, NaN);
    public static final IntervalFloatLattice ZERO   = new IntervalFloatLattice(0.0, 0.0);

    // Widening thresholds (finite set → guarantees termination for floats).
    // If the interval grows beyond a threshold the bound jumps to ±∞.
    private static final double[] THRESHOLDS = {
            Double.NEGATIVE_INFINITY,
            -1e15, -1e10, -1e6, -1e3, -1e2, -1e1, -1.0, -0.5, 0.0,
            0.5,   1.0,   1e1,  1e2,  1e3,  1e6,  1e10, 1e15,
            Double.POSITIVE_INFINITY
    };

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    public IntervalFloatLattice(double low, double high) {
        this.low  = low;
        this.high = high;
    }

    // ------------------------------------------------------------------
    // Lattice interface
    // ------------------------------------------------------------------

    @Override
    public IntervalFloatLattice top() { return TOP; }

    @Override
    public IntervalFloatLattice bottom() { return BOTTOM; }

    @Override
    public boolean isTop() {
        return Double.isInfinite(low) && low < 0
                && Double.isInfinite(high) && high > 0;
    }

    @Override
    public boolean isBottom() {
        return Double.isNaN(low);
    }

    // ------------------------------------------------------------------
    // Representation
    // ------------------------------------------------------------------

    @Override
    public StructuredRepresentation representation() {
        if (isBottom())
            return Lattice.bottomRepresentation();
        String l = Double.isInfinite(low)  ? (low  < 0 ? "-∞" : "+∞") : String.valueOf(low);
        String u = Double.isInfinite(high) ? (high > 0 ? "+∞" : "-∞") : String.valueOf(high);
        return new StringRepresentation("[" + l + ", " + u + "]");
    }

    // ------------------------------------------------------------------
    // LUB  (join)
    // ------------------------------------------------------------------

    @Override
    public IntervalFloatLattice lubAux(IntervalFloatLattice other) throws SemanticException {
        return new IntervalFloatLattice(
                Math.min(this.low,  other.low),
                Math.max(this.high, other.high)
        );
    }

    // ------------------------------------------------------------------
    // GLB  (meet)
    // ------------------------------------------------------------------

    @Override
    public IntervalFloatLattice glbAux(IntervalFloatLattice other) throws SemanticException {
        double l = Math.max(this.low,  other.low);
        double u = Math.min(this.high, other.high);
        // Empty intersection → bottom
        return l <= u ? new IntervalFloatLattice(l, u) : BOTTOM;
    }

    // ------------------------------------------------------------------
    // Partial order
    // ------------------------------------------------------------------

    @Override
    public boolean lessOrEqualAux(IntervalFloatLattice other) throws SemanticException {
        return other.low <= this.low && this.high <= other.high;
    }

    // ------------------------------------------------------------------
    // Widening  ← KEY CHANGE for float support
    //
    // Instead of jumping immediately to ±∞ on any growth, we snap each
    // bound to the nearest threshold in THRESHOLDS. This keeps the
    // abstract chain finite (|THRESHOLDS| steps max) while being more
    // precise than the bare integer widening.
    // ------------------------------------------------------------------

    @Override
    public IntervalFloatLattice wideningAux(IntervalFloatLattice other) throws SemanticException {
        double lResult = this.low;
        double uResult = this.high;

        // Lower bound decreased → snap down to largest threshold ≤ new low
        if (other.low < this.low)
            lResult = lowerThreshold(other.low);

        // Upper bound increased → snap up to smallest threshold ≥ new high
        if (other.high > this.high)
            uResult = upperThreshold(other.high);

        return new IntervalFloatLattice(lResult, uResult);
    }

    /** Largest threshold t such that t ≤ v  (returns -∞ if none). */
    private static double lowerThreshold(double v) {
        for (int i = THRESHOLDS.length - 1; i >= 0; i--)
            if (THRESHOLDS[i] <= v)
                return THRESHOLDS[i];
        return Double.NEGATIVE_INFINITY;
    }

    /** Smallest threshold t such that t ≥ v  (returns +∞ if none). */
    private static double upperThreshold(double v) {
        for (double t : THRESHOLDS)
            if (t >= v)
                return t;
        return Double.POSITIVE_INFINITY;
    }

    // ------------------------------------------------------------------
    // Equality / hash
    // ------------------------------------------------------------------

    @Override
    public int hashCode() { return Objects.hash(low, high); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntervalFloatLattice)) return false;
        IntervalFloatLattice o = (IntervalFloatLattice) obj;
        // Both bottom
        if (this.isBottom() && o.isBottom()) return true;
        return Double.compare(low, o.low) == 0 && Double.compare(high, o.high) == 0;
    }

    @Override
    public int compareTo(IntervalFloatLattice o) {
        if (isBottom()) return o.isBottom() ? 0 : -1;
        if (isTop())    return o.isTop()    ? 0 :  1;
        if (o.isBottom()) return 1;
        if (o.isTop())    return -1;
        int cmpL = Double.compare(this.low,  o.low);
        return cmpL != 0 ? cmpL : Double.compare(this.high, o.high);
    }
}
