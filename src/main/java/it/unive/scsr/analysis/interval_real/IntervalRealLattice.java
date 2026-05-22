package it.unive.scsr.analysis.interval_real;

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
    protected final int wideningCounter; 
    protected final MathNumber low;
    protected final MathNumber high;

    public static final MathNumber ONE = new MathNumber(1.0f);
    public static final IntervalRealLattice TOP = new IntervalRealLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY, 0);
    public static final IntervalRealLattice BOTTOM = new IntervalRealLattice(null, null, 0);

    public IntervalRealLattice(MathNumber low, MathNumber high, int wideningCounter) {
        this.wideningCounter = wideningCounter;
        if (low != null && high != null && low.compareTo(high) > 0) {
            this.low = high;
            this.high = low;
        } else {
            this.low = low;
            this.high = high;
        }
    }

    public IntervalRealLattice(MathNumber low, MathNumber high) {
        this(low, high, 0); // Di base parte da 0
    }

    public IntervalRealLattice(double low, double high) {
        this(new MathNumber(low), new MathNumber(high), 0);
    }

    public IntervalRealLattice() {
        this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY, 0);
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
     * Gets the widening counter.
     *
     * @return the widening counter
     */
    public int getWideningCounter() {
        return this.wideningCounter;
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

        IntervalRealLattice other = (IntervalRealLattice) obj;

        return (
            Objects.equals(getLow(), other.getLow()) && 
            Objects.equals(getHigh(), other.getHigh())
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

    // TODO: How to deal with widening counter and these two methods?

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

        // Check if the resulting interval is valid (maxLow <= minHigh)
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

    // TODO: Sound but loss of precision, we can try to use small increments...
    @Override
    public IntervalRealLattice wideningAux(IntervalRealLattice other) throws SemanticException {
        System.out.println(
            "Widening called with: " + this.representation() + " and " + 
            other.representation() + " (counter: " + this.getWideningCounter() + ")"
        );

        if (this.isBottom() || other.isBottom()) {
            return bottom();
        }

        // Increment the widening counter
        int nextCounter = this.getWideningCounter() + 1;

        System.out.println("Widening counter incremented to: " + nextCounter);

        // Get bounds of both intervals
        MathNumber l1 = this.getLow();
        MathNumber u1 = this.getHigh();
        MathNumber l2 = other.getLow();
        MathNumber u2 = other.getHigh();

        // Default to current upper bound
        MathNumber newUpper = u1;
        // Default to current lower bound
        MathNumber newLower = l1;

        // Handle upper bound
        if (u2.gt(u1)) {
            System.out.println("Upper bound increased from " + u1 + " to " + u2);
            // If we have reached 5 calls to widening
            if (nextCounter >= 5) {
                System.out.println("Reached 5 iterations, widening upper bound to +∞");
                // Widen to +INF
                newUpper = MathNumber.PLUS_INFINITY;
            } else {
                System.out.println("Within 5 iterations, checking difference for upper bound");

                // If we are within the first 5 calls
                // Calculate the difference between the upper bounds
                MathNumber diffUpper = u2.subtract(u1);

                System.out.println("Difference between upper bounds: " + diffUpper);

                // If the difference is less than 1 unit
                if (diffUpper.lt(ONE)) {
                    // We (safely) round up to the next integer
                    newUpper = u2.roundUp(); 
                    System.out.println("Difference is less than 1, rounding up to next integer: " + newUpper);
                } else {
                    System.out.println("Significant increase detected, widening upper bound to +∞");
                    // We have a significant increase, so we widen immediately to +INF
                    newUpper = MathNumber.PLUS_INFINITY;
                }
            }
        }

        // Handle lower bound
        if (l2.lt(l1)) {
            System.out.println("Lower bound decreased from " + l1 + " to " + l2);

            if (nextCounter >= 5) {
                System.out.println("Reached 5 iterations, widening lower bound to -∞");
                // If we have reached 5 iterations, we widen to -INF
                newLower = MathNumber.MINUS_INFINITY;
            } else {
                System.out.println("Within 5 iterations, checking difference for lower bound");

                // If we are within the first 5 calls
                // Calculate the difference between the lower bounds
                MathNumber diffLower = l1.subtract(l2);

                System.out.println("Difference between lower bounds: " + diffLower);

                // If the difference is less than 1 unit
                if (diffLower.lt(ONE)) {
                    // We (safely) round down to the previous integer
                    newLower = l2.roundDown();
                    System.out.println("Difference is less than 1, rounding down to previous integer: " + newLower);
                } else {
                    System.out.println("Significant decrease detected, widening lower bound to -∞");
                    // We have a significant decrease, so we widen immediately to -INF
                    newLower = MathNumber.MINUS_INFINITY;
                }
            }
        }

        // We reset the counter if we have reached 5 calls
        if (nextCounter >= 5) {
            nextCounter = 0;
            System.out.println("Counter reset to 0 after reaching 5 iterations");
        }

        // As we keep track of iteration count, we pass it to the constructor
        return new IntervalRealLattice(newLower, newUpper, nextCounter);
    }

}