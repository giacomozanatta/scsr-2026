package it.unive.scsr.analysis.floatInterval;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/**
 * Lattice for the Float Interval domain.
 *
 * Elements represent closed real intervals [low, high] where low, high ∈ ℝ ∪ {-∞, +∞}.
 * Special elements:
 *   TOP    = [-∞, +∞]
 *   BOTTOM = ∅  (represented by low > high, here stored as null bounds)
 *
 * Key difference from IntervalLattice (integer):
 *   - Bounds are double, so [0.0, 1.0] contains infinitely many values.
 *   - The widening operator cannot simply go to ±∞ after one step;
 *     instead it uses a finite threshold set to ensure termination
 *     while avoiding immediate precision loss.
 *
 * Widening strategy:
 *   We use a set of thresholds T = {-∞, -1.0, 0.0, 1.0, +∞} (configurable).
 *   For each bound:
 *     - If the new bound is strictly outside the old one, jump to the nearest
 *       threshold in the "worsening" direction.
 *   This mirrors the standard integer widening but adapted for ℝ:
 *   after at most |T|-1 widenings the bound reaches ±∞, guaranteeing termination.
 */
public class FloatIntervalLattice implements BaseLattice<FloatIntervalLattice> {

    // -----------------------------------------------------------------------
    // Sentinel values for ±∞
    // -----------------------------------------------------------------------

    public static final double NEG_INF = Double.NEGATIVE_INFINITY;
    public static final double POS_INF = Double.POSITIVE_INFINITY;

    // -----------------------------------------------------------------------
    // Widening thresholds (sorted ascending)
    // Used to bound the number of widening steps for real-valued intervals.
    // Can be extended with domain-specific constants found in the analysed program.
    // -----------------------------------------------------------------------

    private static final double[] WIDENING_THRESHOLDS = {
            NEG_INF, -1000.0, -100.0, -10.0, -1.0, 0.0, 1.0, 10.0, 100.0, 1000.0, POS_INF
    };

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    /** Lower bound of the interval; Double.NaN signals BOTTOM. */
    private final double low;

    /** Upper bound of the interval; Double.NaN signals BOTTOM. */
    private final double high;

    /** True iff this element represents ∅ (BOTTOM). */
    private final boolean isBottom;

    // -----------------------------------------------------------------------
    // Singletons
    // -----------------------------------------------------------------------

    public static final FloatIntervalLattice TOP    = new FloatIntervalLattice(NEG_INF, POS_INF);
    public static final FloatIntervalLattice BOTTOM = new FloatIntervalLattice();
    public static final FloatIntervalLattice ZERO   = new FloatIntervalLattice(0.0, 0.0);

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /** Constructs BOTTOM. */
    public FloatIntervalLattice() {
        this.low      = Double.NaN;
        this.high     = Double.NaN;
        this.isBottom = true;
    }

