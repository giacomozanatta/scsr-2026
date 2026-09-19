package it.unive.scsr.analysis.sign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

// This class represents the different possible states of a number's sign
public class SignLattice implements BaseLattice<SignLattice> {
    
    private final int element;
    
    // Define all the possible sign states
    public static final SignLattice TOP = new SignLattice(0);      // Unknown (can be anything)
    public static final SignLattice GEQ_ZERO = new SignLattice(1); // Greater than or equal to 0
    public static final SignLattice NOT_ZERO = new SignLattice(2); // Not equal to 0
    public static final SignLattice LEQ_ZERO = new SignLattice(3); // Less than or equal to 0
    public static final SignLattice POS = new SignLattice(4);      // Positive (> 0)
    public static final SignLattice ZERO = new SignLattice(5);     // Exactly 0
    public static final SignLattice NEG = new SignLattice(6);      // Negative (< 0)
    public static final SignLattice BOTTOM = new SignLattice(7);   // Empty (unreachable code)

    public SignLattice(int e) { this.element = e; }

    @Override public SignLattice top() { return TOP; }
    @Override public SignLattice bottom() { return BOTTOM; }

    // Merges two sign states into a wider state (Least Upper Bound)
    @Override
    public SignLattice lubAux(SignLattice other) throws SemanticException {
        if (this == other || other == BOTTOM) return this;
        if (this == BOTTOM) return other;
        if (this == TOP || other == TOP) return TOP;
        
        // Merging rules based on the lattice diagram
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
        
        return TOP; // If states conflict too much, return Unknown
    }

    // Checks if this sign state is more specific than or equal to the 'other' state
    @Override
    public boolean lessOrEqualAux(SignLattice other) throws SemanticException {
        if (this == other || other == TOP || this == BOTTOM) return true;
        if (other == GEQ_ZERO) return this == POS || this == ZERO;
        if (other == LEQ_ZERO) return this == NEG || this == ZERO;
        if (other == NOT_ZERO) return this == POS || this == NEG;
        return false;
    }

    // REQUIRED METHODS FOR COMPARISON:
    
    // Checks if this state is equal to the 'other' state
    public Satisfiability eq(SignLattice other) {
        if (this == BOTTOM || other == BOTTOM) return Satisfiability.BOTTOM;
        if (this == TOP || other == TOP) return Satisfiability.UNKNOWN;
        if (this == ZERO && other == ZERO) return Satisfiability.SATISFIED;
        if (this == POS && other == NEG) return Satisfiability.NOT_SATISFIED;
        if (this == NEG && other == POS) return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    // Checks if this state is strictly greater than the 'other' state
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

    // Generates the display text for the HTML results
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