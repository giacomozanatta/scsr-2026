package it.unive.scsr.analysis.interval.extended;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;

public class NumericInterval implements BaseNonRelationalValueDomain<NumericIntervalLattice> {

  @Override
  public NumericIntervalLattice top() {
    return NumericIntervalLattice.TOP;
  }

  @Override
  public NumericIntervalLattice bottom() {
    return NumericIntervalLattice.BOTTOM;
  }

  @Override
  public NumericIntervalLattice evalConstant(
      Constant constant,
      ProgramPoint pp,
      SemanticOracle oracle)
      throws SemanticException {

    Object c = constant.getValue();

    if (c instanceof Integer i)
      return new NumericIntervalLattice(i, i);

    if (c instanceof Long l)
      return new NumericIntervalLattice(l, l);

    if (c instanceof Float f)
      return new NumericIntervalLattice(f, f);

    if (c instanceof Double d)
      return new NumericIntervalLattice(d, d);

    return top();
  }

  @Override
  public NumericIntervalLattice evalUnaryExpression(
      UnaryExpression expression,
      NumericIntervalLattice arg,
      ProgramPoint pp,
      SemanticOracle oracle)
      throws SemanticException {

    if (arg.isBottom())
      return bottom();

    if (expression.getOperator() == NumericNegation.INSTANCE)
      return new NumericIntervalLattice(
          arg.getHigh().multiply(MathNumber.MINUS_ONE),
          arg.getLow().multiply(MathNumber.MINUS_ONE));

    return top();
  }

  @Override
  public NumericIntervalLattice evalBinaryExpression(
      BinaryExpression expression,
      NumericIntervalLattice left,
      NumericIntervalLattice right,
      ProgramPoint pp,
      SemanticOracle oracle)
      throws SemanticException {

    if (left.isBottom() || right.isBottom())
      return bottom();

    MathNumber lowerLeft = left.getLow();
    MathNumber upperLeft = left.getHigh();

    MathNumber lowerRight = right.getLow();
    MathNumber upperRight = right.getHigh();

    BinaryOperator op = expression.getOperator();

    if (op instanceof AdditionOperator)
      return new NumericIntervalLattice(
          lowerLeft.add(lowerRight),
          upperLeft.add(upperRight));

    if (op instanceof SubtractionOperator)
      return new NumericIntervalLattice(
          lowerLeft.subtract(upperRight),
          upperLeft.subtract(lowerRight));

    if (op instanceof MultiplicationOperator) {
      if (isZero(left) || isZero(right))
        return NumericIntervalLattice.ZERO;

      List<MathNumber> bounds = Arrays.asList(
          safeMultiply(lowerLeft, lowerRight),
          safeMultiply(lowerLeft, upperRight),
          safeMultiply(upperLeft, lowerRight),
          safeMultiply(upperLeft, upperRight));

      return new NumericIntervalLattice(
          Collections.min(bounds),
          Collections.max(bounds));
    }

    if (op instanceof DivisionOperator) {
      if (isZero(right))
        return bottom();

      if (containsZero(right))
        return top();

      if (isZero(left))
        return NumericIntervalLattice.ZERO;

      List<MathNumber> bounds = Arrays.asList(
          lowerLeft.divide(lowerRight),
          lowerLeft.divide(upperRight),
          upperLeft.divide(lowerRight),
          upperLeft.divide(upperRight));

      return new NumericIntervalLattice(
          Collections.min(bounds),
          Collections.max(bounds));
    }

    return top();
  }

  // @Override
  // public Satisfiability satisfiesBinaryExpression(
  // BinaryExpression expression,
  // NumericIntervalLattice left,
  // NumericIntervalLattice right,
  // ProgramPoint pp,
  // SemanticOracle oracle)
  // throws SemanticException {

  // if (left.isBottom() || right.isBottom())
  // return Satisfiability.BOTTOM;

  // BinaryOperator op = expression.getOperator();

  // if (op == ComparisonEq.INSTANCE)
  // return eq(left, right);

  // if (op == ComparisonNe.INSTANCE)
  // return eq(left, right).negate();

  // if (op == ComparisonGt.INSTANCE)
  // return gt(left, right);

  // if (op == ComparisonGe.INSTANCE)
  // return ge(left, right);

  // if (op == ComparisonLt.INSTANCE)
  // return lt(left, right);

  // if (op == ComparisonLe.INSTANCE)
  // return le(left, right);

  // return Satisfiability.UNKNOWN;
  // }

  private static boolean isZero(NumericIntervalLattice interval) {
    return !interval.isBottom()
        && interval.getLow().equals(MathNumber.ZERO)
        && interval.getHigh().equals(MathNumber.ZERO);
  }

  private static boolean containsZero(NumericIntervalLattice interval) {
    return !interval.isBottom()
        && interval.getLow().leq(MathNumber.ZERO)
        && interval.getHigh().geq(MathNumber.ZERO);
  }

  private static MathNumber safeMultiply(MathNumber left, MathNumber right) {
    if ((left.isZero() && right.isInfinite()) ||
        (right.isZero() && left.isInfinite()))
      return MathNumber.ZERO;

    return left.multiply(right);
  }

  // private static Satisfiability eq(
  // NumericIntervalLattice left,
  // NumericIntervalLattice right) {

  // if (left.getHigh().lt(right.getLow()) ||
  // left.getLow().gt(right.getHigh()))
  // return Satisfiability.NOT_SATISFIED;

  // if (isSingleton(left)
  // && isSingleton(right)
  // && left.getLow().equals(right.getLow()))
  // return Satisfiability.SATISFIED;

  // return Satisfiability.UNKNOWN;
  // }

  // private static Satisfiability gt(
  // NumericIntervalLattice left,
  // NumericIntervalLattice right) {

  // if (left.getLow().gt(right.getHigh()))
  // return Satisfiability.SATISFIED;

  // if (left.getHigh().leq(right.getLow()))
  // return Satisfiability.NOT_SATISFIED;

  // return Satisfiability.UNKNOWN;
  // }

  // private static Satisfiability ge(
  // NumericIntervalLattice left,
  // NumericIntervalLattice right) {

  // if (left.getLow().geq(right.getHigh()))
  // return Satisfiability.SATISFIED;

  // if (left.getHigh().lt(right.getLow()))
  // return Satisfiability.NOT_SATISFIED;

  // return Satisfiability.UNKNOWN;
  // }

  // private static Satisfiability lt(
  // NumericIntervalLattice left,
  // NumericIntervalLattice right) {

  // if (left.getHigh().lt(right.getLow()))
  // return Satisfiability.SATISFIED;

  // if (left.getLow().geq(right.getHigh()))
  // return Satisfiability.NOT_SATISFIED;

  // return Satisfiability.UNKNOWN;
  // }

  // private static Satisfiability le(
  // NumericIntervalLattice left,
  // NumericIntervalLattice right) {

  // if (left.getHigh().leq(right.getLow()))
  // return Satisfiability.SATISFIED;

  // if (left.getLow().gt(right.getHigh()))
  // return Satisfiability.NOT_SATISFIED;

  // return Satisfiability.UNKNOWN;
  // }
}
