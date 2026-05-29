package it.unive.scsr.analysis.extendedsign;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice>{
    
    private int val; 

    /*  
                   TOP 
         /          |           \
    LEQ_ZERO      NOT_ZERO     GEQ_ZERO
        |     X             X     |
    LT_ZERO        ZERO         GT_ZERO
          \         |            /
                  BOTTOM
    

    numerical representation : 


            7 
         /  |  \
        3   5   6 SUPERIOR PART OF THE LATTICE (including 7)
        | X   X |
        1   2   4 INFERIOR PART OF THE LATTICE (including 0)
        \   |   /
            0


	How this is composed : 

			(greater than 0) (0) (lower than 0)

	bottom 			0  0  0
	lt zero			0  0  1
	zero			0  1  0
	leq zero		0  1  1
	gt zero			1  0  0
	not zero		1  0  1
	geq zero		1  1  0
	top				1  1  1


    */

    public static final ExtendedSignLattice BOTTOM = new ExtendedSignLattice (0);
    public static final ExtendedSignLattice LT_ZERO = new ExtendedSignLattice (1);
    public static final ExtendedSignLattice ZERO = new ExtendedSignLattice (2);
	public static final ExtendedSignLattice LEQ_ZERO = new ExtendedSignLattice (3);
    public static final ExtendedSignLattice GT_ZERO = new ExtendedSignLattice (4);
    public static final ExtendedSignLattice NOT_ZERO = new ExtendedSignLattice (5);
    public static final ExtendedSignLattice GEQ_ZERO = new ExtendedSignLattice (6);
    public static final ExtendedSignLattice TOP = new ExtendedSignLattice (7); 

	public ExtendedSignLattice() {this (7);}
    public ExtendedSignLattice(int v) {this.val = v;}

    @Override
    public ExtendedSignLattice top() {return TOP;}
    
    @Override
    public ExtendedSignLattice bottom() {return BOTTOM;}

    @Override
	public StructuredRepresentation representation() {	
		if (this == ExtendedSignLattice.BOTTOM) return Lattice.bottomRepresentation();
		else if (this == ExtendedSignLattice.TOP) return Lattice.topRepresentation();
		else if (this == ExtendedSignLattice.LT_ZERO) return new StringRepresentation("< 0");
        else if (this == ExtendedSignLattice.ZERO) return new StringRepresentation("= 0");
        else if (this == ExtendedSignLattice.GT_ZERO) return new StringRepresentation("> 0");
        else if (this == ExtendedSignLattice.LEQ_ZERO) return new StringRepresentation("≤ 0");
        else if (this == ExtendedSignLattice.NOT_ZERO) return new StringRepresentation("≠ 0");
        else return new StringRepresentation("≥ 0");
	}

	@Override
	public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
		// LUB is the union of the condition THIS and the condition OTHER, we can use the bitwise OR between the two vals
		switch (this.val | other.val) {
			case 0: return BOTTOM;
			case 1: return LT_ZERO;
			case 2: return ZERO;
			case 3: return LEQ_ZERO;
			case 4: return GT_ZERO;
			case 5: return NOT_ZERO;
			case 6: return GEQ_ZERO;
			default: return TOP;
		}
	}

	@Override
	public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
	    // implement less or Equals logic
	    return (this.val & other.val) == this.val; // we control with and if all the conditions of this are satisfied in other
	}

	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
	    // GLB is the intersection of the condition THIS and the condition OTHER, we can use the bitwise AND between the two vals
	    switch (this.val & other.val) {
			case 0: return BOTTOM;
			case 1: return LT_ZERO;
			case 2: return ZERO;
			case 3: return LEQ_ZERO;
			case 4: return GT_ZERO;
			case 5: return NOT_ZERO;
			case 6: return GEQ_ZERO;
			default: return TOP;
		}
	}

	public Satisfiability eq(ExtendedSignLattice other) {
			if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
			else if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;
			else if (!this.equals(other)) return Satisfiability.NOT_SATISFIED;
			else if (this == ZERO) return Satisfiability.SATISFIED; // the only case where we are sure to have equality in the group
			else return Satisfiability.UNKNOWN;
	}
		
	public Satisfiability gt(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
		else if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;
		else if (this == LT_ZERO) {
			if (other == GT_ZERO || other == GEQ_ZERO || other == ZERO) return Satisfiability.NOT_SATISFIED;
			else return Satisfiability.UNKNOWN;
		}
		else if (this == ZERO) {
			if (other == LT_ZERO) return Satisfiability.SATISFIED;
			else if (other == GT_ZERO || other == GEQ_ZERO || other == ZERO) return Satisfiability.NOT_SATISFIED;
			else return Satisfiability.UNKNOWN;
		}
		else if (this == LEQ_ZERO) {
			if (other == GT_ZERO) return Satisfiability.NOT_SATISFIED;
			else return Satisfiability.UNKNOWN;
		}
		else if (this == GT_ZERO) {
			if (other == LT_ZERO || other == LEQ_ZERO || other == ZERO) return Satisfiability.SATISFIED;
			else return Satisfiability.UNKNOWN;
		}
		else if (this == NOT_ZERO) return Satisfiability.UNKNOWN;
		else if (this == GEQ_ZERO) {
			if (other == LT_ZERO) return Satisfiability.SATISFIED;
			else return Satisfiability.UNKNOWN;
		}
		else return Satisfiability.UNKNOWN;
		}

    @Override
	public int hashCode() {return Objects.hash(val);}

	@Override
	public boolean equals(Object obj) {
	if (this == obj) return true;
	if (obj == null) return false;
	if (getClass() != obj.getClass()) return false;
	ExtendedSignLattice other = (ExtendedSignLattice) obj;
	return val == other.val;
	}

}
