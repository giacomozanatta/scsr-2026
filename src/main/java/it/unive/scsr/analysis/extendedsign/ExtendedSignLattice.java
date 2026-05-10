package it.unive.scsr.analysis.extendedsign;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/**
 * Extended sign lattice with intermediate levels for more precise analysis.
 *
 * The lattice structure is:
 *
 *                  TOP
 *               /   |   \
 *             LE0  NEQ0  GE0
 *            /  \  / \  /  \
 *           NEG  ZERO   POS
 *            \    |    /
 *             \___|___/
 *                 |
 *              BOTTOM
 *
 * Where:
 *         TOP: All integers (unknown)
 *    LE0 (≤0): Negative integers including zero (-∞, 0]
 *   NEQ0 (≠0): All integers except zero (-∞, 0) ∪ (0, +∞)
 *    GE0 (≥0): Positive integers including zero [0, +∞)
 *    NEG (<0): Strictly negative integers (-∞, 0)
 *   ZERO (=0): Exactly zero [0]
 *    POS (>0): Strictly positive integers (0, +∞)
 *      BOTTOM: Empty set (⊥)
 */
public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

	private final int element;

	// Encoding of lattice elements
	public static final ExtendedSignLattice TOP = new ExtendedSignLattice(0);
	public static final ExtendedSignLattice LE0 = new ExtendedSignLattice(1);  // ≤ 0 (negative or zero)
	public static final ExtendedSignLattice NEQ0 = new ExtendedSignLattice(2); // ≠ 0 (non-zero)
	public static final ExtendedSignLattice GE0 = new ExtendedSignLattice(3);  // ≥ 0 (positive or zero)
	public static final ExtendedSignLattice NEG = new ExtendedSignLattice(4);  // < 0 (strictly negative)
	public static final ExtendedSignLattice ZERO = new ExtendedSignLattice(5); // = 0 (exactly zero)
	public static final ExtendedSignLattice POS = new ExtendedSignLattice(6);  // > 0 (strictly positive)
	public static final ExtendedSignLattice BOTTOM = new ExtendedSignLattice(7);

	private ExtendedSignLattice(int e) {
		this.element = e;
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
		if (this == BOTTOM)
			return Lattice.bottomRepresentation();
		else if (this == TOP)
			return Lattice.topRepresentation();
		else if (this == ZERO)
			return new StringRepresentation("0");
		else if (this == POS)
			return new StringRepresentation("+");
		else if (this == NEG)
			return new StringRepresentation("-");
		else if (this == LE0)
			return new StringRepresentation("≤0");
		else if (this == GE0)
			return new StringRepresentation("≥0");
		else if (this == NEQ0)
			return new StringRepresentation("≠0");
		return new StringRepresentation("?");
	}

	@Override
	public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
		// Identity
		if (this.equals(other))
			return this;

		// Bottom handling
		if (this == BOTTOM)
			return other;
		if (other == BOTTOM)
			return this;

		// Top handling
		if (this == TOP || other == TOP)
			return TOP;

		// LUB(NEG, ZERO) = LE0
		if ((this == NEG && other == ZERO) || (this == ZERO && other == NEG))
			return LE0;

		// LUB(ZERO, POS) = GE0
		if ((this == ZERO && other == POS) || (this == POS && other == ZERO))
			return GE0;

		// LUB(NEG, POS) = NEQ0
		if ((this == NEG && other == POS) || (this == POS && other == NEG))
			return NEQ0;

		// NEG ⊂ LE0, NEG ⊂ NEQ0
		if (this == NEG && (other == LE0 || other == NEQ0))
			return other;
		if (other == NEG && (this == LE0 || this == NEQ0))
			return this;

		// POS ⊂ GE0, POS ⊂ NEQ0
		if (this == POS && (other == GE0 || other == NEQ0))
			return other;
		if (other == POS && (this == GE0 || this == NEQ0))
			return this;

		// ZERO ⊂ LE0, ZERO ⊂ GE0
		if (this == ZERO && (other == LE0 || other == GE0))
			return other;
		if (other == ZERO && (this == LE0 || this == GE0))
			return this;

		// LE0 and GE0 are incomparable (meet at ZERO but neither contains the other)
		if ((this == LE0 && other == GE0) || (this == GE0 && other == LE0))
			return TOP;

		// LE0 and NEQ0: LE0 contains 0, NEQ0 doesn't; union is all integers
		if ((this == LE0 && other == NEQ0) || (this == NEQ0 && other == LE0))
			return TOP;

		// GE0 and NEQ0: GE0 contains 0, NEQ0 doesn't; union is all integers
		if ((this == GE0 && other == NEQ0) || (this == NEQ0 && other == GE0))
			return TOP;

		return TOP;
	}

	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
		// Identity
		if (this.equals(other))
			return this;

		// Bottom handling
		if (this == BOTTOM || other == BOTTOM)
			return BOTTOM;

		// Top handling
		if (this == TOP)
			return other;
		if (other == TOP)
			return this;

		// GLB(LE0, GE0) = ZERO (intersection of (-∞,0] and [0,+∞) is {0})
		if ((this == LE0 && other == GE0) || (this == GE0 && other == LE0))
			return ZERO;

		// GLB(LE0, NEQ0) = NEG (intersection of (-∞,0] and (-∞,0)∪(0,+∞) is (-∞,0))
		if ((this == LE0 && other == NEQ0) || (this == NEQ0 && other == LE0))
			return NEG;

		// GLB(GE0, NEQ0) = POS (intersection of [0,+∞) and (-∞,0)∪(0,+∞) is (0,+∞))
		if ((this == GE0 && other == NEQ0) || (this == NEQ0 && other == GE0))
			return POS;

		// LE0 (≤0) and POS (>0) are disjoint (0 is in LE0 but not in POS)
		if ((this == LE0 && other == POS) || (this == POS && other == LE0))
			return BOTTOM;

		// GE0 (≥0) and NEG (<0) are disjoint
		if ((this == GE0 && other == NEG) || (this == NEG && other == GE0))
			return BOTTOM;

		// NEQ0 (≠0) and ZERO (=0) are disjoint
		if ((this == NEQ0 && other == ZERO) || (this == ZERO && other == NEQ0))
			return BOTTOM;

		// GLB(NEG, LE0) = NEG (NEG ⊂ LE0)
		if ((this == NEG && other == LE0) || (this == LE0 && other == NEG))
			return NEG;

		// GLB(POS, GE0) = POS (POS ⊂ GE0)
		if ((this == POS && other == GE0) || (this == GE0 && other == POS))
			return POS;

		// GLB(ZERO, LE0) = ZERO (ZERO ⊂ LE0)
		if ((this == ZERO && other == LE0) || (this == LE0 && other == ZERO))
			return ZERO;

		// GLB(ZERO, GE0) = ZERO (ZERO ⊂ GE0)
		if ((this == ZERO && other == GE0) || (this == GE0 && other == ZERO))
			return ZERO;

		// GLB(NEG, NEQ0) = NEG (NEG ⊂ NEQ0)
		if ((this == NEG && other == NEQ0) || (this == NEQ0 && other == NEG))
			return NEG;

		// GLB(POS, NEQ0) = POS (POS ⊂ NEQ0)
		if ((this == POS && other == NEQ0) || (this == NEQ0 && other == POS))
			return POS;

		return BOTTOM;
	}

	@Override
	public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
		// this ⊑ other means this is more "precise" (i.e. lower in the lattice) than other
		// Identity: x ⊑ x
		if (this.equals(other))
			return true;

		// Bottom is less than everything: ⊥ ⊑ x
		if (this == BOTTOM)
			return true;

		// Everything is less than top: x ⊑ ⊤
		if (other == TOP)
			return true;

		// Concrete values are less than their containing intervals
		// NEG ⊂ LE0, NEG ⊂ NEQ0
		if (this == NEG && (other == LE0 || other == NEQ0))
			return true;

		// ZERO ⊂ LE0, ZERO ⊂ GE0
		if (this == ZERO && (other == LE0 || other == GE0))
			return true;

		// POS ⊂ GE0, POS ⊂ NEQ0
		if (this == POS && (other == GE0 || other == NEQ0))
			return true;

		// No other subset relationships hold
		return false;
	}

	public Satisfiability eq(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;
		else if (this.isTop() || other.isTop())
			return Satisfiability.UNKNOWN;
		else if (!this.equals(other))
			return Satisfiability.NOT_SATISFIED;
		else if (this == ZERO)
			return Satisfiability.SATISFIED;
		else
			return Satisfiability.UNKNOWN;
	}

	public Satisfiability gt(ExtendedSignLattice other) {
		if (this == BOTTOM || other == BOTTOM) {
			return Satisfiability.BOTTOM;
		} else if (this == TOP || other == TOP) {
			return Satisfiability.UNKNOWN;
		} else if (this == NEG) {
			if (other == NEG)
				return Satisfiability.UNKNOWN;
			if (other == ZERO || other == POS || other == GE0)
				return Satisfiability.NOT_SATISFIED; // negatives are never greater than 0 or positives
			if (other == LE0)
				return Satisfiability.UNKNOWN; // NEG ⊂ LE0, so could be greater for some elements in LE0
			if (other == NEQ0)
				return Satisfiability.UNKNOWN; // NEG ⊂ NEQ0
		} else if (this == ZERO) {
			if (other == NEG || other == LE0)
				return Satisfiability.SATISFIED;
			if (other == ZERO || other == POS || other == GE0 || other == NEQ0)
				return Satisfiability.NOT_SATISFIED; // 0 is not greater than 0 or positives
		} else if (this == POS) {
			if (other == NEG || other == ZERO || other == LE0)
				return Satisfiability.SATISFIED; // positives greater than 0 and all negatives
			if (other == POS)
				return Satisfiability.UNKNOWN; // some positives might be greater than others or not
			if (other == GE0)
				return Satisfiability.UNKNOWN; // POS ⊂ GE0
			if (other == NEQ0)
				return Satisfiability.UNKNOWN; // POS ⊂ NEQ0
		} else if (this == LE0) {
			if (other == NEG)
				return Satisfiability.UNKNOWN; // LE0 contains NEG and ZERO
			if (other == ZERO || other == POS || other == GE0 || other == NEQ0)
				return Satisfiability.NOT_SATISFIED; // ≤0 is never greater than 0 or positives
			if (other == LE0)
				return Satisfiability.UNKNOWN;
		} else if (this == GE0) {
			if (other == NEG || other == LE0)
				return Satisfiability.SATISFIED; // ≥0 always greater than all negatives
			if (other == ZERO || other == POS || other == GE0 || other == NEQ0)
				return Satisfiability.UNKNOWN; // GE0 contains 0 and positives
		} else if (this == NEQ0) {
			if (other == ZERO)
				return Satisfiability.UNKNOWN;
			if (other == NEG || other == LE0)
				return Satisfiability.UNKNOWN; // NEQ0 contains NEG and POS
			if (other == POS || other == GE0)
				return Satisfiability.UNKNOWN;
			if (other == NEQ0)
				return Satisfiability.UNKNOWN;
		}
		return Satisfiability.UNKNOWN;
	}
}