package it.unive.scsr.analysis.intervalfloat;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class IntervalFloatLattice
		implements BaseLattice<IntervalFloatLattice>, Comparable<IntervalFloatLattice> {
	
	IntInterval i;
	
	public static IntervalFloatLattice TOP = new IntervalFloatLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
	public static IntervalFloatLattice BOTTOM = new IntervalFloatLattice(null);
	public static IntervalFloatLattice ZERO = new IntervalFloatLattice(0,0);
	
	public IntervalFloatLattice(IntInterval i) {
		this.i = i;
	}
	
	public IntervalFloatLattice(MathNumber l, MathNumber u) {
		this.i = new IntInterval(l, u);
	}
	
	public IntervalFloatLattice(int l, int u) {
		this.i = new IntInterval(l, u);
	}
	
	public IntervalFloatLattice() {
		this(IntInterval.INFINITY);
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

	    @Override
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
	    
	    

	    @Override
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

		@Override
	    public boolean lessOrEqualAux(IntervalFloatLattice other) throws SemanticException {
			if(this.i == null || other.i == null)
				return false;
	    	return other.i.includes(this.i);
	    }

		@Override
		public IntervalFloatLattice wideningAux(IntervalFloatLattice other) throws SemanticException {
			
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

		@Override
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
