package it.unive.scsr.analysis.Extended_Interval;

import java.math.BigDecimal;
import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class Extended_IntervalLattice 
		implements BaseLattice<Extended_IntervalLattice>, Comparable<Extended_IntervalLattice> {
	
	IntInterval i;
	
	public static Extended_IntervalLattice TOP = new Extended_IntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
	public static Extended_IntervalLattice BOTTOM = new Extended_IntervalLattice(null);
	public static Extended_IntervalLattice ZERO = new Extended_IntervalLattice(0,0);
	
	public Extended_IntervalLattice(IntInterval i) {
		this.i = i;
	}

	public Extended_IntervalLattice(double l, double u)
	{
		this.i = new IntInterval(
			new MathNumber(BigDecimal.valueOf(l)), new MathNumber(BigDecimal.valueOf(u)));
	}

	public Extended_IntervalLattice(float l, float u)
	{
		this.i = new IntInterval(
			new MathNumber(BigDecimal.valueOf(l)), new MathNumber(BigDecimal.valueOf(u)));
	}
	
	public Extended_IntervalLattice(MathNumber l, MathNumber u) {
		this.i = new IntInterval(l, u);
	}
	
	public Extended_IntervalLattice(int l, int u) {
		this.i = new IntInterval(l, u);
	}
	
	public Extended_IntervalLattice() {
		this(IntInterval.INFINITY);
	}
	

	@Override
	    public Extended_IntervalLattice top() {
			return TOP;
	    }

	    @Override
	    public Extended_IntervalLattice bottom() {
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

	    @Override
	    public Extended_IntervalLattice lubAux(Extended_IntervalLattice other) throws SemanticException {
	
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
	    	
	    	return new Extended_IntervalLattice(lResult,uResult);
	    }
	    
	    @Override
		public Extended_IntervalLattice glbAux(Extended_IntervalLattice other) throws SemanticException {

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

			return new Extended_IntervalLattice(lResult, uResult);
		}

		@Override
	    public boolean lessOrEqualAux(Extended_IntervalLattice other) throws SemanticException {
			if(this.i == null || other.i == null)
				return false;
	    	return other.i.includes(this.i);
	    }

		@Override
		public Extended_IntervalLattice wideningAux(Extended_IntervalLattice other) throws SemanticException {
			
	    	if(this.i == null || other.i == null)
	    		return BOTTOM;
	    	
			MathNumber u1 = this.i.getHigh();
			MathNumber u2 = other.i.getHigh();
			
			MathNumber uResult = u1;
			if(u2.gt(u1))
				uResult = MathNumber.PLUS_INFINITY;
			
			MathNumber l1 = this.i.getLow();
			MathNumber l2 = other.i.getLow();
			
			MathNumber lResult = l1;
			if(l2.lt(l1)) {
				lResult = MathNumber.MINUS_INFINITY;
			}
			
			return new Extended_IntervalLattice(lResult, uResult);
			
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
			Extended_IntervalLattice other = (Extended_IntervalLattice) obj;
			return Objects.equals(i, other.i);
		}

		@Override
		public int compareTo(Extended_IntervalLattice o) {
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

		public IntInterval getInterval() {
    		return this.i;
		}
}
