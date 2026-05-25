package it.unive.scsr.analysis.sign.taint;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

/**
 * A Cartesian product lattice combining sign and taint abstractions.
 *
 * <p>This lattice pairs an {@link ExtendedSignLattice} with a
 * {@link TaintThreeLevelsLattice} to track both numeric sign and taint
 * information in a single abstract value.</p>
 */
public class SignTaintLattice implements BaseLattice<SignTaintLattice> {

	public static final SignTaintLattice TOP = new SignTaintLattice(ExtendedSignLattice.TOP,
			TaintThreeLevelsLattice.TOP);
	public static final SignTaintLattice BOTTOM = new SignTaintLattice(ExtendedSignLattice.BOTTOM,
			TaintThreeLevelsLattice.BOTTOM);

	private final ExtendedSignLattice sign;
	private final TaintThreeLevelsLattice taint;

	public SignTaintLattice() {
		this(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP);
	}

	public SignTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
		this.sign = sign;
		this.taint = taint;
	}

	public ExtendedSignLattice getSign() {
		return sign;
	}

	public TaintThreeLevelsLattice getTaint() {
		return taint;
	}

	@Override
	public SignTaintLattice top() {
		return TOP;
	}

	@Override
	public SignTaintLattice bottom() {
		return BOTTOM;
	}

	@Override
	public boolean isTop() {
		return sign.isTop() && taint.isTop();
	}

	@Override
	public boolean isBottom() {
		return sign.isBottom() && taint.isBottom();
	}

	@Override
	public StructuredRepresentation representation() {
		return new StringRepresentation("(" + sign.representation() + ", " + taint.representation() + ")");
	}

	@Override
	public SignTaintLattice lubAux(SignTaintLattice other) throws SemanticException {
		return new SignTaintLattice(sign.lub(other.sign), taint.lub(other.taint));
	}

	@Override
	public SignTaintLattice glbAux(SignTaintLattice other) throws SemanticException {
		return new SignTaintLattice(sign.glb(other.sign), taint.glb(other.taint));
	}

	@Override
	public SignTaintLattice wideningAux(SignTaintLattice other) throws SemanticException {
		return new SignTaintLattice(sign.widening(other.sign), taint.widening(other.taint));
	}

	@Override
	public boolean lessOrEqualAux(SignTaintLattice other) throws SemanticException {
		return sign.lessOrEqual(other.sign) && taint.lessOrEqual(other.taint);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sign, taint);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		SignTaintLattice other = (SignTaintLattice) obj;
		return Objects.equals(sign, other.sign) && Objects.equals(taint, other.taint);
	}

	@Override
	public String toString() {
		return representation().toString();
	}
}
