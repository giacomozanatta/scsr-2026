package it.unive.scsr.analysis.realinterval;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class RealIntervalLattice implements BaseLattice<RealIntervalLattice> {

	public static final RealIntervalLattice TOP = new RealIntervalLattice(Double.NEGATIVE_INFINITY,
			Double.POSITIVE_INFINITY);
	public static final RealIntervalLattice BOTTOM = new RealIntervalLattice(true);
	public static final RealIntervalLattice ZERO = new RealIntervalLattice(0.0, 0.0);

	private final double low;
	private final double high;
	private final boolean bottom;

	public RealIntervalLattice() {
		this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public RealIntervalLattice(double value) {
		this(value, value);
	}

	public RealIntervalLattice(double low, double high) {
		if (Double.isNaN(low) || Double.isNaN(high) || low > high) {
			this.low = Double.NaN;
			this.high = Double.NaN;
			this.bottom = true;
		} else {
			this.low = normalizeZero(low);
			this.high = normalizeZero(high);
			this.bottom = false;
		}
	}

	private RealIntervalLattice(boolean bottom) {
		this.low = Double.NaN;
		this.high = Double.NaN;
		this.bottom = bottom;
	}

	public double getLow() {
		return low;
	}

	public double getHigh() {
		return high;
	}

	public boolean containsZero() {
		return !bottom && low <= 0.0 && high >= 0.0;
	}

	public boolean isZero() {
		return !bottom && low == 0.0 && high == 0.0;
	}

	@Override
	public RealIntervalLattice top() {
		return TOP;
	}

	@Override
	public RealIntervalLattice bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (bottom)
			return Lattice.bottomRepresentation();
		return new StringRepresentation("[" + format(low) + "," + format(high) + "]");
	}

	@Override
	public RealIntervalLattice lubAux(RealIntervalLattice other) throws SemanticException {
		return new RealIntervalLattice(Math.min(low, other.low), Math.max(high, other.high));
	}

	@Override
	public RealIntervalLattice glbAux(RealIntervalLattice other) throws SemanticException {
		return new RealIntervalLattice(Math.max(low, other.low), Math.min(high, other.high));
	}

	@Override
	public boolean lessOrEqualAux(RealIntervalLattice other) throws SemanticException {
		return other.low <= low && high <= other.high;
	}

	@Override
	public RealIntervalLattice wideningAux(RealIntervalLattice other) throws SemanticException {
		double widenedLow = other.low < low ? Double.NEGATIVE_INFINITY : low;
		double widenedHigh = other.high > high ? Double.POSITIVE_INFINITY : high;
		return new RealIntervalLattice(widenedLow, widenedHigh);
	}

	@Override
	public int hashCode() {
		return Objects.hash(bottom, low, high);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		RealIntervalLattice other = (RealIntervalLattice) obj;
		if (bottom || other.bottom)
			return bottom == other.bottom;
		return Double.compare(low, other.low) == 0 && Double.compare(high, other.high) == 0;
	}

	@Override
	public String toString() {
		return representation().toString();
	}

	private static double normalizeZero(double value) {
		return value == 0.0 ? 0.0 : value;
	}

	private static String format(double value) {
		if (value == Double.NEGATIVE_INFINITY)
			return "-Inf";
		if (value == Double.POSITIVE_INFINITY)
			return "+Inf";
		return Double.toString(normalizeZero(value));
	}
}
