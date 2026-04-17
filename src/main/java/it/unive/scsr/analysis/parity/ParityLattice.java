package it.unive.scsr.analysis.parity;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

	public static final ParityLattice EVEN = new ParityLattice(0);
	public static final ParityLattice ODD = new ParityLattice(1);
	public static final ParityLattice TOP = new ParityLattice(2);
	public static final ParityLattice BOTTOM = new ParityLattice(-1);

	private final Integer parity;

	public ParityLattice(Integer p) {
		this.parity = p;
	}

	@Override
	public ParityLattice top() {
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		return ParityLattice.BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == ParityLattice.TOP)
			return Lattice.topRepresentation();
		else if (this == ParityLattice.BOTTOM)
			return Lattice.bottomRepresentation();
		else if (this == ParityLattice.EVEN)
			return new StringRepresentation("EVEN");

		return new StringRepresentation("ODD");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return false;
	}

	public Satisfiability eq(ParityLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		if (this.isTop() || other.isTop())
			return Satisfiability.UNKNOWN;

		if (this.equals(other) == false)
			return Satisfiability.NOT_SATISFIED;

		// If both have the same parity -> could be equal, but not guaranteed
		return Satisfiability.UNKNOWN;
	}

	public Satisfiability gt(ParityLattice other) {
		if (this.isBottom() || other.isBottom())
			return Satisfiability.BOTTOM;

		return Satisfiability.UNKNOWN;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.parity);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ParityLattice))
			return false;

		ParityLattice other = (ParityLattice) obj;
		return Objects.equals(this.parity, other.parity);
	}
}