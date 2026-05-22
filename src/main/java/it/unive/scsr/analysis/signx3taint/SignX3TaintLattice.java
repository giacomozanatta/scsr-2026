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

	public static String Bottom = "BOTTOM";
	public static String Clean = "CLEAN";
	public static String Taint = "TAINT";
	public static String Top = "TOP";

	public static String POS = "+";
	public static String NEG = "-";
	public static String ZERO = "0";

	public String getSign(){
		return sign;
	}

	public String get3Taint(){
		return taint3;
	}


	public SignX3TaintLattice(String s, String t){
		sign = s;
		taint3 = t;
	}

	@Override
	public boolean isTop() {
		return sign == Top && taint3 == Top;
	}

	@Override
	public boolean isBottom() {
		return sign == Bottom && taint3 == Bottom;
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

		StructuredRepresentation s = new StringRepresentation("-");
		StructuredRepresentation t = Lattice.topRepresentation();

		if(this.sign == Bottom)
			s = Lattice.bottomRepresentation();
		else if(this.sign == Top)
			s = Lattice.topRepresentation();
		else if(this.sign == ZERO)
			s = new StringRepresentation("0");
		else if(this.sign == POS)
			s = new StringRepresentation("+");

		if(this.taint3 == Bottom)
			t = Lattice.bottomRepresentation();

		if(this.taint3  == Clean)
			t = new StringRepresentation("C");

		if(this.taint3  == Taint)
			t = new StringRepresentation("T");


		return new StringRepresentation("[" + s + "," + t + "]");
	}

	@Override
	public SignX3TaintLattice lubAux(SignX3TaintLattice other) throws SemanticException {
		String s = Top;
		String t = Top;

		if (this.sign.equals(other.getSign()))
			s = this.sign;

		if (this.sign == Bottom)
			s = other.getSign();

		if (other.getSign() == Bottom)
			s = this.sign;

		return new SignX3TaintLattice(s,t);
	}

	@Override
	public boolean lessOrEqualAux(SignX3TaintLattice other) throws SemanticException {

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sign, taint3);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SignX3TaintLattice other = (SignX3TaintLattice) obj;

		return sign.equals(other.getSign() )&& taint3.equals(other.get3Taint());
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
		String t = Clean;
		if (this.taint3 == Taint || other.get3Taint() == Taint)
			t = Taint;

		if (this.taint3 == Top || other.get3Taint() == Top)
			t = Top;

		return new SignX3TaintLattice(this.sign,t);

	}

	@Override
	public boolean isAlwaysTainted() {
		return this.taint3 == Taint;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this.taint3 == Top;
	}
}