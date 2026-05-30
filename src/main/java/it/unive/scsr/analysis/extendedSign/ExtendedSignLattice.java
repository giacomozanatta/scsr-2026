package it.unive.scsr.analysis.extendedSign;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;


/**
 * @author Mattia Acquilesi - 896827
 * @author Alan Dal Col - 895879
*/

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    // -----------------------------------------------------------------------
    // Element encoding
    // -----------------------------------------------------------------------
    //  0 = TOP      (Z)
    //  1 = NON_NEG  (Z≥0)
    //  2 = NON_POS  (Z≤0)
    //  3 = NON_ZERO (Z≠0)
    //  4 = POS      (Z>0)
    //  5 = ZERO     (Z=0)
    //  6 = NEG      (Z<0)
    //  7 = BOTTOM   (∅)

    private final int element;

    public static final ExtendedSignLattice TOP      = new ExtendedSignLattice(0);
    public static final ExtendedSignLattice NON_NEG  = new ExtendedSignLattice(1); // Z≥0
    public static final ExtendedSignLattice NON_POS  = new ExtendedSignLattice(2); // Z≤0
    public static final ExtendedSignLattice NON_ZERO = new ExtendedSignLattice(3); // Z≠0
    public static final ExtendedSignLattice POS      = new ExtendedSignLattice(4); // Z>0
    public static final ExtendedSignLattice ZERO     = new ExtendedSignLattice(5); // Z=0
    public static final ExtendedSignLattice NEG      = new ExtendedSignLattice(6); // Z<0
    public static final ExtendedSignLattice BOTTOM   = new ExtendedSignLattice(7);

    public ExtendedSignLattice(int element) {
        this.element = element;
    }



    @Override
    public ExtendedSignLattice top() {
        return TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return BOTTOM;
    }


    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(other))
            return this;

        if (isPosAndZero(this, other))
            return NON_NEG;

        if (isNegAndZero(this, other))
            return NON_POS;

        if (isPosAndNeg(this, other))
            return NON_ZERO;

        if (isElements(this, other, NON_NEG, NON_POS))
            return TOP;

        if (isElements(this, other, NON_NEG, NON_ZERO))
            return TOP;

        if (isElements(this, other, NON_POS, NON_ZERO))
            return TOP;

        if (isElements(this, other, NON_NEG, NEG))
            return TOP;

        if (isElements(this, other, NON_POS, POS))
            return TOP;

        if (isElements(this, other, NON_ZERO, ZERO))
            return TOP;

        if (isElements(this, other, POS, NON_NEG))
            return NON_NEG;

        if (isElements(this, other, ZERO, NON_NEG))
            return NON_NEG;

        if (isElements(this, other, NEG, NON_POS))
            return NON_POS;

        if (isElements(this, other, ZERO, NON_POS))
            return NON_POS;

        if (isElements(this, other, POS, NON_ZERO))
            return NON_ZERO;

        if (isElements(this, other, NEG, NON_ZERO))
            return NON_ZERO;

        return TOP;
    }

    @Override
    public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(other))
            return this;

        if (isElements(this, other, NON_NEG, NON_ZERO))
            return POS;

        if (isElements(this, other, NON_POS, NON_ZERO))
            return NEG;

        if (isElements(this, other, NON_NEG, NON_POS))
            return ZERO;

        if (this.lessOrEqual(other))
            return this;
        if (other.lessOrEqual(this))
            return other;

        return BOTTOM;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(POS))
            return other.equals(NON_NEG) || other.equals(NON_ZERO);

        if (this.equals(NEG))
            return other.equals(NON_POS) || other.equals(NON_ZERO);

        if (this.equals(ZERO))
            return other.equals(NON_NEG) || other.equals(NON_POS);

        return false;
    }


    public boolean isDefinitelyZero() {
        return this.equals(ZERO);
    }


    public boolean canBeZero() {
        return this.equals(ZERO)
                || this.equals(NON_NEG)
                || this.equals(NON_POS)
                || this.equals(TOP);
    }


    public boolean isDefinitelyPositive() {
        return this.equals(POS);
    }


    public boolean canBePositive() {
        return this.equals(POS)
                || this.equals(NON_NEG)
                || this.equals(NON_ZERO)
                || this.equals(TOP);
    }


    public boolean isDefinitelyNegative() {
        return this.equals(NEG);
    }

    public boolean canBeNegative() {
        return this.equals(NEG)
                || this.equals(NON_POS)
                || this.equals(NON_ZERO)
                || this.equals(TOP);
    }

    public Satisfiability eq(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        if (this.equals(ZERO) && other.equals(ZERO))
            return Satisfiability.SATISFIED;
        if (!canOverlap(this, other))
            return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }


    public Satisfiability gt(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;

        if (this.equals(POS) && (other.equals(NEG) || other.equals(ZERO) || other.equals(NON_POS)))
            return Satisfiability.SATISFIED;
        if (this.equals(ZERO) && other.equals(NEG))
            return Satisfiability.SATISFIED;
        if (this.equals(NON_NEG) && other.equals(NEG))
            return Satisfiability.SATISFIED;

        if (this.equals(NEG) && (other.equals(ZERO) || other.equals(POS) || other.equals(NON_NEG)))
            return Satisfiability.NOT_SATISFIED;
        if (this.equals(ZERO) && (other.equals(ZERO) || other.equals(POS) || other.equals(NON_NEG)))
            return Satisfiability.NOT_SATISFIED;
        if (this.equals(NON_POS) && other.equals(POS))
            return Satisfiability.NOT_SATISFIED;

        return Satisfiability.UNKNOWN;
    }


    @Override
    public StructuredRepresentation representation() {
        if (this.equals(BOTTOM))   return Lattice.bottomRepresentation();
        if (this.equals(TOP))      return Lattice.topRepresentation();
        if (this.equals(POS))      return new StringRepresentation("+");
        if (this.equals(NEG))      return new StringRepresentation("-");
        if (this.equals(ZERO))     return new StringRepresentation("0");
        if (this.equals(NON_NEG))  return new StringRepresentation(">=0");
        if (this.equals(NON_POS))  return new StringRepresentation("<=0");
        if (this.equals(NON_ZERO)) return new StringRepresentation("!=0");
        return Lattice.topRepresentation();
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return element == ((ExtendedSignLattice) obj).element;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }


    private static boolean isPosAndZero(ExtendedSignLattice a, ExtendedSignLattice b) {
        return (a.equals(POS) && b.equals(ZERO)) || (a.equals(ZERO) && b.equals(POS));
    }

    private static boolean isNegAndZero(ExtendedSignLattice a, ExtendedSignLattice b) {
        return (a.equals(NEG) && b.equals(ZERO)) || (a.equals(ZERO) && b.equals(NEG));
    }

    private static boolean isPosAndNeg(ExtendedSignLattice a, ExtendedSignLattice b) {
        return (a.equals(POS) && b.equals(NEG)) || (a.equals(NEG) && b.equals(POS));
    }

    private static boolean isElements(ExtendedSignLattice a, ExtendedSignLattice b,
                                      ExtendedSignLattice x, ExtendedSignLattice y) {
        return (a.equals(x) && b.equals(y)) || (a.equals(y) && b.equals(x));
    }


    private static boolean canOverlap(ExtendedSignLattice a, ExtendedSignLattice b) {
        if (a.canBePositive() && b.canBePositive()) return true;
        if (a.canBeZero() && b.canBeZero())         return true;
        if (a.canBeNegative() && b.canBeNegative()) return true;
        return false;
    }
}