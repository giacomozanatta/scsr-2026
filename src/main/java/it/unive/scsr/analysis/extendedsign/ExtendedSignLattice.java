package it.unive.scsr.analysis.extendedsign;

import java.lang.reflect.Executable;
import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;


public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    private int element;

    public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
    public static ExtendedSignLattice GEQ_ZERO = new ExtendedSignLattice(1);
    public static ExtendedSignLattice GT_ZERO = new ExtendedSignLattice(2);
    public static ExtendedSignLattice EQ_ZERO = new ExtendedSignLattice(3);
    public static ExtendedSignLattice NON_ZERO = new ExtendedSignLattice(4);
    public static ExtendedSignLattice LEQ_ZERO = new ExtendedSignLattice(5);
    public static ExtendedSignLattice LT_ZERO = new ExtendedSignLattice(6);
    public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);

    public ExtendedSignLattice(int e) {
        this.element = e;
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
        if(this == ExtendedSignLattice.BOTTOM)
            return Lattice.bottomRepresentation();
        else if(this == ExtendedSignLattice.EQ_ZERO)
            return new StringRepresentation("0");
        else if(this == ExtendedSignLattice.NON_ZERO)
            return new StringRepresentation("≠0");
        else if(this == ExtendedSignLattice.LEQ_ZERO)
            return new StringRepresentation("≤0");
        else if(this == ExtendedSignLattice.LT_ZERO)
            return new StringRepresentation("<0");
        else if(this == ExtendedSignLattice.GEQ_ZERO)
            return new StringRepresentation("≥0");
        else if(this == ExtendedSignLattice.GT_ZERO)
            return new StringRepresentation(">0");
        return Lattice.topRepresentation();
    }

    @Override
    public int hashCode(){
        return Objects.hash(element);
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ExtendedSignLattice other = (ExtendedSignLattice) obj;
        return element == other.element;
    }

    @Override
    public ExtendedSignLattice widening(ExtendedSignLattice other) throws SemanticException {
        return lub(other);
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this == other)
            return true;
        if (this == ExtendedSignLattice.BOTTOM)
            return true;
        if (other == ExtendedSignLattice.TOP)
            return true;

        if (this == ExtendedSignLattice.LT_ZERO)
            return other == ExtendedSignLattice.LEQ_ZERO || other ==  ExtendedSignLattice.NON_ZERO;
        else if (this == ExtendedSignLattice.EQ_ZERO)
            return other == LEQ_ZERO || other == GEQ_ZERO;
        else if (this == ExtendedSignLattice.GT_ZERO)
            return other == GEQ_ZERO || other == ExtendedSignLattice.NON_ZERO;
        return false;
    }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        if(this == other)
            return this;
        if (this == ExtendedSignLattice.BOTTOM)
            return other;
        if (other == ExtendedSignLattice.BOTTOM)
            return this;
        if (this == ExtendedSignLattice.TOP || other == ExtendedSignLattice.TOP)
            return ExtendedSignLattice.TOP;

        // strict positive/negative cases
        if ((this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.LT_ZERO)
            ||(this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.GT_ZERO))
            return ExtendedSignLattice.NON_ZERO;

        // LEQ_ZERO cases
        if ((this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.EQ_ZERO)
            || (this == ExtendedSignLattice.EQ_ZERO && other == ExtendedSignLattice.LT_ZERO)
            || (this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.LEQ_ZERO)
            || (this == ExtendedSignLattice.LEQ_ZERO && other == ExtendedSignLattice.LT_ZERO)
            || (this == ExtendedSignLattice.LEQ_ZERO && other == ExtendedSignLattice.EQ_ZERO)
            || (this == ExtendedSignLattice.EQ_ZERO && other == ExtendedSignLattice.LEQ_ZERO))
            return ExtendedSignLattice.LEQ_ZERO;

        // GEQ_ZERO cases
        if ((this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.EQ_ZERO)
                || (this == ExtendedSignLattice.EQ_ZERO && other == ExtendedSignLattice.GT_ZERO)
                || (this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.GEQ_ZERO)
                || (this == ExtendedSignLattice.GEQ_ZERO && other == ExtendedSignLattice.GT_ZERO)
                || (this == ExtendedSignLattice.GEQ_ZERO && other == ExtendedSignLattice.EQ_ZERO)
                || (this == ExtendedSignLattice.EQ_ZERO && other == ExtendedSignLattice.GEQ_ZERO))
            return ExtendedSignLattice.GEQ_ZERO;

        //NON_ZERO cases
        if ((this == ExtendedSignLattice.NON_ZERO && other == ExtendedSignLattice.GT_ZERO)
                || (this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.NON_ZERO)
                || (this == ExtendedSignLattice.NON_ZERO && other == ExtendedSignLattice.LT_ZERO)
                || (this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.NON_ZERO))
            return ExtendedSignLattice.NON_ZERO;

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
        if (this == other)
            return this;

        if (this == ExtendedSignLattice.TOP)
            return other;

        if (other == ExtendedSignLattice.TOP)
            return this;

        if (this == ExtendedSignLattice.BOTTOM || other == ExtendedSignLattice.BOTTOM)
            return ExtendedSignLattice.BOTTOM;

        if (this == ExtendedSignLattice.GEQ_ZERO && other == ExtendedSignLattice.NON_ZERO
                || this == ExtendedSignLattice.NON_ZERO && other == ExtendedSignLattice.GEQ_ZERO)
            return ExtendedSignLattice.GT_ZERO;

        if (this == ExtendedSignLattice.LEQ_ZERO && other == ExtendedSignLattice.NON_ZERO
                || this == ExtendedSignLattice.NON_ZERO && other == ExtendedSignLattice.LEQ_ZERO)
            return ExtendedSignLattice.LT_ZERO;

        if (this == ExtendedSignLattice.GEQ_ZERO && other == ExtendedSignLattice.LEQ_ZERO
                || this == ExtendedSignLattice.LEQ_ZERO && other == ExtendedSignLattice.GEQ_ZERO)
            return ExtendedSignLattice.EQ_ZERO;

        return ExtendedSignLattice.BOTTOM;
    }

    public Satisfiability eq(ExtendedSignLattice other) {
        if (this == ExtendedSignLattice.BOTTOM || other == ExtendedSignLattice.BOTTOM)
            return Satisfiability.BOTTOM;

        if (this == other && this != ExtendedSignLattice.TOP && this != ExtendedSignLattice.BOTTOM)
            return Satisfiability.SATISFIED;

        if ((this == ExtendedSignLattice.EQ_ZERO && other == ExtendedSignLattice.NON_ZERO)
            ||(this == ExtendedSignLattice.NON_ZERO && other == ExtendedSignLattice.EQ_ZERO)
            || (this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.LT_ZERO)
            || (this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.GT_ZERO))
            return Satisfiability.NOT_SATISFIED;

        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(ExtendedSignLattice other) {
        if (this == ExtendedSignLattice.BOTTOM || other == ExtendedSignLattice.BOTTOM)
            return Satisfiability.BOTTOM;

        if (this == ExtendedSignLattice.GT_ZERO &&
            (other == ExtendedSignLattice.EQ_ZERO || other == ExtendedSignLattice.LT_ZERO || other == ExtendedSignLattice.LEQ_ZERO))
            return Satisfiability.SATISFIED;

        if (this == ExtendedSignLattice.GEQ_ZERO && other == ExtendedSignLattice.LT_ZERO)
            return Satisfiability.SATISFIED;

        if (this == ExtendedSignLattice.LT_ZERO &&
            (other == ExtendedSignLattice.GT_ZERO || other == ExtendedSignLattice.GEQ_ZERO || other == ExtendedSignLattice.EQ_ZERO))
            return Satisfiability.NOT_SATISFIED;

        if (this == ExtendedSignLattice.EQ_ZERO &&
            (other == ExtendedSignLattice.GT_ZERO || other == ExtendedSignLattice.GEQ_ZERO))
            return Satisfiability.NOT_SATISFIED;

        return Satisfiability.UNKNOWN;
    }
}
