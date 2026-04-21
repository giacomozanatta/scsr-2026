package it.unive.scsr.analysis.interval;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class IntervalLattice implements BaseLattice<IntervalLattice>, Comparable<IntervalLattice> {

	IntInterval i;

	public static final IntervalLattice TOP = new IntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
	public static final IntervalLattice BOTTOM = new IntervalLattice(null);

	public IntervalLattice(IntInterval i) {
		this.i = i;
	}

	public IntervalLattice(MathNumber l, MathNumber u) {
		this.i = (l == null || u == null) ? null : new IntInterval(l, u);
	}

	public IntervalLattice(int l, int u) {
		this.i = new IntInterval(l, u);
	}

	@Override
	public IntervalLattice top() { return TOP; }

	@Override
	public IntervalLattice bottom() { return BOTTOM; }

	@Override
	public boolean isBottom() { return i == null; }

	@Override
	public StructuredRepresentation representation() {
		if (isBottom()) return Lattice.bottomRepresentation();
		if (this.equals(TOP)) return Lattice.topRepresentation();
		return new StringRepresentation("[" + i.getLow() + ", " + i.getHigh() + "]");
	}

	@Override
	public IntervalLattice lubAux(IntervalLattice other) throws SemanticException {
		MathNumber low = i.getLow().min(other.i.getLow());
		MathNumber high = i.getHigh().max(other.i.getHigh());
		return new IntervalLattice(low, high);
	}

	@Override
	public IntervalLattice glbAux(IntervalLattice other) throws SemanticException {
		MathNumber low = i.getLow().max(other.i.getLow());
		MathNumber high = i.getHigh().min(other.i.getHigh());
		if (low.gt(high)) return BOTTOM;
		return new IntervalLattice(low, high);
	}

	@Override
	public boolean lessOrEqualAux(IntervalLattice other) throws SemanticException {
		return other.i.includes(this.i);
	}

	@Override
	public IntervalLattice wideningAux(IntervalLattice other) throws SemanticException {
		MathNumber low = other.i.getLow().lt(i.getLow()) ? MathNumber.MINUS_INFINITY : i.getLow();
		MathNumber high = other.i.getHigh().gt(i.getHigh()) ? MathNumber.PLUS_INFINITY : i.getHigh();
		return new IntervalLattice(low, high);
	}

	@Override
	public int hashCode() { return Objects.hash(i); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		IntervalLattice other = (IntervalLattice) obj;
		return Objects.equals(i, other.i);
	}

	@Override
	public int compareTo(IntervalLattice o) {
		if (this.isBottom()) return o.isBottom() ? 0 : -1;
		if (o.isBottom()) return 1;
		return this.i.getLow().equals(o.i.getLow())
				? this.i.getHigh().compareTo(o.i.getHigh())
				: this.i.getLow().compareTo(o.i.getLow());
	}
}