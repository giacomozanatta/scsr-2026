package it.unive.scsr.analysis.sign.extendedsign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {
    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice extendedSignLattice) throws SemanticException {
        return null;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice extendedSignLattice) throws SemanticException {
        return false;
    }

    @Override
    public ExtendedSignLattice top() {
        return null;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return null;
    }

    @Override
    public StructuredRepresentation representation() {
        return null;
    }
}
