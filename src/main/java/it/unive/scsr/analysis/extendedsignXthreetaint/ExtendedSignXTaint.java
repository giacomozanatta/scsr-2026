package it.unive.scsr.analysis.extendedsignXthreetaint;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;

public class ExtendedSignXTaint implements BaseNonRelationalValueDomain<ExtendedSignXTaintLattice> {
    @Override
    public ExtendedSignXTaintLattice top() {
        return ExtendedSignXTaintLattice.TOP;
    }

    @Override
    public ExtendedSignXTaintLattice bottom() {
        return ExtendedSignXTaintLattice.BOTTOM;
    }
}
