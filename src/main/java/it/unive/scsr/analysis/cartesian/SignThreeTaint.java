package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.sign.extended.ExtendedSign;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;

public class SignThreeTaint implements BaseNonRelationalValueDomain<SignThreeTaintLattice> {

  private final ExtendedSign sign;
  private final TaintThreeLevels taint;

  public SignThreeTaint() {
    sign = new ExtendedSign();
    taint = new TaintThreeLevels();
  }

  @Override
  public SignThreeTaintLattice top() {
    return SignThreeTaintLattice.TOP;
  }

  @Override
  public SignThreeTaintLattice bottom() {
    return SignThreeTaintLattice.BOTTOM;
  }

  @Override
  public SignThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
      throws SemanticException {
    return new SignThreeTaintLattice(
        sign.evalConstant(constant, pp, oracle),
        taint.evalConstant(constant, pp, oracle));
  }

  @Override
  public SignThreeTaintLattice evalUnaryExpression(UnaryExpression expression, SignThreeTaintLattice arg,
      ProgramPoint pp,
      SemanticOracle oracle) throws SemanticException {
    return new SignThreeTaintLattice(
        sign.evalUnaryExpression(expression, arg.getSign(), pp, oracle),
        taint.evalUnaryExpression(expression, arg.getTaint(), pp, oracle));
  }

  @Override
  public SignThreeTaintLattice evalBinaryExpression(BinaryExpression expression, SignThreeTaintLattice left,
      SignThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
    return new SignThreeTaintLattice(
        sign.evalBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle),
        taint.evalBinaryExpression(expression, left.getTaint(), right.getTaint(), pp, oracle));
  }

}
