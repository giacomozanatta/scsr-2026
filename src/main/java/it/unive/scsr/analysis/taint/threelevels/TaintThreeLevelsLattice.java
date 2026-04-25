package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Lattice of  taint with three levels
 *	 Top 
 * 	/	\
 * C	 T	 
 *  \	/
 *  BOTTOM
 * 
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO
		return null;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO
		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		// TODO
		return null;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		// TODO
		return null;
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO
		return null;
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		// TODO
		return null;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		// TODO
		return null;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
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
