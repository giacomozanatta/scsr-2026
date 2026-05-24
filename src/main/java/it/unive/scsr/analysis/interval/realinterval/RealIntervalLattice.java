package it.unive.scsr.analysis.interval.realinterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.numeric.MathNumberConversionException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class RealIntervalLattice
		implements BaseLattice<RealIntervalLattice>, Comparable<RealIntervalLattice> {

	public final MathNumber low;
	public final MathNumber high;

	public static RealIntervalLattice TOP = new RealIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
	public static RealIntervalLattice BOTTOM = new RealIntervalLattice((MathNumber) null, null);
	public static RealIntervalLattice ZERO = new RealIntervalLattice(0., 0.);

	public RealIntervalLattice(MathNumber l, MathNumber h) {
		// Copied and adapted from it.unive.lisa.util.numeric.IntIntervall constructor (lines 129-149)
		if (l == null && h == null) {
			this.low = null;
			this.high = null;
		} else {
			Objects.requireNonNull(l, "Low bound must not be null");
			Objects.requireNonNull(h, "High bound must not be null");
			if (l.isNaN() || h.isNaN()) {
				this.low = MathNumber.NaN;
				this.high = MathNumber.NaN;
			} else if (l.compareTo(h) <= 0) {
				this.low = l;
				this.high = h;
			} else {
				this.low = h;
				this.high = l;
			}
		}
	}

	/*public RealIntervalLattice(double l, double h) {
		this(new MathNumber(l), new MathNumber(h));
	}*/

	public RealIntervalLattice(Number l, Number h) {
		this(new MathNumber(l.doubleValue()), new MathNumber(h.doubleValue()));
	}

	public RealIntervalLattice() {
		this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
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
		if (this == BOTTOM)
			return Lattice.bottomRepresentation();
		String rep;
		rep = String.format("[%s, %s]", this.low, this.high);
		return new StringRepresentation(rep);
	}

	@Override
	public RealIntervalLattice lubAux(RealIntervalLattice other) throws SemanticException {

		// Redundant check, since by assumption this and other are NOT bottom
		if (this.low == null || this.high == null || other.low == null || other.high == null)
			return BOTTOM;

		MathNumber lResult;
		if (this.low.leq(other.low))
			lResult = this.low;
		else
			lResult = other.low;

		MathNumber hResult;
		if (this.high.geq(other.high))
			hResult = this.high;
		else
			hResult = other.high;

		return new RealIntervalLattice(lResult, hResult);
	}

	@Override
	public RealIntervalLattice glbAux(RealIntervalLattice other) throws SemanticException {

		// Redundant check, since by assumption this and other are NOT bottom
		if (this.low == null || this.high == null || other.low == null || other.high == null)
			return BOTTOM;


		MathNumber lResult;
		if (this.low.geq(other.low))
			lResult = this.low;
		else
			lResult = other.low;

		MathNumber hResult;
		if (this.high.leq(other.high))
			hResult = this.high;
		else
			hResult = other.high;

		return new RealIntervalLattice(lResult, hResult);
	}

	@Override
	public boolean lessOrEqualAux(RealIntervalLattice other) throws SemanticException {
		// Redundant check, since by assumption this and other are NOT bottom
		if (this.low == null || this.high == null || other.low == null || other.high == null)
			return false;
		return this.low.geq(other.low) && this.high.leq(other.high);
	}

	@Override
	public RealIntervalLattice wideningAux(RealIntervalLattice other) throws SemanticException {

		// Redundant check, since by assumption this and other are NOT bottom
		if (this.low == null || this.high == null || other.low == null || other.high == null)
			return BOTTOM;

		MathNumber MARGIN = new MathNumber(1e-10);

		MathNumber lResult = other.low; // By default, adopt other bounds
		if (this.low.subtract(other.low).gt(MARGIN)) // But if other is too far (the distance is greater than MARGIN)
			lResult = MathNumber.MINUS_INFINITY; // Set bound to infinity

		MathNumber hResult = other.high;
		if (other.high.subtract(this.high).gt(MARGIN))
			hResult = MathNumber.PLUS_INFINITY;

		return new RealIntervalLattice(lResult, hResult);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		RealIntervalLattice that = (RealIntervalLattice) o;
		return Objects.equals(low, that.low) && Objects.equals(high, that.high);
	}

	@Override
	public int hashCode() {
		return Objects.hash(low, high);
	}

	@Override
	public int compareTo(RealIntervalLattice o) {
		// Copied and adapted from it.unive.lisa.util.numeric.IntIntervall (lines 474-489)
		if (this.isBottom())
			return o.isBottom() ? 0 : -1;
		if (this.isTop())
			return o.isTop() ? 0 : 1;

		if (o.isBottom())
			return 1;
		if (o.isTop())
			return -1;

		int cmp = this.low.compareTo(o.low);
		if (cmp != 0)
			return cmp;
		return this.high.compareTo(o.high);
	}
}
