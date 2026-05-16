package it.unive.scsr.analysis.interval;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class IntervalLattice
		implements BaseLattice<IntervalLattice>, Comparable<IntervalLattice> {
	
	NumberInterval interval;
	
	public static IntervalLattice TOP = new IntervalLattice(NumberInterval.INFINITY);
	public static IntervalLattice BOTTOM = new IntervalLattice(null);
	public static IntervalLattice ZERO = new IntervalLattice(0,0);
	
	public IntervalLattice(NumberInterval interval) {
		this.interval = interval;
	}
	
	public IntervalLattice(Number l, Number u) {
		this.interval = new NumberInterval(l, u);
	}
	public IntervalLattice(MathNumber l, MathNumber u) {
		this.interval = new NumberInterval(l, u);
	}

	public IntervalLattice(double l, double u) {
		this.interval = new NumberInterval(l, u);
	}
	public IntervalLattice(long l, long u) {
		this.interval = new NumberInterval(l, u);
	}

	public IntervalLattice() {
		this(NumberInterval.INFINITY);
	}
	

	@Override
	    public IntervalLattice top() {
			return TOP;
	    }

	    @Override
	    public IntervalLattice bottom() {
	    	return BOTTOM;
	    }

	    @Override
	    public StructuredRepresentation representation() {
	    	if(this == BOTTOM)
	    		return Lattice.bottomRepresentation();

	    	return new StringRepresentation(this.interval.toString());
	    }

	    @Override
	    public IntervalLattice lubAux(IntervalLattice other) throws SemanticException {
	
	    	if(this.interval == null || other.interval == null)
	    		return BOTTOM;
	    	
	    	MathNumber l1 = this.interval.getLow();
	    	MathNumber l2 = other.interval.getLow();
	    	
	    	MathNumber lResult;
	    	if(l1.leq(l2))
	    		lResult =l1;
	    	else 
	    		lResult = l2;
	    	
	    	MathNumber u1 = this.interval.getHigh();
	    	MathNumber u2 = other.interval.getHigh();
	    	
	    	MathNumber uResult;
	    	
	    	if(u1.geq(u2))
	    		uResult = u1;
	    	else
	    		uResult = u2;
	    	
	    	return new IntervalLattice(lResult,uResult);
	    }
	    
	    

	    @Override
		public IntervalLattice glbAux(IntervalLattice other) throws SemanticException {

	    	
	    	if(this.interval == null || other.interval == null)
	    		return BOTTOM;
	    	
	    	MathNumber l1 = this.interval.getLow();
	    	MathNumber l2 = other.interval.getLow();
	    	
	    	MathNumber lResult;
	    	if(l1.geq(l2))
	    		lResult = l1;
	    	else 
	    		lResult = l2;
	    	
	    	MathNumber u1 = this.interval.getHigh();
	    	MathNumber u2 = other.interval.getHigh();
	    	
	    	MathNumber uResult;
	    	if(u1.leq(u2))
	    		uResult = u1;
	    	else
	    		uResult = u2;

			return new IntervalLattice(lResult, uResult);
		}

		@Override
	    public boolean lessOrEqualAux(IntervalLattice other) throws SemanticException {
			if(this.interval == null || other.interval == null)
				return false;
	    	return other.interval.includes(this.interval);
	    }

		@Override
		public IntervalLattice wideningAux(IntervalLattice other) throws SemanticException {
			
	    	if(this.interval == null || other.interval == null)
	    		return BOTTOM;
	    	
			MathNumber u1 = this.interval.getHigh();
			MathNumber u2 = other.interval.getHigh();
			
			MathNumber uResult = u1;
			if(u2.gt(u1))
				uResult = MathNumber.PLUS_INFINITY;
			
			MathNumber l1 = this.interval.getLow();
			MathNumber l2 = other.interval.getLow();
			
			MathNumber lResult = l1;
			if(l2.lt(l1)) {
				lResult = MathNumber.MINUS_INFINITY;
			}
			
			return new IntervalLattice(lResult, uResult);
			
		}

		@Override
		public int hashCode() {
			return Objects.hash(interval);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntervalLattice other = (IntervalLattice) obj;
			return Objects.equals(interval, other.interval);
		}

		@Override
		public int compareTo(IntervalLattice o) {
			if(isBottom())
				return o.isBottom() ? 0 : -1; 
			if(isTop())
				return o.isTop() ? 0 : 1;
			
			if(o.isBottom())
				return 1;
			
			if(isTop())
				return -1;
			
			return interval.compareTo(o.interval);
		}
}
