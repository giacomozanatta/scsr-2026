package it.unive.scsr.analysis.interval;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class IntervalLattice implements BaseLattice<IntervalLattice> {

    private final Integer low;
    private final Integer high;

    public static final IntervalLattice TOP = new IntervalLattice(null, null);
    public static final IntervalLattice BOTTOM = new IntervalLattice(1, 0);

    public IntervalLattice(Integer low, Integer high) {
        // Нормализация: если границы перепутаны, это BOTTOM
        if (low != null && high != null && low > high) {
            this.low = 1;
            this.high = 0;
        } else {
            this.low = low;
            this.high = high;
        }
    }

    public Integer getLow() { return low; }
    public Integer getHigh() { return high; }

    @Override
    public IntervalLattice top() { return TOP; }

    @Override
    public IntervalLattice bottom() { return BOTTOM; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalLattice that = (IntervalLattice) o;
        // Не используем isBottom() здесь, чтобы избежать рекурсии
        return Objects.equals(low, that.low) && Objects.equals(high, that.high);
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }

    @Override
    public StructuredRepresentation representation() {
        if (this.isBottom()) return Lattice.bottomRepresentation();
        if (this.isTop()) return Lattice.topRepresentation();
        return new StringRepresentation("[" + (low == null ? "-inf" : low) + ", " + (high == null ? "+inf" : high) + "]");
    }

    @Override
    public IntervalLattice lubAux(IntervalLattice other) throws SemanticException {
        Integer newLow = (this.low == null || other.low == null) ? null : Math.min(this.low, other.low);
        Integer newHigh = (this.high == null || other.high == null) ? null : Math.max(this.high, other.high);
        return new IntervalLattice(newLow, newHigh);
    }

    @Override
    public boolean lessOrEqualAux(IntervalLattice other) throws SemanticException {
        boolean lowOk = other.low == null || (this.low != null && this.low >= other.low);
        boolean highOk = other.high == null || (this.high != null && this.high <= other.high);
        return lowOk && highOk;
    }
}