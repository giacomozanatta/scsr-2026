package it.unive.scsr.analysis.signx3taint;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.informationFlow.TaintLattice;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class SignX3TaintLattice implements TaintLattice<SignX3TaintLattice> {

	private final String sign;
	private final String taint3;

	public static final String Bottom = "BOTTOM";
	public static final String Clean  = "CLEAN";
	public static final String Taint  = "TAINT";
	public static final String Top    = "TOP";

	public static final String POS  = "+";
	public static final String NEG  = "-";
	public static final String ZERO = "0";

	public String getSign()   { return sign;   }
	public String get3Taint() { return taint3; }

	public SignX3TaintLattice(String s, String t) {
		sign   = s;
		taint3 = t;
	}

	@Override
	public boolean isTop() {
		return Top.equals(sign) && Top.equals(taint3);
	}

	@Override
	public boolean isBottom() {
		return Bottom.equals(sign) && Bottom.equals(taint3);
	}

	@Override
	public SignX3TaintLattice top() {
		return new SignX3TaintLattice(Top, Top);
	}

	@Override
	public SignX3TaintLattice bottom() {
		return new SignX3TaintLattice(Bottom, Bottom);
	}

	@Override
	public StructuredRepresentation representation() {
		StructuredRepresentation s;
		if (Bottom.equals(this.sign))
			s = Lattice.bottomRepresentation();
		else if (Top.equals(this.sign))
			s = Lattice.topRepresentation();
		else if (ZERO.equals(this.sign))
			s = new StringRepresentation("0");
		else if (POS.equals(this.sign))
			s = new StringRepresentation("+");
		else
			s = new StringRepresentation("-");

		StructuredRepresentation t;
		if (Bottom.equals(this.taint3))
			t = Lattice.bottomRepresentation();
		else if (Clean.equals(this.taint3))
			t = new StringRepresentation("C");
		else if (Taint.equals(this.taint3))
			t = new StringRepresentation("T");
		else
			t = Lattice.topRepresentation();

		return new StringRepresentation("[" + s + "," + t + "]");
	}

	@Override
	public SignX3TaintLattice lubAux(SignX3TaintLattice other) throws SemanticException {
		String s;
		if (this.sign.equals(other.sign))
			s = this.sign;
		else if (Bottom.equals(this.sign))
			s = other.sign;
		else if (Bottom.equals(other.sign))
			s = this.sign;
		else
			s = Top;

		String t;
		if (this.taint3.equals(other.taint3))
			t = this.taint3;
		else if (Bottom.equals(this.taint3))
			t = other.taint3;
		else if (Bottom.equals(other.taint3))
			t = this.taint3;
		else
			t = Top;

		return new SignX3TaintLattice(s, t);
	}

	@Override
	public boolean lessOrEqualAux(SignX3TaintLattice other) throws SemanticException {
		boolean signLeq;
		if (Bottom.equals(this.sign) || Top.equals(other.sign))
			signLeq = true;
		else
			signLeq = this.sign.equals(other.sign);

		boolean taintLeq;
		if (Bottom.equals(this.taint3) || Top.equals(other.taint3))
			taintLeq = true;
		else
			taintLeq = this.taint3.equals(other.taint3);

		return signLeq && taintLeq;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sign, taint3);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SignX3TaintLattice other = (SignX3TaintLattice) obj;
		return sign.equals(other.sign) && taint3.equals(other.taint3);
	}

	@Override
	public SignX3TaintLattice tainted() {
		return new SignX3TaintLattice(this.sign, Taint);
	}

	@Override
	public SignX3TaintLattice clean() {
		return new SignX3TaintLattice(this.sign, Clean);
	}

	@Override
	public SignX3TaintLattice or(SignX3TaintLattice other) throws SemanticException {
		String t;
		if (this.taint3.equals(other.taint3))
			t = this.taint3;
		else if (Bottom.equals(this.taint3))
			t = other.taint3;
		else if (Bottom.equals(other.taint3))
			t = this.taint3;
		else
			t = Top;

		return new SignX3TaintLattice(this.sign, t);
	}

	@Override
	public boolean isAlwaysTainted() {
		return Taint.equals(this.taint3);
	}

	@Override
	public boolean isPossiblyTainted() {
		return Top.equals(this.taint3);
	}
}