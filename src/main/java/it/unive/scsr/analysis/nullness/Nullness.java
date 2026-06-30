package it.unive.scsr.analysis.nullness;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;

public class Nullness implements BaseNonRelationalValueDomain<NullnessLattice> {

  @Override
  public NullnessLattice top() {
    return NullnessLattice.TOP;
  }

  @Override
  public NullnessLattice bottom() {
    return NullnessLattice.BOTTOM;
  }

  @Override
  public NullnessLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
      throws SemanticException {
    // adjust this check to however your frontend represents the null literal
    // (e.g. `constant instanceof NullConstant` in some LiSA frontends)
    return constant.getValue() == null ? NullnessLattice.NULL : NullnessLattice.NOT_NULL;
  }

  @Override
  public NullnessLattice evalIdentifier(
      Identifier id,
      ValueEnvironment<NullnessLattice> environment,
      ProgramPoint pp,
      SemanticOracle oracle) throws SemanticException {
    return environment.getState(id);
  }

  @Override
  public NullnessLattice evalUnaryExpression(
      UnaryExpression expression,
      NullnessLattice arg,
      ProgramPoint pp,
      SemanticOracle oracle) throws SemanticException {
    return arg; // casts/negation etc. don't change nullness in most languages
  }

  @Override
  public NullnessLattice evalBinaryExpression(
      BinaryExpression expression,
      NullnessLattice left,
      NullnessLattice right,
      ProgramPoint pp,
      SemanticOracle oracle) throws SemanticException {
    return left.lub(right); // see caveat below
  }
}