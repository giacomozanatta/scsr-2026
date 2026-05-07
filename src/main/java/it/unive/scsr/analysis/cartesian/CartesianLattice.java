package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class CartesianLattice implements BaseLattice<CartesianLattice>, Comparable<CartesianLattice> {
    @Override
    public CartesianLattice top() {
        return null;
    }

    @Override
    public CartesianLattice bottom() {
        return null;
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
