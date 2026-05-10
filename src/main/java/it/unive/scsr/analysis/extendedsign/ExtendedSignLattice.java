package it.unive.scsr.analysis.extendedsign;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice 
		implements BaseLattice<ExtendedSignLattice>{
	
	private int element;
	
	// declaration of lattice elements
	// 0, 1, 2, 3, 4 are just an encoding
	public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
	public static ExtendedSignLattice POS = new ExtendedSignLattice(1);
	public static ExtendedSignLattice NEG = new ExtendedSignLattice(2);
	public static ExtendedSignLattice ZERO = new ExtendedSignLattice(3);
	public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(4);
	
	public ExtendedSignLattice(int e) {
		element = e;
	}

	@Override
	    public ExtendedSignLattice top() {
		return ExtendedSignLattice.TOP;
	    }

	    @Override
	    public ExtendedSignLattice bottom() {
		return ExtendedSignLattice.BOTTOM;
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
			ExtendedSignLattice other = (ExtendedSignLattice) obj;
			return element == other.element;
		}

		@Override
	    public StructuredRepresentation representation() {
		
			if(this == ExtendedSignLattice.BOTTOM)
				return Lattice.bottomRepresentation();
			else if(this == ExtendedSignLattice.TOP)
				return Lattice.topRepresentation();
			else if(this == ExtendedSignLattice.ZERO)
				return new StringRepresentation("0");
			else if(this == ExtendedSignLattice.POS) 
				return new StringRepresentation("+");
	
			return new StringRepresentation("-");
	    }

	    @Override
	    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
			/*
			if (this.equals(other))
				return this;
			else if (this == SignLattice.TOP || other == SignLattice.TOP)
				return SignLattice.TOP;
	    	else if ((this == SignLattice.POS || this == SignLattice.ZERO || this == SignLattice.NEG)
					&&
					(other == SignLattice.POS || other == SignLattice.ZERO || other == SignLattice.NEG))
	    		return SignLattice.TOP;
			else if (this == SignLattice.BOTTOM && (other == SignLattice.POS || other == SignLattice.ZERO || other == SignLattice.NEG))
				return other;
			else if (other == SignLattice.BOTTOM && (this == SignLattice.POS || this == SignLattice.ZERO || this == SignLattice.NEG))
				return this;
			else if (this == SignLattice.BOTTOM && other == SignLattice.BOTTOM)
				return SignLattice.BOTTOM;
			else
				return SignLattice.TOP;
			*/
			return ExtendedSignLattice.TOP; // as before, the logic is already there, so if the 2 compared things are different, it's top
	    }

	    @Override
	    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
	    	return false; // same as before
	    }

		// For glb in this case we use default LiSA implementation
	
	
	    // Other method implementations to use in Sign domain to assume and check satisfability of some stuff
		
	    public Satisfiability eq(
				ExtendedSignLattice other) {
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
		public Satisfiability gt(
				ExtendedSignLattice other) {
			if (this.isBottom() || other.isBottom())
				return Satisfiability.BOTTOM;
			else if (this.isTop() || other.isTop())
				return Satisfiability.UNKNOWN;
			else if (this == NEG)
				return other == NEG ? Satisfiability.UNKNOWN : Satisfiability.NOT_SATISFIED;
			else if (this == ZERO)
				return other == NEG ? Satisfiability.SATISFIED : Satisfiability.NOT_SATISFIED;
			else
				return other == POS ? Satisfiability.UNKNOWN : Satisfiability.SATISFIED;
		}


}
