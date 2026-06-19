package it.unive.scsr.analysis.cartesian;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignThreeTaintLattice implements BaseLattice<SignThreeTaintLattice> {

  private final ExtendedSignLattice sign;
  private final TaintThreeLevelsLattice taint;

  public static final SignThreeTaintLattice BOTTOM = new SignThreeTaintLattice(
      ExtendedSignLattice.BOTTOM,
      TaintThreeLevelsLattice.BOTTOM);

  public static final SignThreeTaintLattice TOP = new SignThreeTaintLattice(
      ExtendedSignLattice.TOP,
      TaintThreeLevelsLattice.TOP);

  public SignThreeTaintLattice(
      ExtendedSignLattice sign,
      TaintThreeLevelsLattice taint) {
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
  public int hashCode() {
    return Objects.hash(sign, taint);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj instanceof SignThreeTaintLattice other)
      return sign.equals(other.getSign()) && taint.equals(other.getTaint());

    return false;
  }

  @Override
  public SignThreeTaintLattice top() {
    return TOP;
  }

  @Override
  public SignThreeTaintLattice bottom() {
    return BOTTOM;
  }

  @Override
  public StructuredRepresentation representation() {
    return new ListRepresentation(sign.representation(), taint.representation());
  }

  @Override
  public SignThreeTaintLattice lubAux(SignThreeTaintLattice other) throws SemanticException {
    return new SignThreeTaintLattice(
        sign.lub(other.getSign()),
        taint.lub(other.getTaint()));
  }

  @Override
  public SignThreeTaintLattice glbAux(SignThreeTaintLattice other) throws SemanticException {
    return new SignThreeTaintLattice(sign.glb(other.getSign()), taint.glb(other.getTaint()));
  }

  @Override
  public boolean lessOrEqualAux(SignThreeTaintLattice other) throws SemanticException {
    return sign.lessOrEqual(other.getSign()) && taint.lessOrEqual(other.getTaint());
  }

  public Satisfiability eq(SignThreeTaintLattice other) throws SemanticException {
    if (other == null)
      return Satisfiability.UNKNOWN;

    if (this.equals(BOTTOM) || other.equals(BOTTOM))
      return Satisfiability.BOTTOM;

    return sign.eq(other.getSign());
  }

  public Satisfiability gt(SignThreeTaintLattice other) throws SemanticException {
    if (other == null)
      return Satisfiability.UNKNOWN;

    if (this.equals(BOTTOM) || other.equals(BOTTOM))
      return Satisfiability.BOTTOM;

    return sign.gt(other.getSign());
  }

}
