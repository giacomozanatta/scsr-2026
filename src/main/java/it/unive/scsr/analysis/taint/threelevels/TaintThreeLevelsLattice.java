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
		// TODO: homework
		return null;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO: homework
		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		// TODO: homework
		return null;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		// TODO: homework
		return null;
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO: homework
		return null;
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		// TODO: homework
		return null;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		// TODO: homework
		return null;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		// TODO: homework
		return null;
	}

	@Override
	public boolean isAlwaysTainted() {
		// TODO: homework
		return false;
	}

	@Override
	public boolean isPossiblyTainted() {
		// TODO: homework
		return false;
	}

}
