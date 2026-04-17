package it.unive.scsr.analysis.parity;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	private int element;
	
	public static ParityLattice TOP = new ParityLattice(0);
	public static ParityLattice EVEN = new ParityLattice(1);
	public static ParityLattice ODD = new ParityLattice(2);
	public static ParityLattice BOTTOM = new ParityLattice(3);

	public ParityLattice(int e) {
		element = e;
	}

	@Override
	public ParityLattice top() 
	{
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() 
	{
		return ParityLattice.BOTTOM;
	}

	@Override
	public int hashCode() 
	{
		return Objects.hash(element);
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ParityLattice other = (ParityLattice) obj;
		
		return element == other.element;
	}
	

	@Override
	public StructuredRepresentation representation() 
	{
		if(this == ParityLattice.BOTTOM)
			return Lattice.bottomRepresentation();

		else if(this == ParityLattice.TOP)
			return Lattice.topRepresentation();

		else if(this == ParityLattice.EVEN)
			return new StringRepresentation("EVEN");
		
		return new StringRepresentation("ODD");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException 
	{
		if (this == other)
        	return this;
    
		if (this == BOTTOM)
        	return other;
    
		if (other == BOTTOM)
        	return this;
    
    	return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException 
	{
		if (this == other || other == TOP)
        	return true;
    	
		if (this == BOTTOM)
        	return true;
    
    	return false;
	}

	public Satisfiability eq(ParityLattice other) 
	{
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;
		
		else if (this.isTop() || other.isTop())
			return Satisfiability.UNKNOWN;
		
		else if (!this.equals(other))
			return Satisfiability.NOT_SATISFIED;
		
		else
			return Satisfiability.UNKNOWN;
	}

	public Satisfiability neq(ParityLattice other) 
	{
    	if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;
		
		else if (this.isTop() || other.isTop())
			return Satisfiability.UNKNOWN;
		
		else if (!this.equals(other))
			return Satisfiability.SATISFIED;
		
		else
			return Satisfiability.UNKNOWN;
	}

}