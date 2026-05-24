package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.Objects;

public class SignTaintLattice implements BaseLattice<SignTaintLattice> {
	private final ExtendedSignLattice signLattice;
	private final TaintThreeLevelsLattice taintLattice;

	public static final SignTaintLattice TOP =
			new SignTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP);
	public static final SignTaintLattice BOTTOM =
			new SignTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);

	public SignTaintLattice(ExtendedSignLattice signLattice, TaintThreeLevelsLattice taintLattice) {
		this.signLattice = signLattice;
		this.taintLattice = taintLattice;
	}

	@Override
	public SignTaintLattice top() {
		return SignTaintLattice.TOP;
	}

	@Override
	public boolean isTop() {
		return this.signLattice.isTop() && this.taintLattice.isTop();
	}

	@Override
	public SignTaintLattice bottom() {
		return SignTaintLattice.BOTTOM;
	}

	@Override
	public boolean isBottom() {
		return this.signLattice.isBottom() && this.taintLattice.isBottom();
	}

	@Override
	public StructuredRepresentation representation() {
		if (this.isTop())
			return Lattice.topRepresentation();
		if (this.isBottom())
			return Lattice.bottomRepresentation();

		StructuredRepresentation s = this.signLattice.representation();
		StructuredRepresentation t = this.taintLattice.representation();
		return new StringRepresentation("SIGN: " + s.toString() + ", TAINT: " + t.toString());
	}

	@Override
	public SignTaintLattice lubAux(SignTaintLattice other) throws SemanticException {
		return new SignTaintLattice(
				this.signLattice.lub(other.signLattice),
				this.taintLattice.lub(other.taintLattice)
		);
	}

	@Override
	public SignTaintLattice glbAux(SignTaintLattice other) throws SemanticException {
		return new SignTaintLattice(
				this.signLattice.glb(other.signLattice),
				this.taintLattice.glb(other.taintLattice)
		);
	}

	@Override
	public boolean lessOrEqualAux(SignTaintLattice other) throws SemanticException {
		return this.signLattice.lessOrEqual(other.signLattice)
				&& this.taintLattice.lessOrEqual(other.taintLattice);
	}

	public ExtendedSignLattice getSignLattice() {
		return signLattice;
	}

	public TaintThreeLevelsLattice getTaintLattice() {
		return taintLattice;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		SignTaintLattice that = (SignTaintLattice) o;
		return Objects.equals(signLattice, that.signLattice) && Objects.equals(taintLattice, that.taintLattice);
	}

	@Override
	public int hashCode() {
		return Objects.hash(signLattice, taintLattice);
	}
}
