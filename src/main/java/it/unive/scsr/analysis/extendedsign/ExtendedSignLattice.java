package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedSignLattice
		implements BaseLattice<ExtendedSignLattice>{
	
	private int element;
	
	// declaration of lattice elements

	public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);     // n belongs to Z
	public static ExtendedSignLattice NONPOS = new ExtendedSignLattice(1);  // n <= 0
	public static ExtendedSignLattice NONNEG = new ExtendedSignLattice(2);  // n >= 0
	public static ExtendedSignLattice NONZERO = new ExtendedSignLattice(3); // n != 0
	public static ExtendedSignLattice POS = new ExtendedSignLattice(4);     // n > 0
	public static ExtendedSignLattice NEG = new ExtendedSignLattice(5);     // n < 0
	public static ExtendedSignLattice ZERO = new ExtendedSignLattice(6);    // n = 0
	public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);  // n does not belong to Z
	
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
				return new StringRepresentation("ZERO");
			else if(this == ExtendedSignLattice.POS)
				return new StringRepresentation("POSITIVE");
			else if(this == ExtendedSignLattice.NONPOS)
				return new StringRepresentation("NON-POSITIVE");
			else if(this == ExtendedSignLattice.NONNEG)
				return new StringRepresentation("NON-NEGATIVE");
			else if(this == ExtendedSignLattice.NONZERO)
				return new StringRepresentation("NON-ZERO");
	
			return new StringRepresentation("NEGATIVE");
	    }

	    @Override
	    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
			// identical elements
			if (this == other)
				return this;

			// bottom
			if (this == ExtendedSignLattice.BOTTOM)
				return other;

			if (other == ExtendedSignLattice.BOTTOM)
				return this;

			// NEG JOIN ZERO = NONPOS
			if ((this == ExtendedSignLattice.NEG  && other == ExtendedSignLattice.ZERO) ||
					(this == ExtendedSignLattice.ZERO && other == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NONPOS;

			// POS JOIN ZERO = NONNEG
			if ((this == ExtendedSignLattice.POS  && other == ExtendedSignLattice.ZERO) ||
					(this == ExtendedSignLattice.ZERO && other == ExtendedSignLattice.POS))
				return ExtendedSignLattice.NONNEG;

			// NEG JOIN POS = NONZERO
			if ((this == ExtendedSignLattice.NEG && other == ExtendedSignLattice.POS) ||
					(this == ExtendedSignLattice.POS && other == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NONZERO;

			// NEG JOIN NONPOS = NONPOS
			if ((this == ExtendedSignLattice.NEG    && other == ExtendedSignLattice.NONPOS) ||
					(this == ExtendedSignLattice.NONPOS && other == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NONPOS;

			// ZERO JOIN NONPOS = NONPOS
			if ((this == ExtendedSignLattice.ZERO   && other == ExtendedSignLattice.NONPOS) ||
					(this == ExtendedSignLattice.NONPOS && other == ExtendedSignLattice.ZERO))
				return ExtendedSignLattice.NONPOS;

			// POS JOIN NONNEG = NONNEG
			if ((this == ExtendedSignLattice.POS    && other == ExtendedSignLattice.NONNEG) ||
					(this == ExtendedSignLattice.NONNEG && other == ExtendedSignLattice.POS))
				return ExtendedSignLattice.NONNEG;

			// ZERO JOIN NONNEG = NONNEG
			if ((this == ExtendedSignLattice.ZERO   && other == ExtendedSignLattice.NONNEG) ||
					(this == ExtendedSignLattice.NONNEG && other == ExtendedSignLattice.ZERO))
				return ExtendedSignLattice.NONNEG;

			// NEG JOIN NONZERO = NONZERO
			if ((this == ExtendedSignLattice.NEG    && other == ExtendedSignLattice.NONZERO) ||
					(this == ExtendedSignLattice.NONZERO && other == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NONZERO;

			// POS JOIN NONZERO = NONZERO
			if ((this == ExtendedSignLattice.POS    && other == ExtendedSignLattice.NONZERO) ||
					(this == ExtendedSignLattice.NONZERO && other == ExtendedSignLattice.POS))
				return ExtendedSignLattice.NONZERO;

			// DEFAULT = TOP
			return ExtendedSignLattice.TOP;
	    }

	    @Override
	    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
			if (this == other)
				return true;

			if (this == ExtendedSignLattice.BOTTOM)
				return true;

			if (other == ExtendedSignLattice.TOP)
				return true;

			if (this == ExtendedSignLattice.NEG && (other == ExtendedSignLattice.NONPOS || other == ExtendedSignLattice.NONZERO))
				return true;

			if (this == ExtendedSignLattice.ZERO && (other == ExtendedSignLattice.NONPOS || other == ExtendedSignLattice.NONNEG))
				return true;

			if (this == ExtendedSignLattice.POS && (other == ExtendedSignLattice.NONNEG || other == ExtendedSignLattice.NONZERO))
				return true;

			return false;
	    }

	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
		// identical elements
		if (this == other)
			return this;

		// top
		if (this == ExtendedSignLattice.TOP)
			return other;

		if (other == ExtendedSignLattice.TOP)
			return this;

		// NONPOS INT. NONZERO = NEG
		if ((this == ExtendedSignLattice.NONPOS  && other == ExtendedSignLattice.NONZERO) ||
				(this == ExtendedSignLattice.NONZERO && other == ExtendedSignLattice.NONPOS))
			return ExtendedSignLattice.NEG;

		// NONNEG INT. NONZERO = POS
		if ((this == ExtendedSignLattice.NONNEG  && other == ExtendedSignLattice.NONZERO) ||
				(this == ExtendedSignLattice.NONZERO && other == ExtendedSignLattice.NONNEG))
			return ExtendedSignLattice.POS;

		// NONPOS INT. NONNEG = ZERO
		if ((this == ExtendedSignLattice.NONPOS  && other == ExtendedSignLattice.NONNEG) ||
				(this == ExtendedSignLattice.NONNEG && other == ExtendedSignLattice.NONPOS))
			return ExtendedSignLattice.ZERO;

		// NEG INT. NONPOS = NEG
		if ((this == ExtendedSignLattice.NEG    && other == ExtendedSignLattice.NONPOS) ||
				(this == ExtendedSignLattice.NONPOS && other == ExtendedSignLattice.NEG))
			return ExtendedSignLattice.NEG;

		// NEG INT. NONZERO = NEG
		if ((this == ExtendedSignLattice.NEG    && other == ExtendedSignLattice.NONZERO) ||
				(this == ExtendedSignLattice.NONZERO && other == ExtendedSignLattice.NEG))
			return ExtendedSignLattice.NEG;

		// ZERO INT. NONPOS = ZERO
		if ((this == ExtendedSignLattice.ZERO    && other == ExtendedSignLattice.NONPOS) ||
				(this == ExtendedSignLattice.NONPOS  && other == ExtendedSignLattice.ZERO))
			return ExtendedSignLattice.ZERO;

		// ZERO INT. NONNEG = ZERO
		if ((this == ExtendedSignLattice.ZERO    && other == ExtendedSignLattice.NONNEG) ||
				(this == ExtendedSignLattice.NONNEG  && other == ExtendedSignLattice.ZERO))
			return ExtendedSignLattice.ZERO;

		// POS INT. NONNEG = POS
		if ((this == ExtendedSignLattice.POS    && other == ExtendedSignLattice.NONNEG) ||
				(this == ExtendedSignLattice.NONNEG && other == ExtendedSignLattice.POS))
			return ExtendedSignLattice.POS;

		// POS INT. NONZERO = POS
		if ((this == ExtendedSignLattice.POS    && other == ExtendedSignLattice.NONZERO) ||
				(this == ExtendedSignLattice.NONZERO && other == ExtendedSignLattice.POS))
			return ExtendedSignLattice.POS;

		// default = BOTTOM
		return ExtendedSignLattice.BOTTOM;
	}
}
