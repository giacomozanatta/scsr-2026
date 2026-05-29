package it.unive.scsr.analysis.interval;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class FloatIntervalLattice implements BaseLattice<FloatIntervalLattice>, Comparable<FloatIntervalLattice> {
     
    // The two bounds were re-implemented as MathNumber and not as a single IntInterval because it covers only integers.
    public final MathNumber low;
    public final MathNumber high;

    // thresholds for the widening operator.
    private static final List<MathNumber> THRESHOLDS = Arrays.asList(
        new MathNumber(-1000L), new MathNumber(-100L), new MathNumber(-10L),
        new MathNumber(-5L), new MathNumber(-1L), new MathNumber(0L),
        new MathNumber(1L), new MathNumber(5L), new MathNumber(10L),
        new MathNumber(100L), new MathNumber(1000L)
    );

    public static final FloatIntervalLattice TOP    = new FloatIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static final FloatIntervalLattice BOTTOM = new FloatIntervalLattice(null, null);
    public static final FloatIntervalLattice ZERO   = new FloatIntervalLattice(new MathNumber(0L), new MathNumber(0L));

    public FloatIntervalLattice(MathNumber low, MathNumber high) {
        this.low  = low;
        this.high = high;
    }

    public FloatIntervalLattice(long l, long u) {
        this(new MathNumber(l), new MathNumber(u));
    }

    private FloatIntervalLattice() {
        this(null, null);
    }

	public MathNumber getLow() {
		return low;
	}

	public MathNumber getHigh() {
		return high;
	}

    public boolean isBottom() {
        return low == null || high == null;
    }

    public boolean isTop() {
        return MathNumber.MINUS_INFINITY.equals(low) && MathNumber.PLUS_INFINITY.equals(high);
    }

    @Override
    public FloatIntervalLattice top() {
        return TOP;
    }

    @Override
    public FloatIntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom())
            return Lattice.bottomRepresentation();
        return new StringRepresentation("[" + low + ", " + high + "]");
    }
    @Override
    public FloatIntervalLattice lubAux(FloatIntervalLattice other) throws SemanticException {
        if (this.isBottom()) return other;
        if (other.isBottom()) return this;

        MathNumber lResult = this.low.compareTo(other.low)   <= 0 ? this.low  : other.low;
        MathNumber uResult = this.high.compareTo(other.high) >= 0 ? this.high : other.high;

        return new FloatIntervalLattice(lResult, uResult);
    }

    @Override
    public FloatIntervalLattice glbAux(FloatIntervalLattice other) throws SemanticException {
        if (this.isBottom() || other.isBottom()) return BOTTOM;

        MathNumber lResult = this.low.compareTo(other.low)   >= 0 ? this.low  : other.low;
        MathNumber uResult = this.high.compareTo(other.high) <= 0 ? this.high : other.high;

        if (lResult.compareTo(uResult) > 0)
            return BOTTOM;

        return new FloatIntervalLattice(lResult, uResult);
    }

    @Override
    public boolean lessOrEqualAux(FloatIntervalLattice other) throws SemanticException {
        if (this.isBottom()) return true;
        if (other.isBottom()) return false;
        return other.low.compareTo(this.low) <= 0
            && this.high.compareTo(other.high) <= 0;
    }

    @Override
    public FloatIntervalLattice wideningAux(FloatIntervalLattice other) throws SemanticException {
        // This widening operator consists on comparing the elements with hard-coded thresholds, returning the least threshold that is greater than the element itself.
        if (this.isBottom()) return other;
        if (other.isBottom()) return this;

        // Every time the interval grows/shrinks, thresholds are applied to avoid infinite loops in the analysis. (ex the [0,1] infinite numbers problem)
        MathNumber lResult;
        if (other.low.compareTo(this.low) < 0) {
            lResult = MathNumber.MINUS_INFINITY;
            for (int idx = THRESHOLDS.size() - 1; idx >= 0; idx--) {
                MathNumber t = THRESHOLDS.get(idx);
                if (t.compareTo(other.low) <= 0) {
                    lResult = t;
                    break;
                }
            }
        } else {
            lResult = this.low;
        }

        MathNumber uResult;
        if (other.high.compareTo(this.high) > 0) {
            uResult = MathNumber.PLUS_INFINITY;
            for (MathNumber t : THRESHOLDS) {
                if (t.compareTo(other.high) >= 0) {
                    uResult = t;
                    break;
                }
            }
        } else {
            uResult = this.high;
        }

        return new FloatIntervalLattice(lResult, uResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FloatIntervalLattice)) return false;
        FloatIntervalLattice other = (FloatIntervalLattice) obj;
        return Objects.equals(low, other.low) && Objects.equals(high, other.high);
    }

    @Override
    public int compareTo(FloatIntervalLattice o) {
        if (this.isBottom()) return o.isBottom() ? 0 : -1;
        if (o.isBottom())    return 1;
        if (this.isTop())    return o.isTop() ? 0 : 1;
        if (o.isTop())       return -1;

        int cmpLow = this.low.compareTo(o.low);
        if (cmpLow != 0) return cmpLow;
        return this.high.compareTo(o.high);
    }

    @Override
    public String toString() {
        return representation().toString();
    }
}