package it.unive.scsr.analysis.interval.extendedInterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedIntervalLattice implements BaseLattice<ExtendedIntervalLattice>, Comparable<ExtendedIntervalLattice>{
    private final MathNumber low;
    private final MathNumber high;
    private final boolean isBottom;

    public static final ExtendedIntervalLattice TOP = new ExtendedIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static final ExtendedIntervalLattice BOTTOM = new ExtendedIntervalLattice();
    public static final ExtendedIntervalLattice ZERO = new ExtendedIntervalLattice(MathNumber.ZERO, MathNumber.ZERO);

    private ExtendedIntervalLattice() {
        this.low = null;
        this.high = null;
        this.isBottom = true;
    }

    public ExtendedIntervalLattice(MathNumber low, MathNumber high) {
        this.low = low;
        this.high = high;
        this.isBottom = false;
    }

    public MathNumber getLow() { return low; }
    public MathNumber getHigh() { return high; }

    @Override
    public ExtendedIntervalLattice top() {
        return TOP;
    }

    @Override
    public ExtendedIntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isBottom() { return isBottom; }

    @Override
    public ExtendedIntervalLattice lubAux(ExtendedIntervalLattice extendedIntervalLattice) throws SemanticException {
        MathNumber newLow = this.low.min(extendedIntervalLattice.low);
        MathNumber newMax = this.high.max(extendedIntervalLattice.high);
        return new ExtendedIntervalLattice(newLow, newMax);
    }

    @Override
    public ExtendedIntervalLattice glbAux(ExtendedIntervalLattice other) throws SemanticException {
        //[max(l1, l2), min(u1, u2)]
        MathNumber newLow = this.low.max(other.low);
        MathNumber newHigh = this.high.min(other.high);
        if (newLow.compareTo(newHigh) > 0)
            return BOTTOM;

        return new ExtendedIntervalLattice(newLow, newHigh);
    }

    @Override
    public boolean lessOrEqualAux(ExtendedIntervalLattice extendedIntervalLattice) throws SemanticException {
        return this.low.compareTo(extendedIntervalLattice.low) >= 0 && this.high.compareTo(extendedIntervalLattice.high) <= 0;
    }

    @Override
    public StructuredRepresentation representation() {
        if(this == BOTTOM) return Lattice.bottomRepresentation();
        return new StringRepresentation("["+ low+","+ high+"]");
    }

    @Override
    public ExtendedIntervalLattice wideningAux(ExtendedIntervalLattice other) throws SemanticException{
        MathNumber wLow = this.low;
        MathNumber wHigh = this.high;

        if(other.low.compareTo(this.low) < 0)
            wLow = MathNumber.MINUS_INFINITY;
        if(other.high.compareTo(this.high) > 0)
            wHigh = MathNumber.PLUS_INFINITY;
        return new ExtendedIntervalLattice(wLow, wHigh);
    }

    @Override
    public int compareTo(ExtendedIntervalLattice o) {
        if (isBottom()) return o.isBottom() ? 0 : -1;
        if (o.isBottom()) return 1;
        if (this.equals(TOP)) return o.equals(TOP) ? 0 : 1;
        if (o.equals(TOP)) return -1;
        int cmpLow = low.compareTo(o.low);
        if (cmpLow != 0) return cmpLow;
        return high.compareTo(o.high);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedIntervalLattice that = (ExtendedIntervalLattice) o;
        return isBottom == that.isBottom && Objects.equals(low, that.low) && Objects.equals(high, that.high);
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high, isBottom);
    }
}
