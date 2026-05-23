package it.unive.scsr.analysis.interval;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class IntervaRealLatticeAlternative
		implements BaseLattice<IntervaRealLatticeAlternative>, Comparable<IntervaRealLatticeAlternative> {

	IntInterval i;

	public static IntervaRealLatticeAlternative TOP = new IntervaRealLatticeAlternative(MathNumber.MINUS_INFINITY,
			MathNumber.PLUS_INFINITY);
	public static IntervaRealLatticeAlternative BOTTOM = new IntervaRealLatticeAlternative(null);
	public static IntervaRealLatticeAlternative ZERO = new IntervaRealLatticeAlternative(0, 0);

	// number of iteration after which we apply widening
	private static final MathNumber guard = new MathNumber(1000);

	public IntervaRealLatticeAlternative(IntInterval i) {
		this.i = i;
	}

	public IntervaRealLatticeAlternative(MathNumber l, MathNumber u) {
		this.i = new IntInterval(l, u);
	}

	public IntervaRealLatticeAlternative(int l, int u) {
		this.i = new IntInterval(l, u);
	}

	public IntervaRealLatticeAlternative(float l, float u) {
		this.i = new IntInterval(new MathNumber((double) l), new MathNumber((double) u));
	}

	public IntervaRealLatticeAlternative(double l, double u) {
		this.i = new IntInterval(new MathNumber(l), new MathNumber(u));
	}

	public IntervaRealLatticeAlternative() {
		this(IntInterval.INFINITY);
	}

	@Override
	public IntervaRealLatticeAlternative top() {
		return TOP;
	}

	@Override
	public IntervaRealLatticeAlternative bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == BOTTOM)
			return Lattice.bottomRepresentation();

		MathNumber l = this.i.getLow();
		MathNumber u = this.i.getHigh();

		return new StringRepresentation("[" + l + "," + u + "]");
	}

	@Override
	public IntervaRealLatticeAlternative lubAux(IntervaRealLatticeAlternative other) throws SemanticException {

		if (this.i == null || other.i == null)
			return BOTTOM;

		MathNumber l1 = this.i.getLow();
		MathNumber l2 = other.i.getLow();

		MathNumber lResult;
		if (l1.leq(l2))
			lResult = l1;
		else
			lResult = l2;

		MathNumber u1 = this.i.getHigh();
		MathNumber u2 = other.i.getHigh();

		MathNumber uResult;

		if (u1.geq(u2))
			uResult = u1;
		else
			uResult = u2;

		return new IntervaRealLatticeAlternative(lResult, uResult);
	}

	@Override
	public IntervaRealLatticeAlternative glbAux(IntervaRealLatticeAlternative other) throws SemanticException {

		if (this.i == null || other.i == null)
			return BOTTOM;

		MathNumber l1 = this.i.getLow();
		MathNumber l2 = other.i.getLow();

		MathNumber lResult;
		if (l1.geq(l2))
			lResult = l1;
		else
			lResult = l2;

		MathNumber u1 = this.i.getHigh();
		MathNumber u2 = other.i.getHigh();

		MathNumber uResult;
		if (u1.leq(u2))
			uResult = u1;
		else
			uResult = u2;

		return new IntervaRealLatticeAlternative(lResult, uResult);
	}

	@Override
	public boolean lessOrEqualAux(IntervaRealLatticeAlternative other) throws SemanticException {
		if (this.i == null || other.i == null)
			return false;
		return other.i.includes(this.i);
	}

	@Override
	public IntervaRealLatticeAlternative wideningAux(IntervaRealLatticeAlternative other) throws SemanticException {

		if (this.i == null || other.i == null)
			return BOTTOM;

		MathNumber u1 = this.i.getHigh();
		MathNumber u2 = other.i.getHigh();
		MathNumber l1 = this.i.getLow();
		MathNumber l2 = other.i.getLow();

		// upper: max(u0, u1)
		MathNumber uMax;
		if (u1.geq(u2)) {

			uMax = u1;
		} else {
			uMax = u2;
		}
		MathNumber uResult;
		if (uMax.lt(guard)) {
			uResult = uMax;
		} else {
			uResult = MathNumber.PLUS_INFINITY;
		}
		// lower: min(l0, l1); 
		MathNumber lMin;
		if (l1.leq(l2)) {
			lMin = l1;
		} else {
			lMin = l2;
		}

		MathNumber lResult;
		if (lMin.gt(guard.multiply(MathNumber.MINUS_ONE))) {
			lResult = lMin;
		} else {
			lResult = MathNumber.MINUS_INFINITY;
		}
		return new IntervaRealLatticeAlternative(lResult, uResult);
	}

	// Narrowing:
	// for [a, b] ≥ [a', b'], [a, b] Delta [a', b']
	// if a = -inf then a' else a
	// if b = +inf then b' else b

	@Override
	public IntervaRealLatticeAlternative narrowingAux(IntervaRealLatticeAlternative other) throws SemanticException {
		if (this.i == null || other.i == null)
			return BOTTOM;

		MathNumber l1 = this.i.getLow();
		MathNumber l2 = other.i.getLow();
		MathNumber u1 = this.i.getHigh();
		MathNumber u2 = other.i.getHigh();
		// if the current bound is +-inf (introduced by widening), replace it
		// with the more precise value from the new iteration
		MathNumber lResult = l1.leq(MathNumber.MINUS_INFINITY) ? l2 : l1;
		MathNumber uResult = u1.geq(MathNumber.PLUS_INFINITY) ? u2 : u1;

		return new IntervaRealLatticeAlternative(lResult, uResult);
	}

	@Override
	public int hashCode() {
		return Objects.hash(i);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntervaRealLatticeAlternative other = (IntervaRealLatticeAlternative) obj;
		return Objects.equals(i, other.i);
	}

	@Override
	public int compareTo(IntervaRealLatticeAlternative o) {
		if (isBottom())
			return o.isBottom() ? 0 : -1;
		if (isTop())
			return o.isTop() ? 0 : 1;

		if (o.isBottom())
			return 1;

		if (isTop())
			return -1;

		return i.compareTo(o.i);
	}

	public MathNumber getHigh() {
		return i == null ? null : i.getHigh();
	}

	public MathNumber getLow() {
		return i == null ? null : i.getLow();
	}

	public boolean includes(IntervaRealLatticeAlternative other) {
		if (i == null || other.i == null)
			return false;
		return i.includes(other.i);
	}
}
