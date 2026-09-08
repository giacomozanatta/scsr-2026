package it.unive.scsr.analysis.extendedSign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {


    private int element;

    private static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
    private static ExtendedSignLattice NON_ZERO = new ExtendedSignLattice(1);
    private static ExtendedSignLattice NON_POS = new ExtendedSignLattice(2);
    private static ExtendedSignLattice NON_NEG = new ExtendedSignLattice(3);
    private static ExtendedSignLattice NEG = new ExtendedSignLattice(4);
    private static ExtendedSignLattice ZERO = new ExtendedSignLattice(5);
    private static ExtendedSignLattice POS = new ExtendedSignLattice(6);
    private static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);


    public ExtendedSignLattice(int element) {
        this.element = element;
    }


    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(other)) return this;
        if (this.equals(ZERO) && other.equals(POS) || this.equals(POS) && other.equals(ZERO)) return NON_NEG;
        if (this.equals(ZERO) && other.equals(NEG) || this.equals(NEG) && other.equals(ZERO)) return NON_POS;
        if (this.equals(POS) && other.equals(NEG) || this.equals(NEG) && other.equals(POS)) return NON_ZERO;
        if (this.lessOrEqualAux(other)) return other;
        if (other.lessOrEqualAux(this)) return this;
        return TOP;
    }

    @Override
    public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(other)) return this;
        if (this.lessOrEqualAux(other)) return this;
        if (other.lessOrEqualAux(this)) return other;
        if (this.equals(NON_POS) && other.equals(NON_NEG) || this.equals(NON_NEG) && other.equals(NON_POS)) return ZERO;
        if (this.equals(NON_POS) && other.equals(NON_ZERO) || this.equals(NON_ZERO) && other.equals(NON_POS)) return NEG;
        if (this.equals(NON_NEG) && other.equals(NON_ZERO) || this.equals(NON_ZERO) && other.equals(NON_NEG)) return POS;
        return BOTTOM;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(other)) return true;
        if (this.equals(ZERO)) return other.equals(NON_NEG) || other.equals(NON_POS);
        if (this.equals(NEG)) return other.equals(NON_POS) || other.equals(NON_ZERO);
        if (this.equals(POS)) return other.equals(NON_NEG) || other.equals(NON_ZERO);
        return false;
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
        if (this == BOTTOM) return Lattice.bottomRepresentation();
        if (this == TOP) return Lattice.topRepresentation();
        if (this == NEG) return new StringRepresentation("<=0");
        if (this == ZERO) return new StringRepresentation("=0");
        if (this == POS) return new StringRepresentation(">=0");
        if (this == NON_NEG) return new StringRepresentation(">=0");
        if (this == NON_POS) return new StringRepresentation("<=0");
        if (this == NON_ZERO) return new StringRepresentation("!=0");
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
