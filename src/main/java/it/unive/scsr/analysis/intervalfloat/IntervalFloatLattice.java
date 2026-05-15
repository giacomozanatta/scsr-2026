package it.unive.scsr.analysis.intervalfloat;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.scsr.analysis.interval.IntervalLattice;
import it.unive.scsr.analysis.intervalfloat.FloatInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class IntervalFloatLattice
		extends IntervalLattice {

	FloatInterval i;


	public static IntervalFloatLattice TOP = new IntervalFloatLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
	public static IntervalFloatLattice BOTTOM = new IntervalFloatLattice(null);
	public static IntervalFloatLattice ZERO = new IntervalFloatLattice(0,0);

	public IntervalFloatLattice(FloatInterval i) {
		this.i = i;
	}

	public IntervalFloatLattice(MathNumber l, MathNumber u) {
		this.i = new FloatInterval(l, u);
	}

	public IntervalFloatLattice(float l, float u) {
		this.i = new FloatInterval(l, u);
	}

	public IntervalFloatLattice() {
		this(FloatInterval.INFINITY);
	}


	@Override
	public IntervalFloatLattice top() {
		return TOP;
	}

	@Override
	public IntervalFloatLattice bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if(this == BOTTOM)
			return Lattice.bottomRepresentation();

		MathNumber l = this.i.getLow();
		MathNumber u = this.i.getHigh();

		return new StringRepresentation("["+l+","+u+"]");
	}

	public IntervalFloatLattice lubAux(IntervalFloatLattice other) throws SemanticException {

		if(this.i == null || other.i == null)
			return BOTTOM;

		MathNumber l1 = this.i.getLow();
		MathNumber l2 = other.i.getLow();

		MathNumber lResult;
		if(l1.leq(l2))
			lResult =l1;
		else
			lResult = l2;

		MathNumber u1 = this.i.getHigh();
		MathNumber u2 = other.i.getHigh();

		MathNumber uResult;

		if(u1.geq(u2))
			uResult = u1;
		else
			uResult = u2;

		return new IntervalFloatLattice(lResult,uResult);
	}



	public IntervalFloatLattice glbAux(IntervalFloatLattice other) throws SemanticException {


		if(this.i == null || other.i == null)
			return BOTTOM;

		MathNumber l1 = this.i.getLow();
		MathNumber l2 = other.i.getLow();

		MathNumber lResult;
		if(l1.geq(l2))
			lResult = l1;
		else
			lResult = l2;

		MathNumber u1 = this.i.getHigh();
		MathNumber u2 = other.i.getHigh();

		MathNumber uResult;
		if(u1.leq(u2))
			uResult = u1;
		else
			uResult = u2;

		return new IntervalFloatLattice(lResult, uResult);
	}

	public boolean lessOrEqualAux(IntervalFloatLattice other) throws SemanticException {
		if(this.i == null || other.i == null)
			return false;
		return other.i.includes(this.i);
	}

	public IntervalFloatLattice wideningAux(IntervalFloatLattice other) throws SemanticException {
		if(this.i == null || other.i == null)
			return BOTTOM;

		// Note: the following code does work as before, but code that
		// does tiny increments (e.g. +0.0000001) could still trick
		// it into thinking that it doesn't go to infinity

		MathNumber u1 = this.i.getHigh();
		MathNumber u2 = other.i.getHigh();
		MathNumber l1 = this.i.getLow();
		MathNumber l2 = other.i.getLow();

		MathNumber uResult = u1;
		MathNumber lResult = l1;

		// small margin to cancel out floating-point inaccuracies
		MathNumber margin = new MathNumber(1e-5);

		// if the new upper bound is significantly greater than the safety margin, then widen to +INFINITY.
		// We are assuming that the increment isn't just noise
		if(u2.subtract(u1).gt(margin)) {
			uResult = MathNumber.PLUS_INFINITY;
		} else if(u2.gt(u1)) { // If it's greater but within the epsilon error margin, just accept the new bound
			uResult = u2;
		}

		if(l1.subtract(l2).gt(margin)) { /// similarly to the previous case, but with the lower bounds
			lResult = MathNumber.MINUS_INFINITY;
		} else if(l2.lt(l1)) {
			lResult = l2;
		}

		return new IntervalFloatLattice(lResult, uResult);
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
		IntervalFloatLattice other = (IntervalFloatLattice) obj;
		return Objects.equals(i, other.i);
	}

	public int compareTo(IntervalFloatLattice o) {
		if(isBottom())
			return o.isBottom() ? 0 : -1;
		if(isTop())
			return o.isTop() ? 0 : 1;

		if(o.isBottom())
			return 1;

		if(isTop())
			return -1;

		return i.compareTo(o.i);
	}
}
