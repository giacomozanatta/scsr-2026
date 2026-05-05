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
public class TaintThreeLevelsLattice
		implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

	public static final TaintThreeLevelsLattice BOTTOM  = new TaintThreeLevelsLattice(0);
	public static final TaintThreeLevelsLattice CLEAN   = new TaintThreeLevelsLattice(1);
	public static final TaintThreeLevelsLattice TAINTED  = new TaintThreeLevelsLattice(2);
	public static final TaintThreeLevelsLattice TOP      = new TaintThreeLevelsLattice(3);

	private final int level;

	private TaintThreeLevelsLattice(int level) {
		this.level = level;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other || other == BOTTOM)
			return this;

		if (this == BOTTOM)
			return other;

		if (this == TOP || other == TOP)
			return TOP;

		// Se arriviamo qui, stiamo unendo CLEAN e TAINTED
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == other || other == TOP || this == BOTTOM)
			return true;

		return false;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == BOTTOM) return other;
		if (other == BOTTOM) return this;

		// L'incertezza domina: se un operando è incerto, il risultato è incerto
		if (this == TOP || other == TOP)
			return TOP;

		// Se uno dei due è tinto (e l'altro non è TOP), il risultato è tinto
		if (this == TAINTED || other == TAINTED)
			return TAINTED;

		return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINTED;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TOP;
	}

	@Override public TaintThreeLevelsLattice top() { return TOP; }
	@Override public TaintThreeLevelsLattice bottom() { return BOTTOM; }
	@Override public TaintThreeLevelsLattice tainted() { return TAINTED; }
	@Override public TaintThreeLevelsLattice clean() { return CLEAN; }

	@Override
	public StructuredRepresentation representation() {
		if (this == TOP) return Lattice.topRepresentation();
		if (this == BOTTOM) return Lattice.bottomRepresentation();
		if (this == TAINTED) return new StringRepresentation("T");
		return new StringRepresentation("C");
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof TaintThreeLevelsLattice other) && this.level == other.level;
	}

	@Override
	public int hashCode() {
		return level;
	}
}