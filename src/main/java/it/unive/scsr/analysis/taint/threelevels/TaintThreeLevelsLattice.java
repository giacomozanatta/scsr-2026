package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

/*
 * Lattice of taint with three levels
 *	 Top 
 * 	/	\
 * C	 T	 
 *  \	/
 *  BOTTOM
 * 
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

	public static final TaintThreeLevelsLattice TOP    = new TaintThreeLevelsLattice(0);
	public static final TaintThreeLevelsLattice TAINT  = new TaintThreeLevelsLattice(1);
	public static final TaintThreeLevelsLattice CLEAN  = new TaintThreeLevelsLattice(2);
	public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(3);

	private final int element;

	public TaintThreeLevelsLattice(int element) {
		this.element = element;
	}

	public TaintThreeLevelsLattice() {
		this(3); // default: BOTTOM
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return BOTTOM;
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TAINT;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// TAINT lub CLEAN = TOP
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		// viene chiamato per comparare TAINT con CLEAN, che però non sono comparabili
		// (quindi this e other non sono né TOP, né BOTTOM)
		// di conseguenza va restituito false
		return false;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		// viene usato nel momento in cui si deve combinare due elementi (es a + b), per capire cos'è il risultato;
		// è diverso da lub; lub con (TAIN, CLEAN), darebbe TOP; mentre qui diamo TAINT, com'è giusto che sia

		if (this == BOTTOM || other == BOTTOM) {
			return BOTTOM;
		}
		// se uno dei due elementi è TAIN, allora anche il risultato lo è; si propaga
		if (this == TAINT || other == TAINT) {
			return TAINT;
		}
		if (this == TOP || other == TOP) {
			return TOP;
		}
		return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINT;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TAINT || this == TOP;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == BOTTOM) {
			return Lattice.bottomRepresentation();
		}
		if (this == TOP) {
			return Lattice.topRepresentation();
		}
		if (this == TAINT) {
			return new StringRepresentation("T");
		}
		return new StringRepresentation("C");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TaintThreeLevelsLattice other)) {
			return false;
		}
		return element == other.element;
	}

	@Override
	public int hashCode() {
		return Objects.hash(element);
	}

}
