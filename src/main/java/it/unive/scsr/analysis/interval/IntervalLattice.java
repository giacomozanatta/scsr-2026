package it.unive.scsr.analysis.interval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class IntervalLattice implements BaseLattice<IntervalLattice> {

    public static final IntervalLattice TOP = new IntervalLattice(null, null);
    public static final IntervalLattice BOTTOM = new IntervalLattice(Double.NaN, Double.NaN);

    private final Double low;
    private final Double high;

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

    @Override
    public IntervalLattice lubAux(IntervalLattice other) throws SemanticException {
        Double newLow = (low == null || other.low == null) ? null : Math.min(low, other.low);
        Double newHigh = (high == null || other.high == null) ? null : Math.max(high, other.high);
        return new IntervalLattice(newLow, newHigh);
    }

    @Override
    public IntervalLattice wideningAux(IntervalLattice other) throws SemanticException {
        // Widening для float: если граница растет, сразу уходим в бесконечность
        Double newLow = (other.low == null || (low != null && other.low < low)) ? null : low;
        Double newHigh = (other.high == null || (high != null && other.high > high)) ? null : high;
        return new IntervalLattice(newLow, newHigh);
    }

    @Override
    public boolean lessOrEqualAux(IntervalLattice other) throws SemanticException {
        boolean lowOk = (other.low == null) || (low != null && low >= other.low);
        boolean highOk = (other.high == null) || (high != null && high <= other.high);
        return lowOk && highOk;
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom()) return Lattice.bottomRepresentation();
        if (isTop()) return Lattice.topRepresentation();
        String l = low == null ? "-inf" : low.toString();
        String h = high == null ? "+inf" : high.toString();
        return new StringRepresentation("[" + l + ", " + h + "]");
    }
}