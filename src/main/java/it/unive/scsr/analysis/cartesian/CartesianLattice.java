package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;


public record CartesianLattice(SignLattice signLattice,
                               TaintThreeLevelsLattice taintThreeLevelsLattice) implements BaseLattice<CartesianLattice> {
    public static final CartesianLattice TOP = new CartesianLattice(SignLattice.TOP, TaintThreeLevelsLattice.Top);
    public static final CartesianLattice BOTTOM = new CartesianLattice(SignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);

    @Override
    public CartesianLattice top() {
        return CartesianLattice.TOP;
    }

    @Override
    public CartesianLattice bottom() {
        return CartesianLattice.BOTTOM;
    }

    @Override
    public CartesianLattice lubAux(CartesianLattice other) throws SemanticException {
        return new CartesianLattice(signLattice.lubAux(other.signLattice), taintThreeLevelsLattice.lubAux(other.taintThreeLevelsLattice));
    }

    @Override
    public boolean lessOrEqualAux(CartesianLattice other) throws SemanticException {
        return this.taintThreeLevelsLattice.lessOrEqualAux(other.taintThreeLevelsLattice) && this.signLattice.lessOrEqualAux(other.signLattice);
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(new StringRepresentation(this.signLattice), new StringRepresentation(this.taintThreeLevelsLattice));
    }


    public Satisfiability eq(
            CartesianLattice other) throws SemanticException {
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
            CartesianLattice other) throws SemanticException {
        // Using unknown since TaintThreeLevelsLattice does not implement gt
        return this.signLattice.gt(other.signLattice).and(Satisfiability.UNKNOWN);
    }
}
