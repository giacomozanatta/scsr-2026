package it.unive.scsr.analysis.sign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

public class SignLattice implements BaseLattice<SignLattice> {
    
    private final int element;
    
    public static final SignLattice TOP = new SignLattice(0);      // Z
    public static final SignLattice GEQ_ZERO = new SignLattice(1); // Z >= 0
    public static final SignLattice NOT_ZERO = new SignLattice(2); // Z != 0
    public static final SignLattice LEQ_ZERO = new SignLattice(3); // Z <= 0
    public static final SignLattice POS = new SignLattice(4);      // Z > 0
    public static final SignLattice ZERO = new SignLattice(5);     // Z = 0
    public static final SignLattice NEG = new SignLattice(6);      // Z < 0
    public static final SignLattice BOTTOM = new SignLattice(7);   // Empty

    public SignLattice(int e) { this.element = e; }

    @Override public SignLattice top() { return TOP; }
    @Override public SignLattice bottom() { return BOTTOM; }

    @Override
    public SignLattice lubAux(SignLattice other) throws SemanticException {
        if (this == other || other == BOTTOM) return this;
        if (this == BOTTOM) return other;
        if (this == TOP || other == TOP) return TOP;
        
        // Объединение по решетке из PDF
        if (this == ZERO) {
            if (other == POS || other == GEQ_ZERO) return GEQ_ZERO;
            if (other == NEG || other == LEQ_ZERO) return LEQ_ZERO;
        }
        if (this == POS) {
            if (other == ZERO || other == GEQ_ZERO) return GEQ_ZERO;
            if (other == NEG || other == NOT_ZERO) return NOT_ZERO;
        }
        if (this == NEG) {
            if (other == ZERO || other == LEQ_ZERO) return LEQ_ZERO;
            if (other == POS || other == NOT_ZERO) return NOT_ZERO;
        }
        
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(SignLattice other) throws SemanticException {
        if (this == other || other == TOP || this == BOTTOM) return true;
        if (other == GEQ_ZERO) return this == POS || this == ZERO;
        if (other == LEQ_ZERO) return this == NEG || this == ZERO;
        if (other == NOT_ZERO) return this == POS || this == NEG;
        return false;
    }

    // ТЕ САМЫЕ МЕТОДЫ, КОТОРЫХ НЕ ХВАТАЛО:
    public Satisfiability eq(SignLattice other) {
        if (this == BOTTOM || other == BOTTOM) return Satisfiability.BOTTOM;
        if (this == TOP || other == TOP) return Satisfiability.UNKNOWN;
        if (this == ZERO && other == ZERO) return Satisfiability.SATISFIED;
        if (this == POS && other == NEG) return Satisfiability.NOT_SATISFIED;
        if (this == NEG && other == POS) return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(SignLattice other) {
        if (this == BOTTOM || other == BOTTOM) return Satisfiability.BOTTOM;
        if (this == TOP || other == TOP) return Satisfiability.UNKNOWN;
        if (this == POS && (other == ZERO || other == NEG)) return Satisfiability.SATISFIED;
        if (this == ZERO && other == NEG) return Satisfiability.SATISFIED;
        if (this == NEG && (other == ZERO || other == POS)) return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignLattice that = (SignLattice) o;
        return element == that.element;
    }

    @Override
    public int hashCode() { return Objects.hash(element); }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM) return Lattice.bottomRepresentation();
        if (this == TOP) return Lattice.topRepresentation();
        if (this == POS) return new StringRepresentation(">");
        if (this == NEG) return new StringRepresentation("<");
        if (this == ZERO) return new StringRepresentation("0");
        if (this == GEQ_ZERO) return new StringRepresentation(">=");
        if (this == LEQ_ZERO) return new StringRepresentation("<=");
        return new StringRepresentation("!=");
    }
}