package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {
  @Override
  public ExtendedSignLattice top() {
    return ExtendedSignLattice.TOP;
  }

  @Override
  public ExtendedSignLattice bottom() {
    return ExtendedSignLattice.BOTTOM;
  }

  @Override
  public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
      throws SemanticException {

    if (constant.getValue() instanceof Integer numb) {
      if (numb == 0)
        return ExtendedSignLattice.ZERO;

      return (numb > 0) ? ExtendedSignLattice.POS : ExtendedSignLattice.NEG;
    }

    return ExtendedSignLattice.TOP;
  }

  @Override
  public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp,
      SemanticOracle oracle) throws SemanticException {

    if (expression.getOperator() instanceof NumericNegation)
      return arg.negation();

    return ExtendedSignLattice.TOP;
  }

  @Override
  public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
      ExtendedSignLattice right,
      ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
    if (expression.getOperator() instanceof AdditionOperator)
      return left.addition(right);

    else if (expression.getOperator() instanceof SubtractionOperator)
      return left.subtraction(right);

    else if (expression.getOperator() instanceof MultiplicationOperator)
      return left.multiplication(right);

    else if (expression.getOperator() instanceof DivisionOperator)
      return left.division(right);

    else if (expression.getOperator() instanceof ModuloOperator)
      return right;

    else if (expression.getOperator() instanceof RemainderOperator)
      return left;

    return ExtendedSignLattice.TOP;
  }

  @Override
  public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
      ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) {
    if (left.isTop() || right.isTop())
      return Satisfiability.UNKNOWN;

    BinaryOperator operator = expression.getOperator();
    if (operator == ComparisonEq.INSTANCE)
      return left.eq(right);

    else if (operator instanceof ComparisonGe)
      return left.eq(right).or(left.gt(right));

    else if (operator instanceof ComparisonGt)
      return left.gt(right);

    else if (operator instanceof ComparisonLe)
      return left.gt(right).negate();

    else if (operator instanceof ComparisonLt)
      return left.gt(right).negate().and(left.eq(right).negate());

    else if (operator instanceof ComparisonNe)
      return left.eq(right).negate();

    return Satisfiability.UNKNOWN;
  }

}
