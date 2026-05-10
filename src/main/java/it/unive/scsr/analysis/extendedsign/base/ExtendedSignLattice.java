package it.unive.scsr.analysis.extendedsign.base;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

	public static final ExtendedSignLattice TOP      = new ExtendedSignLattice("TOP");
	public static final ExtendedSignLattice LTE_ZERO = new ExtendedSignLattice("LTE_ZERO");  // <= 0
	public static final ExtendedSignLattice NON_ZERO = new ExtendedSignLattice("NON_ZERO"); // != 0
	public static final ExtendedSignLattice GTE_ZERO = new ExtendedSignLattice("GTE_ZERO");  // >= 0
	public static final ExtendedSignLattice NEG      = new ExtendedSignLattice("NEG");      // < 0
	public static final ExtendedSignLattice ZERO     = new ExtendedSignLattice("ZERO");     // = 0
	public static final ExtendedSignLattice POS      = new ExtendedSignLattice("POS");      // > 0
	public static final ExtendedSignLattice BOTTOM   = new ExtendedSignLattice("BOTTOM");

	private final String name;

	public ExtendedSignLattice(String name) {
		this.name = name;
	}

	public ExtendedSignLattice() {
		this("BOTTOM");
	}

	@Override
	public ExtendedSignLattice top() {
		return TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return BOTTOM;
	}

	@Override
	public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
		// (NEG | POS | ZERO) union (LTE_ZERO | GTE_ZERO | NON_ZERO) --> (LTE_ZERO | GTE_ZERO | NON_ZERO)
		if ((this == NEG && other == LTE_ZERO) || (other == NEG && this == LTE_ZERO)) {
			return LTE_ZERO;
		}
		if ((this == NEG && other == NON_ZERO) || (other == NEG && this == NON_ZERO)) {
			return NON_ZERO;
		}
		if ((this == ZERO && other == LTE_ZERO) || (other == ZERO && this == LTE_ZERO)) {
			return LTE_ZERO;
		}
		if ((this == ZERO && other == GTE_ZERO) || (other == ZERO && this == GTE_ZERO)) {
			return GTE_ZERO;
		}
		if ((this == POS && other == NON_ZERO) || (other == POS && this == NON_ZERO)) {
			return NON_ZERO;
		}
		if ((this == POS && other == GTE_ZERO) || (other == POS && this == GTE_ZERO)) {
			return GTE_ZERO;
		}

		// (ZERO | NEG | POS) union (ZERO | NEG | POS) --> (LTE_ZERO | NON_ZERO | GTE_ZERO)
		if ((this == NEG && other == ZERO) || (other == NEG && this == ZERO)) {
			return LTE_ZERO;
		}
		if ((this == NEG && other == POS) || (other == NEG && this == POS)) {
			return NON_ZERO;
		}
		if ((this == ZERO && other == POS) || (other == ZERO && this == POS)) {
			return GTE_ZERO;
		}

		// i casi rimanenti riguardano coppie come LTE_ZERO - GTE_ZERO, che danno TOP come risultato dato che comprendono tutte le casistiche
		return TOP;
	}

	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
		// (NEG | POS | ZERO) intersected (LTE_ZERO | GTE_ZERO | NON_ZERO) --> (NEG | POS | ZERO)
		if ((this == NEG && other == LTE_ZERO) || (other == NEG && this == LTE_ZERO)) {
			return NEG;
		}
		if ((this == NEG && other == NON_ZERO) || (other == NEG && this == NON_ZERO)) {
			return NEG;
		}
		if ((this == ZERO && other == LTE_ZERO) || (other == ZERO && this == LTE_ZERO)) {
			return ZERO;
		}
		if ((this == ZERO && other == GTE_ZERO) || (other == ZERO && this == GTE_ZERO)) {
			return ZERO;
		}
		if ((this == POS && other == NON_ZERO) || (other == POS && this == NON_ZERO)) {
			return POS;
		}
		if ((this == POS && other == GTE_ZERO) || (other == POS && this == GTE_ZERO)) {
			return POS;
		}

		// (LTE_ZERO | GTE_ZERO | NON_ZERO) intersected (LTE_ZERO | GTE_ZERO | NON_ZERO) --> (NEG | POS | ZERO)
		if ((this == LTE_ZERO && other == NON_ZERO) || (other == LTE_ZERO && this == NON_ZERO)) {
			return NEG;
		}
		if ((this == LTE_ZERO && other == GTE_ZERO) || (other == LTE_ZERO && this == GTE_ZERO)) {
			return ZERO;
		}
		if ((this == NON_ZERO && other == GTE_ZERO) || (other == NON_ZERO && this == GTE_ZERO)) {
			return POS;
		}

		// i casi rimanenti riguardano coppie come NEG - GTE_ZERO, che danno BOTTOM come risultato dato che non hanno niente in comune
		return BOTTOM;
	}

	@Override
	public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
		if (this == NEG && (other == LTE_ZERO || other == NON_ZERO)) {
			return true;
		}
		if (this == ZERO && (other == LTE_ZERO || other == GTE_ZERO)) {
			return true;
		}
		if (this == POS && (other == NON_ZERO || other == GTE_ZERO)) {
			return true;
		}
		return false;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == BOTTOM) {
			return Lattice.bottomRepresentation();
		}
		if (this == TOP) {
			return Lattice.topRepresentation();
		}
		if (this == NEG) {
			return new StringRepresentation("-");
		}
		if (this == ZERO) {
			return new StringRepresentation("0");
		}
		if (this == POS) {
			return new StringRepresentation("+");
		}
		if (this == LTE_ZERO) {
			return new StringRepresentation("<=0");
		}
		if (this == NON_ZERO) {
			return new StringRepresentation("!=0");
		}
		return new StringRepresentation(">=0");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ExtendedSignLattice other)) {
			return false;
		}
		return Objects.equals(this.name, other.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
