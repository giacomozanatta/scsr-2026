package it.unive.scsr.analysis.sign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;


/**
 *          T
 *      /   |  \
 *     POS  0  NEG
 *     \    |   /
 *       BOTTOM
 */
public class SignLattice implements BaseLattice<SignLattice> {

    private int element;

    // declaration of lattice elements
    // 0, 1, 2, 3, 4 are just an encoding

    //Used for I don't know: maybe positive, negative or zero
    public static SignLattice TOP = new SignLattice(0);

    public static SignLattice POS = new SignLattice(1);
    public static SignLattice NEG = new SignLattice(2);
    public static SignLattice ZERO = new SignLattice(3);

    //Used for error
    public static SignLattice BOTTOM = new SignLattice(4);

    public SignLattice(int e){
        element = e;
    }

    @Override
    public SignLattice top() {
        return SignLattice.TOP;
    }

    @Override
    public SignLattice bottom() {
        return SignLattice.BOTTOM;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.element);
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this) return true;
        if(obj == null || obj.getClass()!=this.getClass()) return false;
        SignLattice objCasted = (SignLattice) obj;
        return (this.element == objCasted.element);
    }

    /**
     * Used to generate graphical representation
     */
    @Override
    public StructuredRepresentation representation() {
        if(this == SignLattice.BOTTOM)
            return Lattice.bottomRepresentation();
        else if (this == SignLattice.TOP)
            return Lattice.topRepresentation();
        else if (this == SignLattice.ZERO)
            return new StringRepresentation("0");
        else if (this == SignLattice.POS)
            return new StringRepresentation("+");

        return new StringRepresentation("-");
    }

    /**
     *
     * Least upper bound between this and given SignLattice
     */
    @Override
    public SignLattice lubAux(SignLattice other) throws SemanticException {
        if(this==SignLattice.POS && other == SignLattice.ZERO)
            return SignLattice.TOP;
        // in general should be handled all case... In this sign domain with POS, ZERO, NEG, it is simply TOP
        return SignLattice.TOP;
    }

    /**
     * Less or equal logic
     */
    @Override
    public boolean lessOrEqualAux(SignLattice other) throws SemanticException {
        //In this case is simple.


        // Controls already done by lessOrEqual
        /*if(this.equals(other) || this.equals(SignLattice.BOTTOM))
            return true;
        else if (this.equals(SignLattice.TOP))
            return false;
        */
        return false;
    }

    // For glb in this case we use default LiSA implementation


    // Other method implementations to use in Sign domain to assume and check satisfability of some stuff

    public Satisfiability eq(SignLattice other){
        if(this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        else if(this.isTop() || other.isTop())
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
    public Satisfiability gt(SignLattice other) {
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
