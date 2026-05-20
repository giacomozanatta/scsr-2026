package it.unive.scsr.analysis.intervalreal;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.combination.constraints.WholeValueElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.iterators.EmptyIterator;

/**
 * FloatInterval Class
 * 
 * An implementation of an interval domain for floating-point numbers, 
 *  where each element is an interval [l, u] with l and u being MathNumbers representing
 *  the lower and upper bounds of the interval, respectively. 
 * 
 * The class supports basic arithmetic operations (addition, subtraction, multiplication, division) 
 *  and handles special cases like division by zero by returning TOP. 
 *  It also includes methods for checking if the interval is infinite, 
 *  finite, a singleton, or includes another interval. 
 *
 * @note This implementation follows closely the structure of {@link it.unive.lisa.util.numeric.IntInterval}
 * 
 * @author Gianmaria Pizzo 872966
 */
public class FloatInterval implements Iterable<Long>, Comparable<FloatInterval>, WholeValueElement<FloatInterval>, BaseLattice<FloatInterval> {
    public static final FloatInterval INFINITY = new FloatInterval();
    public static final FloatInterval ZERO = new FloatInterval(0.0f, 0.0f);
    public static final FloatInterval ONE = new FloatInterval(1.0f, 1.0f);
    public static final FloatInterval MINUS_ONE = new FloatInterval(-1.0f, -1.0f);
    public static final FloatInterval NaN = new FloatInterval(MathNumber.NaN, MathNumber.NaN);
    public static final FloatInterval TOP = INFINITY;
    public static final FloatInterval BOTTOM = new FloatInterval((Float)null, (Float)null);
    private final MathNumber low;
    private final MathNumber high;

    // Constructors

    /**
     * Empty Constructor for the FloatInterval class.
     * 
     * Creates a default float interval representing the top 
     * element of the lattice, which is the interval [-∞, +∞].
     * 
     * @note This constructor is private to prevent the creation 
     *  of additional instances of the top element, as the top 
     *  element is already represented by the static field INFINITY.
     */
    private FloatInterval() {
        this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    }

    public FloatInterval(float low, float high) {
        this(new MathNumber((double)low), new MathNumber((double)high));
    }

    public FloatInterval(Float low, Float high) {
        this(handleNulls(low, high, true), handleNulls(low, high, false));
    }

    private static MathNumber handleNulls(Float low, Float high, boolean isLowBound) {
        if (low == null && high == null) {
            return null;
        } else if (isLowBound) {
            return low == null ? MathNumber.MINUS_INFINITY : new MathNumber((double)low);
        } else {
            return high == null ? MathNumber.PLUS_INFINITY : new MathNumber((double)high);
        }
    }

    public FloatInterval(MathNumber low, MathNumber high) {
        if (low == null && high == null) {
            this.low = null;
            this.high = null;
        } else {
            Objects.requireNonNull(low, "Low bound must not be null");
            Objects.requireNonNull(high, "High bound must not be null");
            if (!low.isNaN() && !high.isNaN()) {
                if (low.compareTo(high) <= 0) {
                this.low = low;
                this.high = high;
                } else {
                this.low = high;
                this.high = low;
                }
            } else {
                this.low = MathNumber.NaN;
                this.high = MathNumber.NaN;
            }
        }
    }

