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
        if (low != null && high != null && low.compareTo(high) > 0) {
            // Automatically swap inverted bounds safely
            this.low = high;
            this.high = low;
        } else {
            this.low = low;
            this.high = high;
        }
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
        return this.getLow() == null && this.getHigh() == null;
    }

    /**
     * Checks if the interval is the top element.
     *
     * @return true if the interval is the top element, false otherwise
     */
    @Override
    public boolean isTop() {
        return this.getLow() != null && this.getLow().isMinusInfinity() && this.getHigh() != null && this.getHigh().isPlusInfinity();
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
    public int compareTo(IntervalRealLattice obj) {
        if(isBottom()){
            return obj.isBottom() ? 0 : -1; 
        }

        if(isTop()){
            return obj.isTop() ? 0 : 1;
        }

        if(obj.isBottom()){
            return 1;
        }

        if(obj.isTop()){
            return -1;
        }

        int cmp = this.getLow().compareTo(obj.getLow());
        return cmp != 0 ? cmp : this.getHigh().compareTo(obj.getHigh());
    }

    @Override
    public IntervalRealLattice lubAux(IntervalRealLattice other) throws SemanticException {
        if (this.isBottom() || other.isBottom()) {
            return bottom();
        }

        // Get the minimum of the lower bounds
        MathNumber newLow = getLow().min(other.getLow());
        // Get the maximum of the upper bounds
        MathNumber newHigh = getHigh().max(other.getHigh());

        return new IntervalRealLattice(newLow, newHigh);
    }

    @Override
    public IntervalRealLattice glbAux(IntervalRealLattice other) throws SemanticException {
        if (this.isBottom() || other.isBottom()) {
            return bottom();
        }

        // Get the maximum of the lower bounds
        MathNumber maxLow = getLow().max(other.getLow());

        // Get the minimum of the upper bounds
        MathNumber minHigh = getHigh().min(other.getHigh());

        // Check if the resulting interval is valid (maxLow ≤ minHigh)
        if (maxLow.gt(minHigh)) {
            return BOTTOM;
        }

        return new IntervalRealLattice(maxLow, minHigh);
    }

    @Override
    public boolean lessOrEqualAux(IntervalRealLattice other) throws SemanticException {
        if (this.isBottom() || other.isBottom()) {
            return false;
        }

        // Checks if 'this' is subset of 'other' (this ⊑ other)
        // [l1,u1] ⊑ [l2,u2]  iff  l2 ≤ l1  ∧  u1 ≤ u2
        // It is equivalent to check "other includes this"
        return getLow().geq(other.getLow()) && getHigh().leq(other.getHigh());
    }

    @Override
    public IntervalRealLattice wideningAux(IntervalRealLattice other) throws SemanticException {
        if (this.isBottom() || other.isBottom()) {
            return bottom();
        }

        // Small threshold margin
        MathNumber tolerance = new MathNumber(1e-4);

        // Get bounds of both intervals
        MathNumber l1 = this.getLow();
        MathNumber u1 = this.getHigh();
        MathNumber l2 = other.getLow();
        MathNumber u2 = other.getHigh();

        // Default to current upper bound
        MathNumber newUpper = u1;

        if (u2.subtract(u1).gt(tolerance)) {
            // If the new upper bound is significantly larger than 
            // the current one, we widen to +INF
            newUpper = MathNumber.PLUS_INFINITY;
        } else if (u2.gt(u1)) {
            // If the new upper bound is larger but within the tolerance    
            // we can accept it without widening
            newUpper = u2;
        }

        // Default to current lower bound
        MathNumber newLower = l1;

        if (l1.subtract(l2).gt(tolerance)) {
            // If the new lower bound is significantly smaller than 
            // the current one, we widen to -INF
            newLower = MathNumber.MINUS_INFINITY;
        } else if (l2.lt(l1)) {
            // If the new lower bound is smaller but within the tolerance
            // we can accept it without widening
            newLower = l2;
        }

        return new IntervalRealLattice(newLower, newUpper);
    }

}