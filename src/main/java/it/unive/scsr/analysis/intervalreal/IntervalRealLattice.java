package it.unive.scsr.analysis.intervalreal;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;


/**
 * IntervalRealLattice Class
 * 
 * An interval lattice for real numbers, where each element is an interval [l, u] with l and u being MathNumbers.
 * The lattice supports the standard operations of lub, glb, widening, and comparison.
 * The top element is represented by the interval [-∞, +∞], and the bottom element is represented by an interval with null bounds.  
 * 
 * @author Gianmaria Pizzo 872966
 */
public class IntervalRealLattice implements BaseLattice<IntervalRealLattice>, Comparable<IntervalRealLattice> {

    protected final MathNumber low;
    protected final MathNumber high;
    public static final IntervalRealLattice TOP = new IntervalRealLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static final IntervalRealLattice BOTTOM = new IntervalRealLattice(null, null);

    /**
     * Constructor for the IntervalRealLattice class.
     * 
     * Creates a new interval lattice element with the specified lower and upper bounds.
     * 
     * @param low the lower bound of the interval
     * @param high the upper bound of the interval
     */
    public IntervalRealLattice(MathNumber low, MathNumber high) {
        this.low = low;
        this.high = high;
    }

    /**
     * Constructor for the IntervalRealLattice class.
     * 
     * Creates a new interval lattice element with the specified lower and upper bounds as doubles.
     * 
     * @param low the lower bound of the interval
     * @param high the upper bound of the interval
     */
    public IntervalRealLattice(double low, double high) {
        this.low = new MathNumber(low);
        this.high = new MathNumber(high);
    }

    public IntervalRealLattice() {
        this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    }

    /**
     * Returns the top element of the lattice, representing the interval (-∞, +∞).
     * 
     * @return the top element of the lattice
     */
    @Override
    public IntervalRealLattice top() {
        return TOP;
    }

    /**
     * Returns the bottom element of the lattice, representing an empty interval.
     * 
     * @return the bottom element of the lattice
     */
    @Override
    public IntervalRealLattice bottom() {
        return BOTTOM;
    }

    /**
     * Gets the lower bound of the interval.
     *
     * @return the lower bound of the interval
     */
    public MathNumber getLow() {
        return this.low;
    }

    /**
     * Gets the upper bound of the interval.
     *
     * @return the upper bound of the interval
     */
    public MathNumber getHigh() {
        return this.high;
    }

    /**
     * Checks if the interval is the bottom element.
     *
     * @return true if the interval is the bottom element, false otherwise
     */
    @Override
    public boolean isBottom() {
        return this.low == null && this.high == null;
    }

    /**
     * Checks if the interval is the top element.
     *
     * @return true if the interval is the top element, false otherwise
     */
    @Override
    public boolean isTop() {
        return this.low != null && this.low.isMinusInfinity() && this.high != null && this.high.isPlusInfinity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLow(), getHigh());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()){
            return false;
        }

        IntervalRealLattice that = (IntervalRealLattice) obj;

        return (
            Objects.equals(getLow(), that.getLow()) && 
            Objects.equals(getHigh(), that.getHigh())
        );
    }

    @Override
    public int compareTo(IntervalRealLattice obj) {
        if (this.equals(obj)) return 0;
        if (this.isBottom()) return -1;
        if (obj.isBottom()) return 1;
        if (this.isTop()) return obj.isTop() ? 0 : 1;
        if (obj.isTop()) return -1;
        int cmp = this.getLow().compareTo(obj.getLow());
        return cmp != 0 ? cmp : this.getHigh().compareTo(obj.getHigh());
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom()){
            return Lattice.bottomRepresentation();
        }

        if (isTop()){ 
            return Lattice.topRepresentation();
        }

        String lo = getLow().isMinusInfinity() ? "-∞" : getLow().toString();
        String hi = getHigh().isPlusInfinity() ? "+∞" : getHigh().toString();
        return new StringRepresentation("[" + lo + ", " + hi + "]");
    }

    @Override
    public IntervalRealLattice lubAux(IntervalRealLattice other) throws SemanticException {
        return new IntervalRealLattice(getLow().min(other.getLow()), getHigh().max(other.getHigh()));
    }

    @Override
    public IntervalRealLattice glbAux(IntervalRealLattice other) throws SemanticException {
        MathNumber maxLow = getLow().max(other.getLow());
        MathNumber minHigh = getHigh().min(other.getHigh());
        if (maxLow.gt(minHigh)) return BOTTOM;
        return new IntervalRealLattice(maxLow, minHigh);
    }

    @Override
    public boolean lessOrEqualAux(IntervalRealLattice other) throws SemanticException {
        // [l1,u1] ⊑ [l2,u2]  iff  l2 ≤ l1  ∧  u1 ≤ u2
        return getLow().geq(other.getLow()) && getHigh().leq(other.getHigh());
    }

    /**
     * Widening for real-valued intervals.
     *
     * Real intervals need widening for the same reason integers do: ascending
     * chains like [0,0] ⊑ [0,0.1] ⊑ [0,0.2] ⊑ … are infinite.  Unlike
     * integers we cannot bound the number of distinct values inside [a,b]
     * (there are uncountably many), so we must force diverging bounds to ±∞
     * immediately rather than relying on any finite-chain argument.
     *
     * Rule:  (this) ∇ (other)
     *   lower = other.low  < this.low  ? -∞ : this.low
     *   upper = other.high > this.high ? +∞ : this.high
     */
    @Override
    public IntervalRealLattice wideningAux(IntervalRealLattice other) throws SemanticException {
        MathNumber newLow  = other.getLow().lt(getLow())   ? MathNumber.MINUS_INFINITY : getLow();
        MathNumber newHigh = other.getHigh().gt(getHigh()) ? MathNumber.PLUS_INFINITY  : getHigh();
        return new IntervalRealLattice(newLow, newHigh);
    }

}