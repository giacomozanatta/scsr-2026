package it.unive.scsr.analysis.sign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public enum ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    TOP, GEQ_ZERO, LEQ_ZERO, NEQ_ZERO, POS, ZERO, NEG, BOTTOM;

    @Override public ExtendedSignLattice top()    { return TOP; }
    @Override public ExtendedSignLattice bottom() { return BOTTOM; }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        if ((this == NEG && other == ZERO) || (this == ZERO && other == NEG)) return LEQ_ZERO;
        if ((this == POS && other == ZERO) || (this == ZERO && other == POS)) return GEQ_ZERO;
        if ((this == NEG && other == POS)  || (this == POS  && other == NEG)) return NEQ_ZERO;
        return TOP;
    }

    @Override
    public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
        return fromAtoms(hasNeg() && other.hasNeg(), hasZero() && other.hasZero(), hasPos() && other.hasPos());
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        return switch (this) {
            case NEG  -> other == LEQ_ZERO || other == NEQ_ZERO;
            case ZERO -> other == LEQ_ZERO || other == GEQ_ZERO;
            case POS  -> other == GEQ_ZERO || other == NEQ_ZERO;
            default   -> false;
        };
    }

    @Override
    public StructuredRepresentation representation() {
        return switch (this) {
            case BOTTOM   -> Lattice.bottomRepresentation();
            case TOP      -> Lattice.topRepresentation();
            case POS      -> new StringRepresentation(">0");
            case NEG      -> new StringRepresentation("<0");
            case ZERO     -> new StringRepresentation("=0");
            case GEQ_ZERO -> new StringRepresentation(">=0");
            case LEQ_ZERO -> new StringRepresentation("<=0");
            default       -> new StringRepresentation("!=0");
        };
    }

    boolean hasNeg()  { return this == NEG || this == LEQ_ZERO || this == NEQ_ZERO || this == TOP; }
    boolean hasZero() { return this == ZERO || this == LEQ_ZERO || this == GEQ_ZERO || this == TOP; }
    boolean hasPos()  { return this == POS || this == GEQ_ZERO || this == NEQ_ZERO || this == TOP; }

    static ExtendedSignLattice fromAtoms(boolean neg, boolean zero, boolean pos) {
        if (!neg && !zero && !pos) return BOTTOM;
        if (neg && zero && pos)    return TOP;
        if (neg  && zero) return LEQ_ZERO;
        if (zero && pos)  return GEQ_ZERO;
        if (neg  && pos)  return NEQ_ZERO;
        if (neg)  return NEG;
        if (zero) return ZERO;
        return POS;
    }

    ExtendedSignLattice negate() {
        return switch (this) {
            case NEG      -> POS;
            case POS      -> NEG;
            case GEQ_ZERO -> LEQ_ZERO;
            case LEQ_ZERO -> GEQ_ZERO;
            default       -> this;
        };
    }

    public Satisfiability eq(ExtendedSignLattice other) {
        if (isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
        if (isTop()    || other.isTop())    return Satisfiability.UNKNOWN;
        if (this == ZERO && other == ZERO)  return Satisfiability.SATISFIED;
        boolean overlap = (hasNeg() && other.hasNeg())
                       || (hasZero() && other.hasZero())
                       || (hasPos()  && other.hasPos());
        return overlap ? Satisfiability.UNKNOWN : Satisfiability.NOT_SATISFIED;
    }

    public Satisfiability gt(ExtendedSignLattice other) {
        if (isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
        if (isTop()    || other.isTop())    return Satisfiability.UNKNOWN;

        if (this == POS && (other == NEG || other == ZERO || other == LEQ_ZERO)) return Satisfiability.SATISFIED;
        if (this == ZERO     && other == NEG) return Satisfiability.SATISFIED;
        if (this == GEQ_ZERO && other == NEG) return Satisfiability.SATISFIED;

        if (this == NEG     && (other == ZERO || other == POS || other == GEQ_ZERO)) return Satisfiability.NOT_SATISFIED;
        if (this == ZERO    && (other == ZERO || other == POS || other == GEQ_ZERO)) return Satisfiability.NOT_SATISFIED;
        if (this == LEQ_ZERO && (other == POS || other == GEQ_ZERO))                 return Satisfiability.NOT_SATISFIED;

        return Satisfiability.UNKNOWN;
    }
}
