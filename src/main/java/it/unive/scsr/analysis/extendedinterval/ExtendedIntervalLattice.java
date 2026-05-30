package it.unive.scsr.analysis.extendedinterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedIntervalLattice implements BaseLattice<ExtendedIntervalLattice> {

    public final double low;
    public final double high;
    private static final double EPSILON = 0.0001;

    public static final ExtendedIntervalLattice TOP =
            new ExtendedIntervalLattice(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public static final ExtendedIntervalLattice BOTTOM =
            new ExtendedIntervalLattice(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);

    public static final ExtendedIntervalLattice ZERO =
            new ExtendedIntervalLattice(0.0, 0.0);

    public ExtendedIntervalLattice(double low, double high) {
        if (Double.isNaN(low) || Double.isNaN(high)){
            this.low = Double.POSITIVE_INFINITY;
            this.high = Double.NEGATIVE_INFINITY;
            return;
        }
        this.low = low;
        this.high = high;
    }

    public boolean isBottom() {
        return low > high;
    }

    public boolean isTop() {
        return low == Double.NEGATIVE_INFINITY
                && high == Double.POSITIVE_INFINITY;
    }

    @Override
    public ExtendedIntervalLattice top() {
        return TOP;
    }

    @Override
    public ExtendedIntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom())
            return Lattice.bottomRepresentation();
        if (isTop())
            return Lattice.topRepresentation();

        return new StringRepresentation("[" + low + ", " + high + "]");
    }

    @Override
    public ExtendedIntervalLattice lubAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.isBottom())
            return other;
        if (other.isBottom())
            return this;

        double newLow = Math.min(this.low, other.low);
        double newHigh = Math.max(this.high, other.high);

        return new ExtendedIntervalLattice(newLow, newHigh);
    }

    @Override
    public ExtendedIntervalLattice glbAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.isBottom() || other.isBottom())
            return BOTTOM;

        double newLow = Math.max(this.low, other.low);
        double newHigh = Math.min(this.high, other.high);

        if (newLow > newHigh)
            return BOTTOM;

        return new ExtendedIntervalLattice(newLow, newHigh);
    }

    @Override
    public boolean lessOrEqualAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.isBottom())
            return true;
        if (other.isBottom())
            return false;

        return other.low <= this.low && this.high <= other.high;
    }

    @Override
    public ExtendedIntervalLattice wideningAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.isBottom())
            return other;
        if (other.isBottom())
            return this;

        double newLow = this.low;
        double newHigh = this.high;

        if (other.low < this.low - EPSILON)
            newLow = Double.NEGATIVE_INFINITY;

        if (other.high > this.high + EPSILON)
            newHigh = Double.POSITIVE_INFINITY;

        return new ExtendedIntervalLattice(newLow, newHigh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ExtendedIntervalLattice other = (ExtendedIntervalLattice) obj;
        return Double.compare(low, other.low) == 0 && Double.compare(high, other.high) == 0;
    }

    @Override
    public String toString(){
        return representation().toString();
    }

    public Satisfiability eq(
            ExtendedIntervalLattice other) {

        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;

        if (this.high < other.low
                || other.high < this.low)
            return Satisfiability.NOT_SATISFIED;

        if (this.low == this.high
                && other.low == other.high
                && this.low == other.low)
            return Satisfiability.SATISFIED;

        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(
            ExtendedIntervalLattice other) {

        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;

        if (this.low > other.high)
            return Satisfiability.SATISFIED;

        if (this.high <= other.low)
            return Satisfiability.NOT_SATISFIED;

        return Satisfiability.UNKNOWN;
    }
}