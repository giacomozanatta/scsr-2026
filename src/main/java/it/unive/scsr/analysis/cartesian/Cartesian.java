package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;

public class Cartesian
        implements BaseNonRelationalValueDomain<CartesianLattice> {
    @Override
    public CartesianLattice top() {
        return CartesianLattice.TOP;
    }

    @Override
    public CartesianLattice bottom() {
        return CartesianLattice.BOTTOM;
    }
}