    /**
     * Constructs the interval [low, high].
     * If low > high the result is BOTTOM (empty interval).
     */
    public FloatIntervalLattice(double low, double high) {
        if (Double.isNaN(low) || Double.isNaN(high) || low > high) {
            this.low      = Double.NaN;
            this.high     = Double.NaN;
            this.isBottom = true;
        } else {
            this.low      = low;
            this.high     = high;
            this.isBottom = false;
        }
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public double getLow()  { return low;  }
    public double getHigh() { return high; }

    // -----------------------------------------------------------------------
    // BaseLattice
    // -----------------------------------------------------------------------

    @Override
    public FloatIntervalLattice top() {
        return TOP;
    }

    @Override
    public FloatIntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isBottom() {
        return isBottom;
    }

    @Override
    public boolean isTop() {
        return !isBottom && low == NEG_INF && high == POS_INF;
    }

    // -----------------------------------------------------------------------
    // LUB  (join) – smallest interval containing both
    // -----------------------------------------------------------------------

    @Override
    public FloatIntervalLattice lubAux(FloatIntervalLattice other) throws SemanticException {
        double newLow  = Math.min(this.low,  other.low);
        double newHigh = Math.max(this.high, other.high);
        return new FloatIntervalLattice(newLow, newHigh);
    }

    // -----------------------------------------------------------------------
    // GLB  (meet) – largest interval contained in both
    // -----------------------------------------------------------------------

    @Override
    public FloatIntervalLattice glbAux(FloatIntervalLattice other) throws SemanticException {
        double newLow  = Math.max(this.low,  other.low);
        double newHigh = Math.min(this.high, other.high);
        // If newLow > newHigh the constructor returns BOTTOM automatically
        return new FloatIntervalLattice(newLow, newHigh);
    }

    // -----------------------------------------------------------------------
    // Ordering  (this ≤ other  iff  [this] ⊆ [other])
    // -----------------------------------------------------------------------

    @Override
    public boolean lessOrEqualAux(FloatIntervalLattice other) throws SemanticException {
        return other.low <= this.low && this.high <= other.high;
    }

    // -----------------------------------------------------------------------
    // Widening  –  the key novelty for real-valued intervals
    //
    // Standard integer widening:
    //   w(X, Y) = [ Y.low < X.low  ? -∞ : X.low,
    //               Y.high > X.high ? +∞ : X.high ]
    //
    // Problem for reals:
    //   [0,1] → [0, 0.5] → [0, 0.25] → ...  never stabilises under integer widening
    //   because 0.25 < 1 does not trigger the jump to +∞.
    //   The interval [0,1] already has X.high = 1 ≥ 0.25 = Y.high, so integer
    //   widening would keep [0,1] forever — but a shrinking sequence is fine.
    //   The real issue is GROWING sequences: [0,1] → [0,2] → [0,4] → ...
    //
    // Solution – threshold widening:
    //   For each bound, if it moves in the worsening direction (lower bound
    //   decreases OR upper bound increases) we snap it to the nearest
    //   threshold instead of directly to ±∞.
    //   This allows a controlled number of widening steps before reaching ±∞,
    //   trading some precision for guaranteed termination.
    // -----------------------------------------------------------------------

    @Override
    public FloatIntervalLattice wideningAux(FloatIntervalLattice other) throws SemanticException {
        double newLow  = widenLow(this.low,  other.low);
        double newHigh = widenHigh(this.high, other.high);
        return new FloatIntervalLattice(newLow, newHigh);
    }

    /**
     * Widens the lower bound.
     * If the new lower bound is smaller than the old one (growing downward),
     * jump to the largest threshold that is still ≤ newLow.
     * Otherwise keep the old lower bound.
     */
    private static double widenLow(double oldLow, double newLow) {
        if (newLow < oldLow) {
            // Find the largest threshold t such that t <= newLow
            double threshold = NEG_INF;
            for (double t : WIDENING_THRESHOLDS) {
                if (t <= newLow) threshold = t;
                else break;
            }
            return threshold;
        }
        return oldLow; // stable or growing upward → keep
    }

    /**
     * Widens the upper bound.
     * If the new upper bound is larger than the old one (growing upward),
     * jump to the smallest threshold that is still ≥ newHigh.
     * Otherwise keep the old upper bound.
     */
    private static double widenHigh(double oldHigh, double newHigh) {
        if (newHigh > oldHigh) {
            // Find the smallest threshold t such that t >= newHigh
            double threshold = POS_INF;
            for (int i = WIDENING_THRESHOLDS.length - 1; i >= 0; i--) {
                double t = WIDENING_THRESHOLDS[i];
                if (t >= newHigh) threshold = t;
                else break;
            }
            return threshold;
        }
        return oldHigh; // stable or shrinking → keep
    }

    /**
     * Returns true if this interval contains zero.
     */
    public boolean containsZero() {
        return !isBottom && low <= 0.0 && 0.0 <= high;
    }

    /**
     * Returns true if this interval is strictly positive (low > 0).
     */
    public boolean isStrictlyPositive() {
        return !isBottom && low > 0.0;
    }

    /**
     * Returns true if this interval is strictly negative (high < 0).
     */
    public boolean isStrictlyNegative() {
        return !isBottom && high < 0.0;
    }

    // -----------------------------------------------------------------------
    // Representation
    // -----------------------------------------------------------------------

    @Override
    public StructuredRepresentation representation() {
        if (isBottom) return Lattice.bottomRepresentation();
        String lo = (low  == NEG_INF) ? "-∞" : Double.toString(low);
        String hi = (high == POS_INF) ? "+∞" : Double.toString(high);
        return new StringRepresentation("[" + lo + ", " + hi + "]");
    }

    // -----------------------------------------------------------------------
    // equals / hashCode
    // -----------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FloatIntervalLattice)) return false;
        FloatIntervalLattice other = (FloatIntervalLattice) obj;
        if (isBottom && other.isBottom) return true;
        if (isBottom || other.isBottom) return false;
        return Double.compare(low, other.low) == 0
                && Double.compare(high, other.high) == 0;
    }

    @Override
    public int hashCode() {
        return isBottom ? Objects.hash(Boolean.TRUE)
                : Objects.hash(low, high);
    }

    @Override
    public String toString() {
        return representation().toString();
    }
}