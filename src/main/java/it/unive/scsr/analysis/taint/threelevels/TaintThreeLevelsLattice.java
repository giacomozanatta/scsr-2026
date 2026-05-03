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

        int element;
        
        static public TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(0);
        static public TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(1);
	static public TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(2);
	static public TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(3);

	public TaintThreeLevelsLattice(int e) {
		this.element = e;
	}
    
	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		return Top;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
                if(other == this)
                    return true;
		return other == Top;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return Top;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return Bottom;
	}

	@Override
	public StructuredRepresentation representation() {
		if(this == Bottom)
                    return Lattice.bottomRepresentation();
                if(this == Top)
                    return Lattice.topRepresentation();
		
		return this == Taint ? new StringRepresentation("T") : new StringRepresentation("C");
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return Taint;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return Clean;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
                if(this == Top || other == Top)
                    return TaintThreeLevelsLattice.Top;
            
		if(this == Bottom || other == Bottom)
                    return TaintThreeLevelsLattice.Bottom;
		
		if(this == Taint || other == Taint)
                    return TaintThreeLevelsLattice.Taint;
		
		return TaintThreeLevelsLattice.Clean;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == Taint;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == Taint || this == Top;
	}

}
