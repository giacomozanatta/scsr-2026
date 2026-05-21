package it.unive.scsr.analysis.interval.realinterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class RealIntervalLattice
		implements BaseLattice<RealIntervalLattice>, Comparable<RealIntervalLattice> {
	
	private final MathNumber low;
	private final MathNumber high;

	public static RealIntervalLattice TOP = new RealIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
	public static RealIntervalLattice BOTTOM = new RealIntervalLattice(null, null);
	public static RealIntervalLattice ZERO = new RealIntervalLattice(0,0);


	
	public RealIntervalLattice(MathNumber low, MathNumber high) {
		this.low = low;
		this.high = high;
	}
	
	public RealIntervalLattice(double l, double u) {
		this.low = new MathNumber(l);
		this.high = new MathNumber(u);
	}
	
	public RealIntervalLattice() {
		this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
	}
	

	@Override
	public RealIntervalLattice top() {
			return TOP;
	    }

	@Override
	public RealIntervalLattice bottom() { return BOTTOM; }

	@Override
	public StructuredRepresentation representation() {
		if(this == BOTTOM)
			return Lattice.bottomRepresentation();

		return new StringRepresentation("["+low+","+high+"]");
	}

	@Override
	public RealIntervalLattice lubAux(RealIntervalLattice other) throws SemanticException {
		if(this.low == null || this.high == null || other.low == null || other.high == null)
			return BOTTOM;

		MathNumber l1 = this.low;
		MathNumber l2 = other.low;

		MathNumber lResult;
		if(l1.leq(l2))
			lResult = l1;
		else
			lResult = l2;

		MathNumber u1 = this.high;
		MathNumber u2 = other.high;

		MathNumber uResult;

		if(u1.geq(u2))
			uResult = u1;
		else
			uResult = u2;

		return new RealIntervalLattice(lResult,uResult);
	}
	    


	@Override
	public RealIntervalLattice glbAux(RealIntervalLattice other) throws SemanticException {
		if(this.low == null || this.high == null || other.low == null || other.high == null)
			return BOTTOM;

		MathNumber l1 = this.low;
		MathNumber l2 = other.low;

		MathNumber lResult;
		if(l1.geq(l2))
			lResult = l1;
		else
			lResult = l2;

		MathNumber u1 = this.high;
		MathNumber u2 = other.high;

		MathNumber uResult;
		if(u1.leq(u2))
			uResult = u1;
		else
			uResult = u2;

		return new RealIntervalLattice(lResult, uResult);
	}

	@Override
	public boolean lessOrEqualAux(RealIntervalLattice other) throws SemanticException {
		if(this.low == null || this.high == null || other.low == null || other.high == null)
			return false;
		return other.includes(this);
	}

	@Override
	public RealIntervalLattice wideningAux(RealIntervalLattice other) throws SemanticException {
		if(this.low == null || this.high == null || other.low == null || other.high == null)
			return BOTTOM;

		MathNumber u1 = this.high;
		MathNumber u2 = other.high;

		MathNumber uResult = u1;
		if(u2.gt(u1))
			uResult = MathNumber.PLUS_INFINITY;

		MathNumber l1 = this.low;
		MathNumber l2 = other.low;

		MathNumber lResult = l1;
		if(l2.lt(l1)) {
			lResult = MathNumber.MINUS_INFINITY;
		}

		return new RealIntervalLattice(lResult, uResult);

	}

	@Override
	public int hashCode() {
		return Objects.hash(low, high);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RealIntervalLattice other = (RealIntervalLattice) obj;
		return this.low.equals(other.low) && this.high.equals(other.high);
	}

	@Override
	public int compareTo(RealIntervalLattice o) {
		if(isBottom())
			return o.isBottom() ? 0 : -1;
		if(isTop())
			return o.isTop() ? 0 : 1;

		if(o.isBottom())
			return 1;

		if(isTop())
			return -1;

		int isLowSame = this.low.compareTo(o.low);
		if(isLowSame != 0)
			return this.high.compareTo(o.high);

		return isLowSame;
	}

	private boolean includes(RealIntervalLattice other){
		if (isBottom() || other.isBottom())
			return false;
		return low.compareTo(other.low) <= 0 && high.compareTo(other.high) >= 0;
	}

    public MathNumber getLow() {
        return low;
    }

    public MathNumber getHigh() {
        return high;
    }
}
