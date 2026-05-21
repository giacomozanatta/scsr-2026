package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    private int e;

    public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);

    public static ExtendedSignLattice NOTPOS= new ExtendedSignLattice(1);
    public static ExtendedSignLattice NOTZERO = new ExtendedSignLattice(2);
    public static ExtendedSignLattice NOTNEG = new ExtendedSignLattice(3);

    public static ExtendedSignLattice NEG= new ExtendedSignLattice(4);
    public static ExtendedSignLattice ZERO = new ExtendedSignLattice(5);
    public static ExtendedSignLattice POS = new ExtendedSignLattice(6);

    public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);

    public ExtendedSignLattice(int e){ this.e = e; }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        if(this == TOP || other == TOP)
            return TOP;
        if(this == BOTTOM || other == BOTTOM)
            return BOTTOM;

        if(this == NEG || other == NEG){
           if(this == NOTPOS || other == NOTPOS)
               return NOTPOS;
           else if(this == NOTZERO || other == NOTZERO)
               return NOTZERO;
        }
        if(this == ZERO || other == ZERO){
            if(this == NOTPOS || other == NOTPOS)
                return NOTPOS;
            else if(this == NOTNEG || other == NOTNEG)
                return NOTNEG;
        }
        if(this == POS || other == POS){
            if(this == NOTNEG || other == NOTNEG)
                return NOTNEG;
            else if(this == NOTZERO || other == NOTZERO)
                return NOTZERO;
        }

        // SHouldnt be reached
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if(this == NEG && other == NOTPOS)
            return true;
        if(this == ZERO && (other == NOTPOS || other == NOTNEG))
            return true;
        if(this == POS && other == NOTNEG)
            return true;

        return false; // Is neg/pos less than or equal ot notzero? Prob not
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
    public StructuredRepresentation representation() {
        if(this == BOTTOM)
            return Lattice.bottomRepresentation();
        else if(this == ExtendedSignLattice.TOP)
            return Lattice.topRepresentation();

        else if(this == ExtendedSignLattice.NOTNEG )
            return new StringRepresentation(">=");
        else if(this == ExtendedSignLattice.NOTZERO )
            return new StringRepresentation("!=");
        else if(this == ExtendedSignLattice.NOTPOS )
            return new StringRepresentation("<=");

        else if(this == ExtendedSignLattice.NEG )
            return new StringRepresentation("<");
        else if(this == ExtendedSignLattice.ZERO )
            return new StringRepresentation("0");

        return new StringRepresentation(">"); // POS
    }

    @Override
    public int hashCode() {
        return Objects.hash(e);
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
        return e == other.e;
    }

    public Satisfiability eq(ExtendedSignLattice other){
        if(this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if(this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        if(!this.equals(other))
            return Satisfiability.NOT_SATISFIED;
        if(this == ZERO)
            return Satisfiability.SATISFIED;

        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(ExtendedSignLattice other){
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;

        if(this == NEG){
            if(other == NEG || other == NOTZERO || other == NOTPOS){
                return Satisfiability.UNKNOWN;
            }
            // Zero, pos, notneg
            return Satisfiability.NOT_SATISFIED;
        }
        if(this == NOTPOS){
            if(other == NEG
            || other == NOTZERO
            || other == ZERO
            || other == NOTNEG
            || other == NOTPOS){
                return Satisfiability.UNKNOWN;
            }
            if(other == POS)
                return Satisfiability.NOT_SATISFIED;
        }

        if(this == POS){
            if(other == POS
            || other == NOTNEG
            || other == NOTZERO){
                return Satisfiability.UNKNOWN;
            }
            // Zero, notpos, neg
            return Satisfiability.SATISFIED;
        }
        if(this == NOTNEG){
            if(other == ZERO || other == NOTZERO || other == POS || other == NOTPOS){
                return Satisfiability.UNKNOWN;
            }
            return Satisfiability.SATISFIED;
        }

        if(this == ZERO){
            if(other == ZERO || other == NOTNEG || other == POS){
                return Satisfiability.NOT_SATISFIED;
            }
            if(other == NOTZERO || other == NOTPOS){
                return Satisfiability.UNKNOWN;
            }

            // Neg
            return Satisfiability.SATISFIED;
        }

        // Notzero
        return Satisfiability.UNKNOWN;
    }
}
