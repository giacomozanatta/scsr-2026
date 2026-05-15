package it.unive.scsr.analysis.cartesian.extended;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;


public record ExtendedCartesianLattice(ExtendedSignLattice signLattice,
                                       TaintThreeLevelsLattice taintThreeLevelsLattice) implements BaseLattice<ExtendedCartesianLattice> {
    public static final ExtendedCartesianLattice TOP = new ExtendedCartesianLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.Top);
    public static final ExtendedCartesianLattice BOTTOM = new ExtendedCartesianLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);

    @Override
    public ExtendedCartesianLattice top() {
        return ExtendedCartesianLattice.TOP;
    }

    @Override
    public ExtendedCartesianLattice bottom() {
        return ExtendedCartesianLattice.BOTTOM;
    }

    @Override
    public ExtendedCartesianLattice lubAux(ExtendedCartesianLattice other) throws SemanticException {
        return new ExtendedCartesianLattice(signLattice.lubAux(other.signLattice), taintThreeLevelsLattice.lubAux(other.taintThreeLevelsLattice));
    }

    @Override
    public boolean lessOrEqualAux(ExtendedCartesianLattice other) throws SemanticException {
        return this.taintThreeLevelsLattice.lessOrEqualAux(other.taintThreeLevelsLattice) && this.signLattice.lessOrEqualAux(other.signLattice);
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(new StringRepresentation(this.signLattice), new StringRepresentation(this.taintThreeLevelsLattice));
    }


    public Satisfiability eq(
            ExtendedCartesianLattice other) throws SemanticException {
        // Using unknown since TaintThreeLevelsLattice does not implement eq
        return this.signLattice.eq(other.signLattice).and(Satisfiability.UNKNOWN);
    }

    /**
     * Tests if this instance is greater than the given one, returning a
     * {@link Satisfiability} element.
     *
     * @param other the instance
     * @return the satisfiability of {@code this > other}
     */
    public Satisfiability gt(
            ExtendedCartesianLattice other) throws SemanticException {
        // Using unknown since TaintThreeLevelsLattice does not implement gt
        return this.signLattice.gt(other.signLattice).and(Satisfiability.UNKNOWN);
    }
}
