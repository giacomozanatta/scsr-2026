package it.unive.scsr.analysis.Extended_Sign;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class Extended_SignLattice 
		implements BaseLattice<Extended_SignLattice>{
	
	private int element;
	
	// declaration of lattice elements
	// 0, 1, 2, 3, 4 are just an encoding
	public static Extended_SignLattice TOP = new Extended_SignLattice(0);
	public static Extended_SignLattice POS = new Extended_SignLattice(1);
	public static Extended_SignLattice NEG = new Extended_SignLattice(2);
	public static Extended_SignLattice STRICT_POS = new Extended_SignLattice(3);
	public static Extended_SignLattice STRICT_NEG = new Extended_SignLattice(4);
	public static Extended_SignLattice ZERO = new Extended_SignLattice(5);
	public static Extended_SignLattice BOTTOM = new Extended_SignLattice(6);
	
	public Extended_SignLattice(int e) {
		element = e;
	}

	@Override
	    public Extended_SignLattice top() {
		return Extended_SignLattice.TOP;
	    }

	    @Override
	    public Extended_SignLattice bottom() {
		return Extended_SignLattice.BOTTOM;
	    }
	    
	    

	    @Override
		public int hashCode() {
			return Objects.hash(element);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Extended_SignLattice other = (Extended_SignLattice) obj;
			return element == other.element;
		}

		@Override
	    public StructuredRepresentation representation() {
		
			if(this == Extended_SignLattice.BOTTOM)
				return Lattice.bottomRepresentation();
			else if(this == Extended_SignLattice.TOP)
				return Lattice.topRepresentation();
			else if(this == Extended_SignLattice.ZERO)
				return new StringRepresentation("0");
			else if(this == Extended_SignLattice.POS) 
				return new StringRepresentation("+");
			else if(this == Extended_SignLattice.STRICT_POS)
				return new StringRepresentation("++");
			else if(this == Extended_SignLattice.STRICT_NEG)
				return new StringRepresentation("--");
	
			return new StringRepresentation("-");
	    }

	    @Override
	    public Extended_SignLattice lubAux(Extended_SignLattice other) throws SemanticException 
		{
	    	if(this == Extended_SignLattice.POS && other == Extended_SignLattice.STRICT_POS)
	    		return Extended_SignLattice.POS;

			if(this == Extended_SignLattice.POS && other == Extended_SignLattice.ZERO)
	    		return Extended_SignLattice.POS;

			if(this == Extended_SignLattice.STRICT_POS && other == Extended_SignLattice.ZERO)
	    		return Extended_SignLattice.POS;

			if(this == Extended_SignLattice.STRICT_POS && other == Extended_SignLattice.POS)
	    		return Extended_SignLattice.POS;

			if(this == Extended_SignLattice.STRICT_NEG && other == Extended_SignLattice.ZERO)
	    		return Extended_SignLattice.NEG;

			if(this == Extended_SignLattice.STRICT_NEG && other == Extended_SignLattice.NEG)
	    		return Extended_SignLattice.NEG;

			if(this == Extended_SignLattice.ZERO && other == Extended_SignLattice.STRICT_POS)
	    		return Extended_SignLattice.POS;

			if(this == Extended_SignLattice.ZERO && other == Extended_SignLattice.POS)
	    		return Extended_SignLattice.POS;

			if(this == Extended_SignLattice.ZERO && other == Extended_SignLattice.STRICT_NEG)
	    		return Extended_SignLattice.NEG;

			if(this == Extended_SignLattice.ZERO && other == Extended_SignLattice.NEG)
	    		return Extended_SignLattice.NEG;

			if(this == Extended_SignLattice.NEG && other == Extended_SignLattice.STRICT_NEG)
	    		return Extended_SignLattice.NEG;

			if(this == Extended_SignLattice.NEG && other == Extended_SignLattice.ZERO)
	    		return Extended_SignLattice.NEG;

	    	return Extended_SignLattice.TOP;
	    }

	    @Override
	    public boolean lessOrEqualAux(Extended_SignLattice other) throws SemanticException 
		{
	    	 if(this == STRICT_POS && other == POS) 
				return true;
    
			 if(this == STRICT_NEG && other == NEG) 
				return true;
    
			 if(this == ZERO && other == POS) 
				return true;
    
			 if(this == ZERO && other == NEG) 
				return true;
    
			 return false;
	    }

		// For glb in this case we use default LiSA implementation
	
	
	    // Other method implementations to use in Sign domain to assume and check satisfability of some stuff
		
	    public Satisfiability eq(Extended_SignLattice other) 
		{
			if (this.isBottom() || other.isBottom())
				return Satisfiability.BOTTOM;

			else if (this.isTop() || other.isTop())
				return Satisfiability.UNKNOWN;

			else if (!this.equals(other))
				return Satisfiability.NOT_SATISFIED;

			else if (this == ZERO)
				return Satisfiability.SATISFIED;
			else
				return Satisfiability.UNKNOWN;
		}

		/**
		 * Tests if this instance is greater than the given one, returning a
		 * {@link Satisfiability} element.
		 * 
		 * @param other the instance
		 * 
		 * @return the satisfiability of {@code this > other}
		 */
		public Satisfiability gt(Extended_SignLattice other) 
		{
			if (this.isBottom() || other.isBottom())
				return Satisfiability.BOTTOM;

			else if (this.isTop() || other.isTop())
				return Satisfiability.UNKNOWN;

			else if (this == NEG)
				return ((other == STRICT_NEG) || (other == NEG)) ? Satisfiability.UNKNOWN : Satisfiability.NOT_SATISFIED;

			else if (this == ZERO)
			{
				if (other == NEG)
					return Satisfiability.UNKNOWN;

				else if (other == STRICT_NEG)
					return Satisfiability.SATISFIED;

				else
					return Satisfiability.NOT_SATISFIED;
			}
				
			else if (this == STRICT_NEG)
			{
				if (other == STRICT_NEG)
					return Satisfiability.UNKNOWN;

				else
					return Satisfiability.NOT_SATISFIED;
			}

			else if (this == STRICT_POS)
				return ((other == STRICT_POS) || (other == POS)) ? Satisfiability.UNKNOWN : Satisfiability.SATISFIED;

			else
				return ((other == STRICT_NEG)) ? Satisfiability.SATISFIED : Satisfiability.UNKNOWN;
		}


}
