package it.unive.scsr.analysis.interval;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class FloatIntervalLattice implements BaseLattice<FloatIntervalLattice> {

    static final FloatIntervalLattice TOP    = new FloatIntervalLattice(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    static final FloatIntervalLattice BOTTOM = new FloatIntervalLattice(Double.NaN, Double.NaN);

    final double lo;
    final double hi;

    public FloatIntervalLattice() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    FloatIntervalLattice(double lo, double hi) {
        this.lo = lo;
        this.hi = hi;
    }

    @Override public FloatIntervalLattice top()    { return TOP; }
    @Override public FloatIntervalLattice bottom() { return BOTTOM; }

    @Override
    public FloatIntervalLattice lubAux(FloatIntervalLattice other) throws SemanticException {
        if (isBottom()) return other;
        if (other.isBottom()) return this;
        return new FloatIntervalLattice(Math.min(lo, other.lo), Math.max(hi, other.hi));
    }

    @Override
    public FloatIntervalLattice glbAux(FloatIntervalLattice other) throws SemanticException {
        if (isBottom() || other.isBottom()) return BOTTOM;
        double newLo = Math.max(lo, other.lo);
        double newHi = Math.min(hi, other.hi);
        return newLo > newHi ? BOTTOM : new FloatIntervalLattice(newLo, newHi);
    }

    @Override
    public boolean lessOrEqualAux(FloatIntervalLattice other) throws SemanticException {
        if (isBottom()) return true;
        if (other.isBottom()) return false;
        return other.lo <= lo && hi <= other.hi;
    }

    @Override
    public FloatIntervalLattice wideningAux(FloatIntervalLattice other) throws SemanticException {
        double newLo = (other.lo < lo) ? Double.NEGATIVE_INFINITY : lo;
        double newHi = (other.hi > hi) ? Double.POSITIVE_INFINITY : hi;
        return new FloatIntervalLattice(newLo, newHi);
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom()) return Lattice.bottomRepresentation();
        String l = lo == Double.NEGATIVE_INFINITY ? "-inf" : String.valueOf(lo);
        String h = hi == Double.POSITIVE_INFINITY ? "+inf" : String.valueOf(hi);
        return new StringRepresentation("[" + l + ", " + h + "]");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FloatIntervalLattice)) return false;
        FloatIntervalLattice o = (FloatIntervalLattice) obj;
        if (Double.isNaN(lo) && Double.isNaN(o.lo)) return true;
        return Double.compare(lo, o.lo) == 0 && Double.compare(hi, o.hi) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lo, hi);
    }
}
