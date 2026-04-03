package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;


/**
 * Class that implements the lattice of the parity domain.
 * 
 * @brief The parity domain is a simple abstract domain that tracks whether an integer variable is even, odd, or unknown (top).
 * 
 * The lattice has the following structure:
 * *          TOP
 * *         /   \
 * *      EVEN   ODD
 * *         \   /
 * *         BOTTOM
 * 
 * @author Gianmaria Pizzo 872966
 */
public class ParityLattice implements BaseLattice<ParityLattice> {
	private int element;

	public static final ParityLattice TOP = new ParityLattice(0);
    public static final ParityLattice EVEN = new ParityLattice(1);
    public static final ParityLattice ODD = new ParityLattice(2);
    public static final ParityLattice BOTTOM = new ParityLattice(3);

	/**
	 * Constructor for ParityLattice.
	 * 
	 * @brief The constructor is private because we want to control the creation of the elements of the lattice,
	 * 		which are only the four defined as static final fields (TOP, EVEN, ODD, BOTTOM).
	 *
	 * @param e the integer representing the element of the lattice (0 for TOP, 1 for EVEN, 2 for ODD, 3 for BOTTOM).
	 */
	public ParityLattice(int e) {
		// As SignLattice
        this.element = e;
    }

	/**
	 * Return the top element of the lattice.
	 * 
	 * @brief The top element of the lattice is the most general element, representing all possible values.
	 * 
	 * @return the top element of the lattice
	 */
	@Override
    public ParityLattice top() {
		// As SignLattice
        return ParityLattice.TOP;
    }

	/**
	 * Return the bottom element of the lattice.
	 * 
	 * @brief The bottom element of the lattice is the most specific element, representing no possible values.
	 * 
	 * @return the bottom element of the lattice
	 */
    @Override
    public ParityLattice bottom() {
		// As SignLattice
        return ParityLattice.BOTTOM;
    }

	/**
	 * Return the hash code of the lattice element.
	 * 
	 * @brief The hash code is computed based on the element of the lattice.
	 * 
	 * @return the hash code of the lattice element
	 */
    @Override
    public int hashCode() {
		// As SignLattice
        return Objects.hash(element);
    }

	/**
	 * Return true if the lattice element is equal to the given object.
	 * 
	 * @brief The equality is computed based on the element of the lattice.
	 * 
	 * @param obj the object to compare with
	 * 
	 * @return true if the lattice element is equal to the given object, false otherwise
	 */
    @Override
    public boolean equals(Object obj) {
		// As SignLattice
        if (this == obj){
            return true;
		}
	
        if (obj == null || getClass() != obj.getClass()){
			return false;
		}

		ParityLattice other = (ParityLattice) obj;
		return element == other.element;
    }

	/**
	 * Return the string representation of the lattice element.
	 * 
	 * @brief The string representation is computed based on the element of the lattice.
	 * 
	 * @return the string representation of the lattice element
	 */	
	@Override
    public StructuredRepresentation representation() {
		// As SignLattice
        if (this == TOP){
            return Lattice.topRepresentation();
		} 
		else if (this == BOTTOM){
            return Lattice.bottomRepresentation();
		} 
		else if (this == EVEN){
            return new StringRepresentation("EVEN");
        } 

        return new StringRepresentation("ODD");
    }

	/**
	 * Return the least upper bound (lub) between 'this' and 'other'.
	 * 
	 * @brief In our case, the least upper bound between EVEN and ODD is TOP, while the lub between any element and itself is the element itself (already handled by BaseLattice).
	 * 
	 * @param other the other element to compute the lub with
	 * 
	 * @return the least upper bound between 'this' and 'other'
	 * 
	 * @throws SemanticException if any semantic error occurs during the lub computation
	 */
	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// Return top() (aka the least upper bound between EVEN and ODD). 
		return ParityLattice.TOP;
	}

	/**
	 * Return true if 'this' is less or equal than 'other' (aka 'this' is more precise than 'other').
	 * 
	 * @brief In our case, the only cases where 'this' is less or equal than 'other' are:
	 * 	- 'this' is bottom() (aka the least element of the lattice) or
	 * 	- 'other' is top() (aka the greatest element of the lattice), and
	 * 	- 'this' and 'other' are equal (already handled by BaseLattice).
	 * 	- In all other cases, elements are not comparable, so we return false.
	 * 
	 * @param other the other element to compare with
	 * 
	 * @return true if 'this' is less or equal than 'other', false otherwise
	 * 
	 * @throws SemanticException if any semantic error occurs during the comparison
	 */
	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// Case where 'this' is bottom() (aka the least element of the lattice) or
		// case where 'other' is top() (aka the greatest element of the lattice), and
		// equality case is already handled by BaseLattice.

		// If we get here elements are not comparable, so we return false.
		return false;
	}

	/**
	 * Return the greatest lower bound (glb) between 'this' and 'other'.
	 * 
	 * @param other the other element to compute the glb with
	 * 
	 * @return the greatest lower bound between 'this' and 'other'
	 */
    public Satisfiability eq(ParityLattice other) {
		// As SignLattice, but with the following cases:
        if (this.isBottom() || other.isBottom()){
            return Satisfiability.BOTTOM;
		}
        else if (this.isTop() || other.isTop()){
            return Satisfiability.UNKNOWN;
		}
        else if (!this.equals(other)){
            // Odd cannot be equal to Even, so we can say that the result is not satisfied.
            return Satisfiability.NOT_SATISFIED;
        }
        else{
            // EVEN == EVEN or ODD == ODD. 
            // Without knowing the actual value, we cannot say that the result is satisfied, 
			// but we can say that it is not unsatisfied, so we return UNKNOWN.
            return Satisfiability.UNKNOWN;
		}
    }

	/**
	 * Return the greatest lower bound (glb) between 'this' and 'other'.
	 * 
	 * @param other the other element to compare with
	 * 
	 * @return  the greatest lower bound between 'this' and 'other'
	 */
    public Satisfiability gt(ParityLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        else 
			// In the parity domain we have no information about the magnitude.
			// Any comparison (EVEN > ODD, ODD > EVEN, etc.) is undecidable.
            return Satisfiability.UNKNOWN;
    }

}