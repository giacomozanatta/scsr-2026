package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;


public class CartesianLattice implements BaseLattice<CartesianLattice>, Comparable<CartesianLattice> {
    private final CartesianProduct pair;
    public static final CartesianLattice TOP = new CartesianLattice(SignLattice.TOP, TaintThreeLevelsLattice.Top);
    public static final CartesianLattice BOTTOM = new CartesianLattice(SignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);

    public CartesianLattice(SignLattice signLattice, TaintThreeLevelsLattice taintThreeLevelsLattice) {
        pair = new CartesianProduct(signLattice, taintThreeLevelsLattice);
    }

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
        return null;
    }

    @Override
    public boolean lessOrEqualAux(CartesianLattice other) throws SemanticException {
        return false;
    }

    @Override
    public StructuredRepresentation representation() {
        return null;
    }

    @Override
    public int compareTo(CartesianLattice cartesianLattice) {
        return 0;
    }
}
