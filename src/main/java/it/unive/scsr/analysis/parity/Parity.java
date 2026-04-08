package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	    public ParityLattice top() {
			return ParityLattice.TOP;
	    }

	    @Override
	    public ParityLattice bottom() {
			return ParityLattice.BOTTOM;
	    }

	    //TODO add missing components
}