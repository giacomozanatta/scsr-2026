package it.unive.scsr.analysis.taint;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Lattice of Taint Domain 
 * 
 * T
 * |
 * C
 * |
 * BOTTOM
 * 
 */
public class TaintLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintLattice> {

	Boolean element;

	static public TaintLattice Taint = new TaintLattice(true);
	static public TaintLattice Clean = new TaintLattice(false);
	static public TaintLattice Bottom = new TaintLattice(null);

	public TaintLattice(Boolean e) {
		this.element = e;
	}

	@Override
	public TaintLattice top() {
		return Taint;
	}

	@Override
	public TaintLattice bottom() {
		return Bottom;
	}

	@Override
	public StructuredRepresentation representation() {
		if(this == Bottom)
			return Lattice.bottomRepresentation();
		
		return this == Taint ? new StringRepresentation("T") : new StringRepresentation("C");
	}

	@Override
	public TaintLattice lubAux(TaintLattice other) throws SemanticException {
		if (this == TaintLattice.Bottom)
			return other;
		if (other == TaintLattice.Bottom)
			return this;

		if (this == TaintLattice.Taint || other == TaintLattice.Taint)
			return TaintLattice.Taint;

		return TaintLattice.Clean;
	}

	@Override
	public boolean lessOrEqualAux(TaintLattice other) throws SemanticException {
		if (this == other)
			return true;

		if (this == TaintLattice.Bottom)
			return true;

		if (this == TaintLattice.Clean && other == TaintLattice.Taint)
			return true;

		return false;
	}

	@Override
	public TaintLattice tainted() {
		return TaintLattice.Taint;
	}

	@Override
	public TaintLattice clean() {
		return TaintLattice.Clean;
	}

	@Override
	public TaintLattice or(TaintLattice other) throws SemanticException {
		
		if(this == TaintLattice.Bottom || other == TaintLattice.Bottom)
			return TaintLattice.Bottom;
		
		if(this == TaintLattice.Taint || other == TaintLattice.Taint)
			return TaintLattice.Taint;
		
		return TaintLattice.Clean;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == Taint;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == Taint;
	}
	
}
