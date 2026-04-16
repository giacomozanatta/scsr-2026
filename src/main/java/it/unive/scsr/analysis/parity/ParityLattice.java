package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

public class ParityLattice implements BaseLattice<ParityLattice> {

	public static final ParityLattice TOP = new ParityLattice("TOP");
	public static final ParityLattice BOTTOM = new ParityLattice("BOTTOM");
	public static final ParityLattice EVEN = new ParityLattice("EVEN");
	public static final ParityLattice ODD = new ParityLattice("ODD");

	private final String name;

	public ParityLattice() {
		this("TOP");
	}

	private ParityLattice(String name) {
		this.name = name;
	}

	@Override
	public ParityLattice top() { return TOP; }

	@Override
	public ParityLattice bottom() { return BOTTOM; }

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		return this.equals(other) ? this : TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return this.equals(other);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ParityLattice other)) return false;
		return Objects.equals(this.name, other.name);
	}

	@Override
	public StructuredRepresentation representation() {
		return new StringRepresentation(name);
	}

	public Satisfiability eq(ParityLattice other) {
		if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
		if (this.isTop() || other.isTop()) return Satisfiability.UNKNOWN;
		return this.equals(other) ? Satisfiability.UNKNOWN : Satisfiability.NOT_SATISFIED;
	}

	public Satisfiability gt(ParityLattice other) {
		if (this.isBottom() || other.isBottom()) return Satisfiability.BOTTOM;
		return Satisfiability.UNKNOWN;
	}
}