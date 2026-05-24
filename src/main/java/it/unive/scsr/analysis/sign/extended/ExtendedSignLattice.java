package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;
import java.util.Set;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

	private final int element;

	public static ExtendedSignLattice TOP = new ExtendedSignLattice(0);
	public static ExtendedSignLattice POSZERO = new ExtendedSignLattice(1);
	public static ExtendedSignLattice POS = new ExtendedSignLattice(2);
	public static ExtendedSignLattice NEGZERO = new ExtendedSignLattice(3);
	public static ExtendedSignLattice NEG = new ExtendedSignLattice(4);
	public static ExtendedSignLattice ZERO = new ExtendedSignLattice(5);
	public static ExtendedSignLattice NONZERO = new ExtendedSignLattice(6);
	public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);

	public ExtendedSignLattice(int e) {
		element = e;
	}

	@Override
	public ExtendedSignLattice top() {
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return ExtendedSignLattice.BOTTOM;
	}


	@Override
	public int hashCode() {
		return Objects.hash(element);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedSignLattice other = (ExtendedSignLattice) obj;
		return element == other.element;
	}

	@Override
	public StructuredRepresentation representation() {

		if (this == ExtendedSignLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if (this == ExtendedSignLattice.ZERO)
			return new StringRepresentation("= 0");
		else if (this == ExtendedSignLattice.NONZERO)
			return new StringRepresentation("≠ 0");
		else if (this == ExtendedSignLattice.POS)
			return new StringRepresentation("> 0");
		else if (this == ExtendedSignLattice.POSZERO)
			return new StringRepresentation("≥ 0");
		else if (this == ExtendedSignLattice.NEG)
			return new StringRepresentation("< 0");
		else if (this == ExtendedSignLattice.NEGZERO)
			return new StringRepresentation("≤ 0");

		return Lattice.topRepresentation();
	}

	@Override
	public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
		ExtendedSignLattice a, b;
		a = this;
		b = other;
		int secondTake = 1;
		do {
			if (a == POSZERO) {
				if (Set.of(ZERO, POS).contains(b))
					return POSZERO;
				else
					return TOP;
			}
			if (a == NONZERO) {
				if (Set.of(POS, NEG).contains(b))
					return NONZERO;
				else
					return TOP;
			}
			if (a == NEGZERO) {
				if (Set.of(ZERO, NEG).contains(b))
					return NEGZERO;
				else
					return TOP;
			}
			if (a == POS) {
				if (Set.of(ZERO, POSZERO).contains(b))
					return POSZERO;
				else if (Set.of(NEG, NONZERO).contains(b))
					return NONZERO;
				else
					return TOP;
			}
			if (a == ZERO) {
				if (Set.of(POS, POSZERO).contains(b))
					return POSZERO;
				else if (Set.of(NEG, NEGZERO).contains(b))
					return NEGZERO;
				else
					return TOP;
			}
			if (a == NEG) {
				if (Set.of(ZERO, NEGZERO).contains(b))
					return NEGZERO;
				else if (Set.of(POS, NONZERO).contains(b))
					return NONZERO;
				else
					return TOP;
			}
			a = other;
			b = this;
		} while (secondTake-- > 0);
		return TOP;
	}

	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
		ExtendedSignLattice a, b;
		a = this;
		b = other;
		int secondTake = 1;
		do {
			if (a == POS) {
				if (Set.of(NONZERO, POSZERO).contains(b))
					return POS;
				else
					return BOTTOM;
			}
			if (a == ZERO) {
				if (Set.of(POSZERO, NEGZERO).contains(b))
					return ZERO;
				else
					return BOTTOM;
			}
			if (a == NEG) {
				if (Set.of(NONZERO, NEGZERO).contains(b))
					return NEG;
				else
					return BOTTOM;
			}
			if (a == POSZERO) {
				if (Set.of(NONZERO, POS).contains(b))
					return POS;
				else if (Set.of(NEGZERO, ZERO).contains(b))
					return ZERO;
				else
					return BOTTOM;
			}
			if (a == NONZERO) {
				if (Set.of(POS, POSZERO).contains(b))
					return POS;
				else if (Set.of(NEG, NEGZERO).contains(b))
					return NEG;
				else
					return BOTTOM;
			}
			if (a == NEGZERO) {
				if (Set.of(NONZERO, NEG).contains(b))
					return NEG;
				else if (Set.of(POSZERO, ZERO).contains(b))
					return ZERO;
				else
					return BOTTOM;
			}
			a = other;
			b = this;
		} while (secondTake-- > 0);
		return BOTTOM;
	}

	@Override
	public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
		if (this == POSZERO) {
			return false;
		}
		if (this == NONZERO) {
			return false;
		}
		if (this == NEGZERO) {
			return false;
		}
		if (this == POS) {
			return Set.of(NONZERO, POSZERO).contains(other);
		}
		if (this == ZERO) {
			return Set.of(POSZERO, NEGZERO).contains(other);
		}
		if (this == NEG) {
			return Set.of(NONZERO, NEGZERO).contains(other);
		}
		return false;
	}

	public Satisfiability eq(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		if (this.isTop() || other.isTop())
			return Satisfiability.UNKNOWN;

		if (this == ZERO && other == ZERO)
			return Satisfiability.SATISFIED;

		if (
				this == POS && Set.of(ZERO, NEG, NEGZERO).contains(other)
						|| this == ZERO && Set.of(POS, POSZERO, NEG, NEGZERO).contains(other)
						|| this == NEG && Set.of(POS, POSZERO, ZERO).contains(other)
						|| this == POSZERO && other == NEG
						|| this == NONZERO && other == ZERO
						|| this == NEGZERO && other == POS
		)
			return Satisfiability.NOT_SATISFIED;

		return Satisfiability.UNKNOWN;
	}

	public Satisfiability gt(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		if (this.isTop() || other.isTop())
			return Satisfiability.UNKNOWN;

		if (
				this == POS && Set.of(ZERO, NEGZERO, NEG).contains(other)
						|| this == POSZERO && other == NEG
						|| this == ZERO && other == NEG
		)
			return Satisfiability.SATISFIED;

		if (
				this == ZERO && Set.of(POS, POSZERO, ZERO).contains(other)
						|| this == NEGZERO && Set.of(POS, POSZERO, ZERO).contains(other)
						|| this == NEG && Set.of(POS, POSZERO, ZERO).contains(other)
		)
			return Satisfiability.NOT_SATISFIED;

		return Satisfiability.UNKNOWN;
	}


}