    // Lattice Methods

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + (this.getHigh() == null ? 0 : this.getHigh().hashCode());
        result = prime * result + (this.getLow() == null ? 0 : this.getLow().hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        } else {
            FloatInterval other = (FloatInterval)obj;
            if (this.high == null) {
                if (other.high != null) {
                    return false;
                }
            } else if (!this.high.equals(other.high)) {
                return false;
            }

            if (this.low == null) {
                if (other.low != null) {
                    return false;
                }
            } else if (!this.low.equals(other.low)) {
                return false;
            }

            return true;
        }
    }

    public StructuredRepresentation representation() {
        if (this.isBottom()) {
            return Lattice.bottomRepresentation();
        }
        if (this.isTop()) {
            return Lattice.topRepresentation();
        }
        if (this.getLow().isNaN()) {
            return new StringRepresentation("NaN");
        }
        String lo = this.getLow().isMinusInfinity() ? "-∞" : this.getLow().toString();
        String hi = this.getHigh().isPlusInfinity() ? "+∞" : this.getHigh().toString();

        // TODO: Add 1e-6 precision to the string representation to avoid confusion with integers
        return new StringRepresentation("[" + lo + ", " + hi + "]");
    }

    public String toString() { 
        return this.representation().toString(); 
    }

    // Getters and Utility Methods

    public MathNumber getHigh() {
        return this.high;
    }

    public MathNumber getLow() {
        return this.low;
    }

    public FloatInterval top() { 
        return TOP; 
    }

    public boolean isTop() { 
        return this.lowIsMinusInfinity() && this.highIsPlusInfinity(); 
    }

    public FloatInterval bottom() { 
        return BOTTOM; 
    }

    public boolean isBottom() { 
        return this.getLow() == null && this.getHigh() == null; 
    }

    public boolean lowIsMinusInfinity() {
        return !this.isBottom() && this.getLow().isMinusInfinity();
    }

    public boolean highIsPlusInfinity() {
        return !this.isBottom() && this.getHigh().isPlusInfinity();
    }

    public boolean isInfinite() {
        return !this.isBottom() && (this == INFINITY || this.highIsPlusInfinity() || this.lowIsMinusInfinity());
    }

    public boolean isFinite() {
        return !this.isBottom() && !this.isInfinite();
    }

    public boolean isInfinity() {
        return this == INFINITY;
    }

    public boolean isSingleton() {
        return this.isFinite() && this.getLow().equals(this.getHigh());
    }

    // TODO: Use compare to instead?
    public boolean is(int n) {
        return !this.isBottom() && this.isSingleton() && this.getLow().is(n);
    }

    // FIXME: The logic should implement some rounding to the nearest representable float, but for now we just cache the result to avoid creating too many objects 
    private static FloatInterval cacheAndRound(FloatInterval i) {
        return i;
    }

    // Arithmetic Operations

    public FloatInterval plus(FloatInterval other) {
        // If either interval is bottom, the result is bottom
        if (this.isBottom() || other.isBottom()){
            return BOTTOM;
        } 
        else if (this.isInfinity() || other.isInfinity()) {
            return INFINITY;
        } 
        else {
            FloatInterval result = new FloatInterval(
                this.getLow().add(other.getLow()), 
                this.getHigh().add(other.getHigh())
            );

            return cacheAndRound(result);
        }
    }

    public FloatInterval diff(FloatInterval other) {
        if (this.isBottom() || other.isBottom()) {
            return BOTTOM;
        } 
        else if (this.isInfinity() || other.isInfinity()) {
            return INFINITY;
        } 
        else {
            FloatInterval result = new FloatInterval(
                this.getLow().subtract(other.getHigh()),
                this.getHigh().subtract(other.getLow())
            );

            return cacheAndRound(result);
        }
    }

    private static MathNumber min(MathNumber... nums) {
        if (nums.length == 0) {
            throw new IllegalArgumentException("No numbers provided");
        } else {
            MathNumber min = nums[0];

            for(int i = 1; i < nums.length; ++i) {
                min = min.min(nums[i]);
            }

            return min;
        }
    }

    private static MathNumber max(MathNumber... nums) {
        if (nums.length == 0) {
            throw new IllegalArgumentException("No numbers provided");
        } else {
            MathNumber max = nums[0];

            for(int i = 1; i < nums.length; ++i) {
                max = max.max(nums[i]);
            }

            return max;
        }
    }

    public FloatInterval mul(FloatInterval other) {
        if (!this.isBottom() && !other.isBottom()) {
            if (!this.is(0) && !other.is(0)) {
                if (!this.isInfinity() && !other.isInfinity()) {
                    if (this.getLow().compareTo(MathNumber.ZERO) >= 0 && other.getLow().compareTo(MathNumber.ZERO) >= 0) {
                        return cacheAndRound(new FloatInterval(this.getLow().multiply(other.getLow()), this.getHigh().multiply(other.getHigh())));
                    } else {
                        MathNumber ll = this.getLow().multiply(other.getLow());
                        MathNumber lh = this.getLow().multiply(other.getHigh());
                        MathNumber hl = this.getHigh().multiply(other.getLow());
                        MathNumber hh = this.getHigh().multiply(other.getHigh());
                        return cacheAndRound(new FloatInterval(min(ll, lh, hl, hh), max(ll, lh, hl, hh)));
                    }
                } else {
                    return INFINITY;
                }
            } else {
                return ZERO;
            }
        } else {
            return BOTTOM;
        }
    }

    public FloatInterval div(FloatInterval other, boolean ignoreZero, boolean errorOnZero) {
        if (!this.isBottom() && !other.isBottom()) {
            if (!errorOnZero || !other.is(0) && !other.includes(ZERO)) {
                if (this.is(0)) {
                    return ZERO;
                } else if (other.is(0)) {
                    return TOP;
                } else if (!other.includes(ZERO)) {
                    return this.mul(new FloatInterval(MathNumber.ONE.divide(other.high), MathNumber.ONE.divide(other.low)));
                } else if (other.high.isZero()) {
                    return this.mul(new FloatInterval(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(other.low)));
                } else if (other.low.isZero()) {
                    return this.mul(new FloatInterval(MathNumber.ONE.divide(other.high), MathNumber.PLUS_INFINITY));
                } else if (ignoreZero) {
                    return this.mul(new FloatInterval(MathNumber.ONE.divide(other.low), MathNumber.ONE.divide(other.high)));
                } else {
                    FloatInterval lower = this.mul(new FloatInterval(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(other.low)));
                    FloatInterval higher = this.mul(new FloatInterval(MathNumber.ONE.divide(other.high), MathNumber.PLUS_INFINITY));
                    if (lower.includes(higher)) {
                        return lower;
                    } else {
                        return higher.includes(lower) ? higher : cacheAndRound(new FloatInterval(lower.low.compareTo(higher.low) > 0 ? higher.low : lower.low, lower.high.compareTo(higher.high) < 0 ? higher.high : lower.high));
                    }
                }
            } else {
                throw new ArithmeticException("FloatInterval divide by zero");
            }
        } else {
            return BOTTOM;
        }
    }

    // Set Operations

    public boolean includes(FloatInterval other) {
        if (!this.isBottom() && !other.isBottom()) {
            return this.low.compareTo(other.low) <= 0 && this.high.compareTo(other.high) >= 0;
        } else {
            return false;
        }
    }

    public boolean intersects(FloatInterval other) {
        if (!this.isBottom() && !other.isBottom()) {
            return this.includes(other) || other.includes(this) || this.high.compareTo(other.low) >= 0 && this.high.compareTo(other.high) <= 0 || other.high.compareTo(this.low) >= 0 && other.high.compareTo(this.high) <= 0;
        } else {
            return false;
        }
    }

    // Real intervals are uncountable; enumeration not supported
    public Iterator<Long> iterator() {
        return EmptyIterator.emptyIterator();
    }

    public int compareTo(FloatInterval o) {
        if (this.isBottom()) {
            return o.isBottom() ? 0 : -1;
        } else if (this.isTop()) {
            return o.isTop() ? 0 : 1;
        } else if (o.isBottom()) {
            return 1;
        } else if (o.isTop()) {
            return -1;
        } else {
            int cmp;
            return (cmp = this.low.compareTo(o.low)) != 0 ? cmp : this.high.compareTo(o.high);
        }
    }

    public FloatInterval lubAux(FloatInterval other) throws SemanticException {
        MathNumber newLow = this.getLow().min(other.getLow());
        MathNumber newHigh = this.getHigh().max(other.getHigh());
        return newLow.isMinusInfinity() && newHigh.isPlusInfinity() ? this.top() : new FloatInterval(newLow, newHigh);
    }

    public FloatInterval glbAux(FloatInterval other) {
        MathNumber newLow = this.getLow().max(other.getLow());
        MathNumber newHigh = this.getHigh().min(other.getHigh());
        if (newLow.compareTo(newHigh) > 0) {
            return this.bottom();
        } else {
            return newLow.isMinusInfinity() && newHigh.isPlusInfinity() ? this.top() : new FloatInterval(newLow, newHigh);
        }
    }

    public FloatInterval wideningAux(FloatInterval other) throws SemanticException {
        MathNumber newHigh = other.getHigh().compareTo(this.getHigh()) > 0 ? MathNumber.PLUS_INFINITY : this.getHigh();
        MathNumber newLow = other.getLow().compareTo(this.getLow()) < 0 ? MathNumber.MINUS_INFINITY : this.getLow();
        return newLow.isMinusInfinity() && newHigh.isPlusInfinity() ? this.top() : new FloatInterval(newLow, newHigh);
    }

    public FloatInterval narrowingAux(FloatInterval other) throws SemanticException {
        MathNumber newHigh = this.getHigh().isInfinite() ? other.getHigh() : this.getHigh();
        MathNumber newLow = this.getLow().isInfinite() ? other.getLow() : this.getLow();
        return new FloatInterval(newLow, newHigh);
    }

    public boolean lessOrEqualAux(FloatInterval other) throws SemanticException {
        return other.includes(this);
    }

    public Set<BinaryExpression> constraints(ValueExpression e, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    public FloatInterval generate(Set<BinaryExpression> constraints, ProgramPoint pp) throws SemanticException {
        if (constraints == null) {
            return this.bottom();
        } else {
            return TOP;
        }
    }

}