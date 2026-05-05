package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
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

	private final int val;

	static public TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(100);
	static public TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(1);
	static public TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(0);
	static public TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(-100);

	public TaintThreeLevelsLattice(int v){
		this.val = v;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		return false;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return TaintThreeLevelsLattice.BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this.val == TaintThreeLevelsLattice.TOP.val){
			return Lattice.topRepresentation();
		}
		else if(this.val == TaintThreeLevelsLattice.BOTTOM.val){
			return Lattice.bottomRepresentation();
		}
		else if(this.val == TaintThreeLevelsLattice.TAINT.val){
			return new StringRepresentation("T");
		}
		return new StringRepresentation("C");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.TAINT;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this.val == TaintThreeLevelsLattice.BOTTOM.val || other.val == TaintThreeLevelsLattice.BOTTOM.val){
			return TaintThreeLevelsLattice.BOTTOM;
		}
		else if (this.val == TaintThreeLevelsLattice.TAINT.val || other.val == TaintThreeLevelsLattice.TAINT.val){
			return TaintThreeLevelsLattice.TAINT;
		}
		else if (this.val == TaintThreeLevelsLattice.TOP.val || other.val == TaintThreeLevelsLattice.TOP.val){
			return TaintThreeLevelsLattice.TOP;
		}
		return TaintThreeLevelsLattice.CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this.val == TaintThreeLevelsLattice.TAINT.val;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this.val == TaintThreeLevelsLattice.TOP.val;
	}

}
