package it.unive.scsr.analysis.sign;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//TASK REQUEST: to implement two domains in LiSA :Extended Sign
/*
   TOP = any integer 
   LEQ_ZERO = Z<=0 (negative or zero)
   NEQ_ZERO = Z!=0 (negative or positive)
   GEQ_ZERO = Z>=0 (zero or positive)
   NEG = Z<0 (strictly negative)
   ZERO = Z=0 (exactly zero)
   POS = Z>0 (strictly positive)
   BOTTOM = unreachable or invalid value
 */
public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {
 private int element;

    public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
    public static ExtendedSignLattice LEQ_ZERO = new ExtendedSignLattice(1); // Z<=0
    public static ExtendedSignLattice NEQ_ZERO = new ExtendedSignLattice(2); // Z!=0
    public static ExtendedSignLattice GEQ_ZERO = new ExtendedSignLattice(3); // Z>=0
    public static ExtendedSignLattice NEG = new ExtendedSignLattice(4); // Z<0
    public static ExtendedSignLattice ZERO = new ExtendedSignLattice(5); // Z=0
    public static ExtendedSignLattice POS = new ExtendedSignLattice(6); // Z>0
    public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);

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
        public StructuredRepresentation representation() {
            if(this == ExtendedSignLattice.BOTTOM){ return Lattice.bottomRepresentation();}
            else if (this == ExtendedSignLattice.TOP){ return Lattice.topRepresentation();}
            else if (this == ExtendedSignLattice.NEG) {return new StringRepresentation("-");}
            else if (this == ExtendedSignLattice.ZERO){ return new StringRepresentation("0");}
            else if (this == ExtendedSignLattice.POS){ return new StringRepresentation("+");}
            else if (this == ExtendedSignLattice.LEQ_ZERO) {return new StringRepresentation("<=0");}
            else if (this == ExtendedSignLattice.NEQ_ZERO) {return new StringRepresentation("!=0");}
            else return new StringRepresentation(">=0");
        }

        @Override
        public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
				if ((this == NEG && other == ZERO) || (this == ZERO && other == NEG)) {
					return LEQ_ZERO;
				}
				if ((this == NEG && other == POS) || (this == POS && other == NEG)) {
					return NEQ_ZERO;
				}
				if ((this == POS && other == ZERO) || (this == ZERO && other == POS)) {
					return GEQ_ZERO;
				}
			//the lub of GEQ_ZERO, NEQ_ZERO and LEQ_ZERO is TOP
			return TOP;
		}
        @Override
        public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {

			//neg is less or equal than leq_zero and neq_zero
			if (this == NEG && (other == LEQ_ZERO ||other == NEQ_ZERO)){ return true;}
			//pos is less or equal than neq_zero and geq_zero
			if (this == POS && (other == GEQ_ZERO || other ==NEQ_ZERO)){ return true;}
			//zero is less or equal than leq_zero and geq_zero
			if(this == ZERO && (other == GEQ_ZERO || other ==LEQ_ZERO)) {return true;}
		//all the other is false
		return false;
        }

	public Satisfiability eq(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
		if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;
		//satisfied when they are both zero
		if (this == ZERO && other == ZERO) return Satisfiability.SATISFIED;
		//disjointed sets are not satisfied
		if ((this == NEG && (other == ZERO || other == POS || other == GEQ_ZERO)) ||
				(this == POS && (other == ZERO || other == NEG || other == LEQ_ZERO)) ||
				(this == ZERO && (other == POS || other == NEG || other == NEQ_ZERO)) ||
				(this == GEQ_ZERO && other == NEG) || (this == LEQ_ZERO && other == POS) || (this == NEQ_ZERO && other == ZERO)

		) {

			return Satisfiability.NOT_SATISFIED;
		}

		return Satisfiability.UNKNOWN;
	}

	/**
	 * Tests if this > other (strict greater-than), returning a Satisfiability.
	 * SATISFIED when the abstract sets guarantee the ordering.
	 * NOT_SATISFIED when the abstract sets make it impossible.
	 */
	public Satisfiability gt(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
		if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;

		if (this == POS && (other == NEG || other == LEQ_ZERO ||other == ZERO)){
			return Satisfiability.SATISFIED;
		}
		if(this == ZERO && other == NEG){
			return Satisfiability.SATISFIED;

		}
		if (this == GEQ_ZERO && other == NEG){
			return Satisfiability.SATISFIED;

		}
		if (this == NEG && (other == POS ||other == ZERO || other == GEQ_ZERO)){
			return  Satisfiability.NOT_SATISFIED;
		}
		if (this == ZERO && (other == POS ||other == ZERO || other == GEQ_ZERO)){
			return  Satisfiability.NOT_SATISFIED;
		}
		if(this == LEQ_ZERO && (other == POS || other == GEQ_ZERO || other == ZERO)){
			return  Satisfiability.NOT_SATISFIED;
		}
		return Satisfiability.UNKNOWN;
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
}