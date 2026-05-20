package it.unive.scsr.analysis.extendedinterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedIntervalLattice
		implements BaseLattice<ExtendedIntervalLattice>,
		Comparable<ExtendedIntervalLattice> {

	private final MathNumber lower;
	private final MathNumber upper;
	private final boolean bottom;

	public static final ExtendedIntervalLattice TOP =
			new ExtendedIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);

	public static final ExtendedIntervalLattice BOTTOM =
			new ExtendedIntervalLattice(true);

	public static final ExtendedIntervalLattice ZERO =
			new ExtendedIntervalLattice(0, 0);

	private static final MathNumber[] THRESHOLDS = {
			MathNumber.MINUS_INFINITY,

			new MathNumber(-1000000),
			new MathNumber(-100000),
			new MathNumber(-10000),
			new MathNumber(-1000),
			new MathNumber(-100),
			new MathNumber(-10),
			new MathNumber(-1),
			new MathNumber(-0.1),
			MathNumber.ZERO,
			new MathNumber(0.1),
			new MathNumber(1),
			new MathNumber(10),
			new MathNumber(100),
			new MathNumber(1000),
			new MathNumber(10000),
			new MathNumber(100000),
			new MathNumber(1000000),

			MathNumber.PLUS_INFINITY
	};

	// BOTTOM constructor
	private ExtendedIntervalLattice(boolean bottom) {
		this.lower = null;
		this.upper = null;
		this.bottom = bottom;
	}

	public ExtendedIntervalLattice(MathNumber l, MathNumber u) {
		this.lower = l;
		this.upper = u;
		this.bottom = false;
	}

	public ExtendedIntervalLattice(int l, int u) {
		this(new MathNumber(l), new MathNumber(u));
	}

	public ExtendedIntervalLattice(double l, double u) {
		this(new MathNumber(l), new MathNumber(u));
	}

	public ExtendedIntervalLattice(long l, long u) {
		this(new MathNumber(l), new MathNumber(u));
	}

	public ExtendedIntervalLattice(float l, float u) {
		this(new MathNumber(l), new MathNumber(u));
	}

	public ExtendedIntervalLattice(short l, short u) {
		this(new MathNumber(l), new MathNumber(u));
	}

	public boolean isBottom() {
		return bottom;
	}

	public boolean isTop() {
		return !bottom
				&& lower.equals(MathNumber.MINUS_INFINITY)
				&& upper.equals(MathNumber.PLUS_INFINITY);
	}

	public MathNumber getHigh() {
		return upper;
	}

	public MathNumber getLow() {
		return lower;
	}

	@Override
	public ExtendedIntervalLattice top() {
		return TOP;
	}

	@Override
	public ExtendedIntervalLattice bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (isBottom())
			return Lattice.bottomRepresentation();

		return new StringRepresentation("[" + lower + "," + upper + "]");
	}

	@Override
	public ExtendedIntervalLattice lubAux(ExtendedIntervalLattice other)
			throws SemanticException {

		if (this.isBottom())
			return other;
		if (other.isBottom())
			return this;

		MathNumber l = this.lower.leq(other.lower) ? this.lower : other.lower;
		MathNumber u = this.upper.geq(other.upper) ? this.upper : other.upper;

		return new ExtendedIntervalLattice(l, u);
	}

	@Override
	public ExtendedIntervalLattice glbAux(ExtendedIntervalLattice other)
			throws SemanticException {

		if (this.isBottom() || other.isBottom())
			return BOTTOM;

		MathNumber l = this.lower.geq(other.lower) ? this.lower : other.lower;
		MathNumber u = this.upper.leq(other.upper) ? this.upper : other.upper;

		if (l.gt(u))
			return BOTTOM;

		return new ExtendedIntervalLattice(l, u);
	}

	@Override
	public boolean lessOrEqualAux(ExtendedIntervalLattice other)
			throws SemanticException {

		if (this.isBottom())
			return true;
		if (other.isBottom())
			return false;

		return other.lower.leq(this.lower)
				&& other.upper.geq(this.upper);
	}

	public boolean includes(ExtendedIntervalLattice other) {

		if (this.isBottom())
			return other.isBottom();
		if (other.isBottom())
			return true;

		return this.lower.leq(other.lower)
				&& this.upper.geq(other.upper);
	}

	@Override
	public ExtendedIntervalLattice wideningAux(ExtendedIntervalLattice other)
			throws SemanticException {

		if (this.isBottom())
			return other;
		if (other.isBottom())
			return this;

		MathNumber l = this.lower;
		MathNumber u = this.upper;

		if (other.lower.lt(this.lower))
			l = MathNumber.MINUS_INFINITY;
		if (other.upper.gt(this.upper))
			u = MathNumber.PLUS_INFINITY;

		return new ExtendedIntervalLattice(l, u);
	}

	@Override
	public ExtendedIntervalLattice narrowingAux(ExtendedIntervalLattice other) {

		// bottom propagation
		if (this.isBottom())
			return this;
		if (other.isBottom())
			return ExtendedIntervalLattice.BOTTOM;

		// if other is TOP, it gives no information -> keep current
		if (other.isTop())
			return this;

		MathNumber l1 = this.lower;
		MathNumber u1 = this.upper;

		MathNumber l2 = other.lower;
		MathNumber u2 = other.upper;

		MathNumber l;
		MathNumber u;

		// narrowing only refines bounds, never widens them

		// lower bound refinement
		if (l1.equals(MathNumber.MINUS_INFINITY)) {
			l = l2;
		} else {
			l = (l2.compareTo(l1) > 0) ? l2 : l1;
		}

		// upper bound refinement
		if (u1.equals(MathNumber.PLUS_INFINITY)) {
			u = u2;
		} else {
			u = (u2.compareTo(u1) < 0) ? u2 : u1;
		}

		return new ExtendedIntervalLattice(l, u);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lower, upper, bottom);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ExtendedIntervalLattice other)) return false;

		return bottom == other.bottom
				&& Objects.equals(lower, other.lower)
				&& Objects.equals(upper, other.upper);
	}

	@Override
	public int compareTo(ExtendedIntervalLattice o) {

		if (isBottom())
			return o.isBottom() ? 0 : -1;
		if (o.isBottom())
			return 1;

		if (isTop())
			return o.isTop() ? 0 : 1;
		if (o.isTop())
			return -1;

		int cmp = lower.compareTo(o.lower);
		if (cmp != 0) return cmp;

		return upper.compareTo(o.upper);
	}

	public Satisfiability eq(ExtendedIntervalLattice other) {

		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		// disjoint intervals => impossible equality
		if (this.upper.lt(other.lower)
				|| other.upper.lt(this.lower))
			return Satisfiability.NOT_SATISFIED;

		boolean singletonThis =
				this.lower.equals(this.upper);

		boolean singletonOther =
				other.lower.equals(other.upper);

		if (singletonThis
				&& singletonOther
				&& this.lower.equals(other.lower))
			return Satisfiability.SATISFIED;

		return Satisfiability.UNKNOWN;
	}

	public Satisfiability gt(ExtendedIntervalLattice other) {

		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		// definitely true
		if (this.lower.gt(other.upper))
			return Satisfiability.SATISFIED;

		// definitely false
		if (this.upper.leq(other.lower))
			return Satisfiability.NOT_SATISFIED;

		return Satisfiability.UNKNOWN;
	}
}