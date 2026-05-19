package it.unive.scsr.analysis.sign.extendedSign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    private int elem;

    public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
    public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(4);
    public static ExtendedSignLattice ZERO = new ExtendedSignLattice(3); // Z = 0
    public static ExtendedSignLattice POS = new ExtendedSignLattice(1); // Z > 0
    public static ExtendedSignLattice NEG = new ExtendedSignLattice(2); // Z < 0
    public static ExtendedSignLattice NEQZERO = new ExtendedSignLattice(5); // Z != 0
    public static ExtendedSignLattice GEQZERO = new ExtendedSignLattice(6); // Z >= 0
    public static ExtendedSignLattice LEQZERO = new ExtendedSignLattice(7); // Z <= 0

    public ExtendedSignLattice(int elem) { this.elem = elem; }

    @Override
    public ExtendedSignLattice top() { return TOP; }

    @Override
    public ExtendedSignLattice bottom() { return BOTTOM; }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        // FIX: Evitiamo il Widening Bypass! Se uno include l'altro, vincono in automatico.
        if (this.lessOrEqualAux(other)) return other;
        if (other.lessOrEqualAux(this)) return this;

        if ((this.equals(ZERO) && other.equals(NEG)) || (this.equals(NEG) && other.equals(ZERO))) return LEQZERO;
        if ((this.equals(ZERO) && other.equals(POS)) || (this.equals(POS) && other.equals(ZERO))) return GEQZERO;
        if ((this.equals(NEG) && other.equals(POS)) || (this.equals(POS) && other.equals(NEG))) return NEQZERO;
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this.equals(other)) return true; // Riflessività
        if (this.equals(ZERO)) return other.equals(GEQZERO) || other.equals(LEQZERO);
        if (this.equals(NEG)) return other.equals(NEQZERO) || other.equals(LEQZERO);
        if (this.equals(POS)) return other.equals(GEQZERO) || other.equals(NEQZERO);
        return false;
    }

    @Override
    public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
        // FIX: Evitiamo il GLB Bypass!
        if (this.lessOrEqualAux(other)) return this;
        if (other.lessOrEqualAux(this)) return other;

        if ((this.equals(GEQZERO) && other.equals(LEQZERO)) || (this.equals(LEQZERO) && other.equals(GEQZERO))) return ZERO;
        if ((this.equals(GEQZERO) && other.equals(NEQZERO)) || (this.equals(NEQZERO) && other.equals(GEQZERO))) return POS;
        if ((this.equals(LEQZERO) && other.equals(NEQZERO)) || (this.equals(NEQZERO) && other.equals(LEQZERO))) return NEG;
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this.equals(TOP)) return Lattice.topRepresentation();
        if (this.equals(BOTTOM)) return Lattice.bottomRepresentation();
        if (this.equals(ZERO)) return new StringRepresentation("0");
        if (this.equals(POS)) return new StringRepresentation("+");
        if (this.equals(NEG)) return new StringRepresentation("-");
        if (this.equals(NEQZERO)) return new StringRepresentation("!=0");
        if (this.equals(GEQZERO)) return new StringRepresentation(">=0");
        return new StringRepresentation("<=0");
    }

    public Satisfiability eq(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;
        if (this.equals(ZERO) && other.equals(ZERO)) return Satisfiability.SATISFIED;
        if ((this.equals(POS) && other.equals(NEG)) || (other.equals(POS) && this.equals(NEG))) return Satisfiability.NOT_SATISFIED;
        if ((this.equals(ZERO) && (other.equals(POS) || other.equals(NEG) || other.equals(NEQZERO))) || (other.equals(ZERO) && (this.equals(POS) || this.equals(NEG) || this.equals(NEQZERO)))) return Satisfiability.NOT_SATISFIED;
        if ((this.equals(POS) && other.equals(LEQZERO)) || (other.equals(POS) && this.equals(LEQZERO))) return Satisfiability.NOT_SATISFIED;
        if ((this.equals(NEG) && other.equals(GEQZERO)) || (other.equals(NEG) && this.equals(GEQZERO))) return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;
        if (this.equals(ZERO) && other.equals(NEG)) return Satisfiability.SATISFIED;
        if (this.equals(POS) && (other.equals(NEG) || other.equals(ZERO) || other.equals(LEQZERO))) return Satisfiability.SATISFIED;
        if (this.equals(GEQZERO) && other.equals(NEG)) return Satisfiability.SATISFIED;
        if (this.equals(NEG) && (other.equals(POS) || other.equals(GEQZERO) || other.equals(ZERO))) return Satisfiability.NOT_SATISFIED;
        if (this.equals(ZERO) && (other.equals(POS) || other.equals(GEQZERO) || other.equals(ZERO))) return Satisfiability.NOT_SATISFIED;
        if (this.equals(LEQZERO) && (other.equals(POS) || other.equals(GEQZERO))) return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedSignLattice that = (ExtendedSignLattice) o;
        return elem == that.elem;
    }

    @Override
    public int hashCode() { return Objects.hashCode(elem); }
}
