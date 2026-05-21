package it.unive.scsr.analysis.sign.extsign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice>{

	private int element;

	// declaration of lattice elements
	// 0, 1, 2, 3, 4 are just an encoding
	public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);

	// "Level 2" elements
	public static ExtendedSignLattice POSZERO = new ExtendedSignLattice(1);
	public static ExtendedSignLattice NOTZERO = new ExtendedSignLattice(2);
	public static ExtendedSignLattice NEGZERO = new ExtendedSignLattice(3);

	// "Level 1" elements
	public static ExtendedSignLattice POS = new ExtendedSignLattice(4);
	public static ExtendedSignLattice NEG = new ExtendedSignLattice(5);
	public static ExtendedSignLattice ZERO = new ExtendedSignLattice(6);

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
		else if(this == ExtendedSignLattice.POSZERO)
			return new StringRepresentation(">=0");
		else if(this == ExtendedSignLattice.NOTZERO)
			return new StringRepresentation("!=0");
		else if(this == ExtendedSignLattice.NEGZERO)
			return new StringRepresentation("<=0");
		else if(this == ExtendedSignLattice.POS)
			return new StringRepresentation("+");
		else if(this == ExtendedSignLattice.NEG)
			return new StringRepresentation("-");
		return new StringRepresentation("0");
	}

	@Override
	public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {

		// lub between level 2 elements
		if(pair(this,other,NEGZERO, NOTZERO)) return TOP;
		if (pair(this, other, NEGZERO, POSZERO)) return TOP;
		if(pair(this, other, POSZERO, NOTZERO)) return TOP;

		// lub between level 1 elements
		if(pair(this, other, NEG, POS)) return NOTZERO;
		if(pair(this, other, NEG, ZERO)) return NEGZERO;
		if(pair(this, other, POS, ZERO)) return POSZERO;

		// lub between ZERO and level 2 elements
		if(pair(this, other, ZERO, NEGZERO)) return NEGZERO;
		if(pair(this, other, ZERO, POSZERO)) return POSZERO;
		if(pair(this, other, ZERO, NOTZERO)) return TOP;

		// lub between NEG and level 2 elements
		if(pair(this, other, NEG, NEGZERO)) return NEGZERO;
		if(pair(this, other, NEG, POSZERO)) return TOP;
		if(pair(this, other, NEG, NOTZERO)) return NOTZERO;

		// lub between POS and level 2 elements
		if(pair(this, other, POS, NEGZERO)) return TOP;
		if(pair(this, other, POS, POSZERO)) return POSZERO;
		if(pair(this, other, POS, NOTZERO)) return NOTZERO;

		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
		// base cases handled : BOTTOM <= anything, anything <= TOP, x <= x

		// Remaining cases :
		// NEG  <=  NEGZERO, NOTZERO
		// POS  <=  POSZERO, NOTZERO
		// ZERO <=  NEGZERO, POSZERO

		if(this == NEG && (other == NEGZERO || other == NOTZERO)) return true;
		if(this == POS && (other == POSZERO || other == NOTZERO)) return true;
		if(this == ZERO && (other == NEGZERO || other == POSZERO)) return true;

		return false;
    }

	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {

		// glb between level 2 elements
		if(pair(this,other,NEGZERO, NOTZERO)) return NEG;
		if (pair(this, other, NEGZERO, POSZERO)) return ZERO;
		if(pair(this, other, POSZERO, NOTZERO)) return POS;

		// glb between level 1 elements
		if(pair(this, other, NEG, POS)) return BOTTOM;
		if(pair(this, other, NEG, ZERO)) return BOTTOM;
		if(pair(this, other, POS, ZERO)) return BOTTOM;

		// glb between ZERO and level 2 elements
		if(pair(this, other, ZERO, NEGZERO)) return ZERO;
		if(pair(this, other, ZERO, POSZERO)) return ZERO;
		if(pair(this, other, ZERO, NOTZERO)) return BOTTOM;

		// glb between NEG and level 2 elements
		if(pair(this, other, NEG, NEGZERO)) return NEG;
		if(pair(this, other, NEG, POSZERO)) return BOTTOM;
		if(pair(this, other, NEG, NOTZERO)) return NEG;

		// glb between POS and level 2 elements
		if(pair(this, other, POS, NEGZERO)) return BOTTOM;
		if(pair(this, other, POS, POSZERO)) return POS;
		if(pair(this, other, POS, NOTZERO)) return POS;

		return BOTTOM;
	}

	// Other method implementations to use in Sign domain to assume and check satisfiability of some stuff

	public Satisfiability eq(ExtendedSignLattice other) throws SemanticException {
		/*
		 * Abstract equality can only be definitely true for singletons (ZERO here)
		 * because ZERO.eq(ZERO) -> SATISFIED.
		 * Any other matching pair, like POS.eq(POS) is UNKNOWN because two positives are not necessarily equal concretely.
		 */
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;
		else if (this.isTop() || other.isTop())
			return Satisfiability.UNKNOWN;
		else if (this == ZERO && other == ZERO)
			return Satisfiability.SATISFIED;
		else if (this.glb(other).isBottom()) // no intersection between the two
			return Satisfiability.NOT_SATISFIED;
		else
			return Satisfiability.UNKNOWN;
	}

	/**
	 * Tests if this instance is greater than the given one, returning a
	 * {@link Satisfiability} element.
	 * @param other the instance
	 * @return the satisfiability of {@code this > other}
	 */
	public Satisfiability gt(ExtendedSignLattice other) {
		/*
		 * A compound element "C" satisfied "C>other" only if every concrete value in C satisfied it.
		 * If some do and some don't, the result is UNKNOWN.
		 */
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		// NOTZERO doesn't give any valuable information on the sign to deduce anything from comparisons
		else if (this.isTop() || other.isTop() || this == NOTZERO || other == NOTZERO)
			return Satisfiability.UNKNOWN;

		// unknown if other is also <= 0. Unsatisfied otherwise (other is >0, >=0, or 0)
		else if (this == NEG)
			return (other == NEG || other == NEGZERO) ? Satisfiability.UNKNOWN : Satisfiability.NOT_SATISFIED;

		else if (this == NEGZERO)
			return (other == POS) ? Satisfiability.NOT_SATISFIED : Satisfiability.UNKNOWN;

		// satisfied if other is negative strictly. Otherwise, other is >0, >=0 or =0 too so this>other is unsatisfied.
		// if other is <= 0, it could be 0 (in which case this<other is unsatisfied) or <0 (in which case it is satisfied) => unknown
		else if (this == ZERO){
			if(other == NEGZERO)
				return Satisfiability.UNKNOWN;
			return (other == NEG ) ? Satisfiability.SATISFIED : Satisfiability.NOT_SATISFIED;
		}

		// satisfied if other is negative (strictly. Otherwise, unknown :
		// other <= 0 -> could be zero (POSZERO as well, so both could be equal in which case it is unsatisfied) ; or could be negative (satisfied) -> unknown
		// other >= 0, 0 or >0 -> unknown
		else if (this == POSZERO)
			return (other == NEG) ? Satisfiability.NOT_SATISFIED : Satisfiability.UNKNOWN;

		// else : all cases have been handled, this == POS necessarily
		// if other is > 0, >= 0 -> unknown
		// if other is 0 -> satisfied (POS > 0)
		// if other is <= 0 or <0 −> satisfied for sure too
		else
			return (other == POS || other == POSZERO) ? Satisfiability.UNKNOWN : Satisfiability.SATISFIED;
	}


	/** Returns true if, for an unordered lattice element pair, {x,y} = {a,b} **/
	// For example; used for lub(x,y) = lub(y,x)
	// to avoid swapping parameters and accumulating conditions that check for the same thing
	public static boolean pair(ExtendedSignLattice x, ExtendedSignLattice y, ExtendedSignLattice a, ExtendedSignLattice b) {
		return (x == a && y == b) || (x == b && y == a);
	}

}
