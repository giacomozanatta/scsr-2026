package it.unive.scsr.analysis.extendedSign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {


    private int element;

    public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
    public static ExtendedSignLattice NON_ZERO = new ExtendedSignLattice(1);
    public static ExtendedSignLattice NON_POS = new ExtendedSignLattice(2);
    public static ExtendedSignLattice NON_NEG = new ExtendedSignLattice(3);
    public static ExtendedSignLattice NEG = new ExtendedSignLattice(4);
    public static ExtendedSignLattice ZERO = new ExtendedSignLattice(5);
    public static ExtendedSignLattice POS = new ExtendedSignLattice(6);
    public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);


    public ExtendedSignLattice(int element) {
        this.element = element;
    }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        if ((this.equals(ZERO) && other.equals(POS)) || (this.equals(POS) && other.equals(ZERO))) return NON_NEG;
        if ((this.equals(ZERO) && other.equals(NEG)) || (this.equals(NEG) && other.equals(ZERO))) return NON_POS;
        if ((this.equals(POS) && other.equals(NEG)) || (this.equals(NEG) && other.equals(POS))) return NON_ZERO;
        if (this.lessOrEqualAux(other)) return other;
        if (other.lessOrEqualAux(this)) return this;
        return TOP;
    }

    @Override
    public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
        if ((this.equals(NON_POS) && other.equals(NON_NEG)) || (this.equals(NON_NEG) && other.equals(NON_POS))) return ZERO;
        if ((this.equals(NON_POS) && other.equals(NON_ZERO)) || (this.equals(NON_ZERO) && other.equals(NON_POS))) return NEG;
        if ((this.equals(NON_NEG) && other.equals(NON_ZERO)) || (this.equals(NON_ZERO) && other.equals(NON_NEG))) return POS;
        if (this.lessOrEqualAux(other)) return this;
        if (other.lessOrEqualAux(this)) return other;
        return BOTTOM;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(ZERO)) return other.equals(NON_NEG) || other.equals(NON_POS);
        if (this.equals(NEG)) return other.equals(NON_POS) || other.equals(NON_ZERO);
        if (this.equals(POS)) return other.equals(NON_NEG) || other.equals(NON_ZERO);
        return false;
    }

    public Satisfiability eq(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        if (this.equals(ZERO) && other.equals(ZERO))
            return Satisfiability.SATISFIED;
        try {
            if (this.glb(other).isBottom())
                return Satisfiability.NOT_SATISFIED;
        } catch (SemanticException e) {
            return Satisfiability.UNKNOWN;
        }
        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;

        if (this.equals(POS) && (other.equals(ZERO) || other.equals(NEG) || other.equals(NON_POS))) return Satisfiability.SATISFIED;
        if ((this.equals(ZERO) || this.equals(NON_NEG)) && other.equals(NEG)) return Satisfiability.SATISFIED;

        if ((this.equals(NEG) || this.equals(ZERO)) && (other.equals(ZERO) || other.equals(POS) || other.equals(NON_NEG))) return Satisfiability.NOT_SATISFIED;
        if (this.equals(NON_POS) && other.equals(POS)) return Satisfiability.NOT_SATISFIED;

        return Satisfiability.UNKNOWN;
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
    public StructuredRepresentation representation() {
        if (this.isBottom()) return Lattice.bottomRepresentation();
        if (this.isTop()) return Lattice.topRepresentation();
        if (this.equals(NEG)) return new StringRepresentation("<0");
        if (this.equals(ZERO)) return new StringRepresentation("=0");
        if (this.equals(POS)) return new StringRepresentation(">0");
        if (this.equals(NON_NEG)) return new StringRepresentation(">=0");
        if (this.equals(NON_POS)) return new StringRepresentation("<=0");
        if (this.equals(NON_ZERO)) return new StringRepresentation("!=0");
        return new StringRepresentation("?");
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExtendedSignLattice esl = (ExtendedSignLattice) obj;
        return element == esl.element;
    }

    public int hashCode() {
        return Objects.hash(this.element);
    }
}
