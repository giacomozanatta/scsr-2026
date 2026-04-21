package it.unive.scsr.analysis.taint.threetaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Lattice of Three taint
 *	 Top 
 * 	/	\
 * C	 T	 
 *  \	/
 *  BOTTOM
 * 
 */
public class ThreeTaintLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<ThreeTaintLattice> {

	@Override
	public ThreeTaintLattice lubAux(ThreeTaintLattice other) throws SemanticException {
		// TODO
		return null;
	}

	@Override
	public boolean lessOrEqualAux(ThreeTaintLattice other) throws SemanticException {
		// TODO
		return false;
	}

	@Override
	public ThreeTaintLattice top() {
		// TODO
		return null;
	}

	@Override
	public ThreeTaintLattice bottom() {
		// TODO
		return null;
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO
		return null;
	}

	@Override
	public ThreeTaintLattice tainted() {
		// TODO
		return null;
	}

	@Override
	public ThreeTaintLattice clean() {
		// TODO
		return null;
	}

	@Override
	public ThreeTaintLattice or(ThreeTaintLattice other) throws SemanticException {
		// TODO
		return null;
	}

	@Override
	public boolean isAlwaysTainted() {
		// TODO
		return false;
	}

	@Override
	public boolean isPossiblyTainted() {
		// TODO
		return false;
	}

}
