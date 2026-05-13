package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;

import java.util.Objects;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {
    private final int element;

    public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
    public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(1);
    public static ExtendedSignLattice LT_ZERO = new ExtendedSignLattice(2);
    public static ExtendedSignLattice GT_ZERO = new ExtendedSignLattice(3);
    public static ExtendedSignLattice LE_ZERO = new ExtendedSignLattice(4);
    public static ExtendedSignLattice GE_ZERO = new ExtendedSignLattice(5);
    public static ExtendedSignLattice NE_ZERO = new ExtendedSignLattice(6);
    public static ExtendedSignLattice ZERO = new ExtendedSignLattice(7);

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
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        if(this == other)
            return this;
        // Z<0 ⊔ Z=0 = Z≤0
        if((this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.ZERO) || (this == ExtendedSignLattice.ZERO && other == ExtendedSignLattice.LT_ZERO))
            return ExtendedSignLattice.LE_ZERO;
        // Z=0 ⊔ Z>0 = Z≥0
        if((this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.ZERO) || (this == ExtendedSignLattice.ZERO && other == ExtendedSignLattice.GT_ZERO))
            return ExtendedSignLattice.GE_ZERO;
        // Z<0 ⊔ Z>0 = Z≠0
        if((this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.GT_ZERO) || (this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.LT_ZERO))
            return ExtendedSignLattice.NE_ZERO;
        // Z<0 ⊔ Z≤0 = Z≤0
        if((this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.LE_ZERO) || (this == ExtendedSignLattice.LE_ZERO && other == ExtendedSignLattice.LT_ZERO))
            return ExtendedSignLattice.LE_ZERO;
        // Z=0 ⊔ Z≥0 = Z≥0
        if((this == ExtendedSignLattice.ZERO && other == ExtendedSignLattice.GE_ZERO) || (this == ExtendedSignLattice.GE_ZERO && other == ExtendedSignLattice.ZERO))
            return ExtendedSignLattice.GE_ZERO;
        // Z>0 ⊔ Z≥0 = Z≥0
        if((this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.GE_ZERO) || (this == ExtendedSignLattice.GE_ZERO && other == ExtendedSignLattice.GT_ZERO))
            return ExtendedSignLattice.GE_ZERO;
        // Z<0 ⊔ Z≠0 = Z≠0
        if((this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.NE_ZERO) || (this == ExtendedSignLattice.NE_ZERO && other == ExtendedSignLattice.LT_ZERO))
            return ExtendedSignLattice.NE_ZERO;
        // Z>0 ⊔ Z≠0 = Z≠0
        if((this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.NE_ZERO) || (this == ExtendedSignLattice.NE_ZERO && other == ExtendedSignLattice.GT_ZERO))
            return ExtendedSignLattice.NE_ZERO;
        // Z≤0 ⊔ Z≥0 = Z (TOP)
        if((this == ExtendedSignLattice.LE_ZERO && other == ExtendedSignLattice.GE_ZERO) || (this == ExtendedSignLattice.GE_ZERO && other == ExtendedSignLattice.LE_ZERO))
            return ExtendedSignLattice.TOP;
        // Z≤0 ⊔ Z≠0 = Z (TOP)
        if((this == ExtendedSignLattice.LE_ZERO && other == ExtendedSignLattice.NE_ZERO) || (this == ExtendedSignLattice.NE_ZERO && other == ExtendedSignLattice.LE_ZERO))
            return ExtendedSignLattice.TOP;
        // Z≥0 ⊔ Z≠0 = Z (TOP)
        if((this == ExtendedSignLattice.GE_ZERO && other == ExtendedSignLattice.NE_ZERO) || (this == ExtendedSignLattice.NE_ZERO && other == ExtendedSignLattice.GE_ZERO))
            return ExtendedSignLattice.TOP;
        // Z<0 ⊔ Z≥0 = Z (TOP)
        if((this == ExtendedSignLattice.LT_ZERO && other == ExtendedSignLattice.GE_ZERO) || (this == ExtendedSignLattice.GE_ZERO && other == ExtendedSignLattice.LT_ZERO))
            return ExtendedSignLattice.TOP;
        // Z>0 ⊔ Z≤0 = Z (TOP)
        if((this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.LE_ZERO) || (this == ExtendedSignLattice.LE_ZERO && other == ExtendedSignLattice.GT_ZERO))
            return ExtendedSignLattice.TOP;
        
        return ExtendedSignLattice.TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if(this == other)
            return true;
        if((this == ExtendedSignLattice.LT_ZERO && other == LE_ZERO) || (this == ExtendedSignLattice.LT_ZERO && other == NE_ZERO))
            return true;
        if((this == ExtendedSignLattice.ZERO && other == LE_ZERO) || (this == ExtendedSignLattice.ZERO && other == GE_ZERO))
            return true;
        if((this == ExtendedSignLattice.GT_ZERO && other == ExtendedSignLattice.GE_ZERO) || (this == ExtendedSignLattice.GT_ZERO && other == NE_ZERO))
            return true;
        if((this == ExtendedSignLattice.LE_ZERO || this == NE_ZERO || this == GE_ZERO) && other == ExtendedSignLattice.TOP)
            return true;
        if((this == ExtendedSignLattice.LT_ZERO || this == ZERO || this == GT_ZERO) && other == ExtendedSignLattice.TOP)
            return true;

        return false;
    }

    @Override
    public StructuredRepresentation representation() {
        if(this == ExtendedSignLattice.BOTTOM)
            return Lattice.bottomRepresentation();
        if(this == ExtendedSignLattice.TOP)
            return Lattice.topRepresentation();
        if(this == ExtendedSignLattice.LT_ZERO)
            return new StringRepresentation("Z < 0");
        if(this == ExtendedSignLattice.GT_ZERO)
            return new StringRepresentation("Z > 0");
        if(this == ExtendedSignLattice.ZERO)
            return new StringRepresentation("Z = 0");
        if(this == ExtendedSignLattice.LE_ZERO)
            return new StringRepresentation("Z <= 0");
        if(this == ExtendedSignLattice.GE_ZERO)
            return new StringRepresentation("Z >= 0");
        if(this == ExtendedSignLattice.NE_ZERO)
            return new StringRepresentation("Z != 0");
        return Lattice.topRepresentation();
    }

    public Satisfiability eq(ExtendedSignLattice other) {
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

    public Satisfiability gt(ExtendedSignLattice other) {
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
