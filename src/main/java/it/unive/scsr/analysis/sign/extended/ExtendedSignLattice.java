package it.unive.scsr.analysis.sign.extended;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

	static final int NEG_MASK = 1;
	static final int ZERO_MASK = 2;
	static final int POS_MASK = 4;
	static final int TOP_MASK = NEG_MASK | ZERO_MASK | POS_MASK;

	public static final ExtendedSignLattice BOTTOM = new ExtendedSignLattice(0);
	public static final ExtendedSignLattice NEG = new ExtendedSignLattice(NEG_MASK);
	public static final ExtendedSignLattice ZERO = new ExtendedSignLattice(ZERO_MASK);
	public static final ExtendedSignLattice POS = new ExtendedSignLattice(POS_MASK);
	public static final ExtendedSignLattice NON_POS = new ExtendedSignLattice(NEG_MASK | ZERO_MASK);
	public static final ExtendedSignLattice NON_ZERO = new ExtendedSignLattice(NEG_MASK | POS_MASK);
	public static final ExtendedSignLattice NON_NEG = new ExtendedSignLattice(ZERO_MASK | POS_MASK);
	public static final ExtendedSignLattice TOP = new ExtendedSignLattice(TOP_MASK);

	private final int mask;

	public ExtendedSignLattice() {
		this(TOP_MASK);
	}

	private ExtendedSignLattice(int mask) {
		this.mask = mask;
	}

	static ExtendedSignLattice fromMask(int mask) {
		switch (mask) {
		case 0:
			return BOTTOM;
		case NEG_MASK:
			return NEG;
		case ZERO_MASK:
			return ZERO;
		case POS_MASK:
			return POS;
		case NEG_MASK | ZERO_MASK:
			return NON_POS;
		case NEG_MASK | POS_MASK:
			return NON_ZERO;
		case ZERO_MASK | POS_MASK:
			return NON_NEG;
		case TOP_MASK:
			return TOP;
		default:
			throw new IllegalArgumentException("Unknown extended sign mask: " + mask);
		}
	}

	boolean contains(ExtendedSignLattice atom) {
		return (mask & atom.mask) != 0;
	}

	public boolean mayBeNegative() {
		return (mask & NEG_MASK) != 0;
	}

	public boolean isDefinitelyNegative() {
		return mask == NEG_MASK;
	}

	public boolean mayBeZero() {
		return (mask & ZERO_MASK) != 0;
	}

	public boolean mayBePositive() {
		return (mask & POS_MASK) != 0;
	}

	boolean containsZero() {
		return (mask & ZERO_MASK) != 0;
	}

	boolean isSingleton() {
		return mask == NEG_MASK || mask == ZERO_MASK || mask == POS_MASK;
	}

	int mask() {
		return mask;
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
	public StructuredRepresentation representation() {
		if (this.equals(BOTTOM))
			return Lattice.bottomRepresentation();
		if (this.equals(TOP))
			return Lattice.topRepresentation();
		if (this.equals(NEG))
			return new StringRepresentation("-");
		if (this.equals(ZERO))
			return new StringRepresentation("0");
		if (this.equals(POS))
			return new StringRepresentation("+");
		if (this.equals(NON_POS))
			return new StringRepresentation("<=0");
		if (this.equals(NON_ZERO))
			return new StringRepresentation("!=0");
		return new StringRepresentation(">=0");
	}

	@Override
	public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
		return fromMask(mask | other.mask);
	}

	@Override
	public ExtendedSignLattice glbAux(ExtendedSignLattice other) throws SemanticException {
		return fromMask(mask & other.mask);
	}

	@Override
	public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
		return (mask | other.mask) == other.mask;
	}

	public Satisfiability eq(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;
		if ((mask & other.mask) == 0)
			return Satisfiability.NOT_SATISFIED;
		if (this.equals(ZERO) && other.equals(ZERO))
			return Satisfiability.SATISFIED;
		return Satisfiability.UNKNOWN;
	}

	public Satisfiability gt(ExtendedSignLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		boolean mayBeTrue = false;
		boolean mayBeFalse = false;
		for (ExtendedSignLattice left : atoms())
			if (contains(left))
				for (ExtendedSignLattice right : atoms())
					if (other.contains(right)) {
						Satisfiability atom = atomGt(left, right);
						mayBeTrue |= atom.mightBeTrue();
						mayBeFalse |= atom.mightBeFalse();
					}

		if (mayBeTrue && mayBeFalse)
			return Satisfiability.UNKNOWN;
		return mayBeTrue ? Satisfiability.SATISFIED : Satisfiability.NOT_SATISFIED;
	}

	private static Satisfiability atomGt(ExtendedSignLattice left, ExtendedSignLattice right) {
		if (left == NEG)
			return right == NEG ? Satisfiability.UNKNOWN : Satisfiability.NOT_SATISFIED;
		if (left == ZERO)
			return right == NEG ? Satisfiability.SATISFIED : Satisfiability.NOT_SATISFIED;
		return right == POS ? Satisfiability.UNKNOWN : Satisfiability.SATISFIED;
	}

	static ExtendedSignLattice[] atoms() {
		return new ExtendedSignLattice[] { NEG, ZERO, POS };
	}

	@Override
	public int hashCode() {
		return Objects.hash(mask);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		return mask == ((ExtendedSignLattice) obj).mask;
	}

	@Override
	public String toString() {
		return representation().toString();
	}
}
