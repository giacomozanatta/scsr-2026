package it.unive.scsr.analysis.extendedinterval;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;

public class ExtendedInterval
		implements BaseNonRelationalValueDomain<ExtendedIntervalLattice> {

	@Override
	public ExtendedIntervalLattice top() {
		return ExtendedIntervalLattice.TOP;
	}

	@Override
	public ExtendedIntervalLattice bottom() {
		return ExtendedIntervalLattice.BOTTOM;
	}

	@Override
	public ExtendedIntervalLattice evalConstant(Constant constant,
												ProgramPoint pp,
												SemanticOracle oracle)
			throws SemanticException {

		Object v = constant.getValue();

		if (v instanceof Integer i)
			return new ExtendedIntervalLattice(i, i);
		if (v instanceof Double d)
			return new ExtendedIntervalLattice(d, d);
		if (v instanceof Long l)
			return new ExtendedIntervalLattice(l, l);
		if (v instanceof Short s)
			return new ExtendedIntervalLattice(s, s);
		if (v instanceof Float f)
			return new ExtendedIntervalLattice(f, f);

		return ExtendedIntervalLattice.TOP;
	}

	@Override
	public ExtendedIntervalLattice evalUnaryExpression(
			UnaryExpression expression,
			ExtendedIntervalLattice arg,
			ProgramPoint pp,
			SemanticOracle oracle)
			throws SemanticException {

		if (arg.isBottom())
			return ExtendedIntervalLattice.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			return new ExtendedIntervalLattice(
					arg.getHigh().multiply(MathNumber.MINUS_ONE),
					arg.getLow().multiply(MathNumber.MINUS_ONE)
			);
		}

		return ExtendedIntervalLattice.TOP;
	}

	@Override
	public ExtendedIntervalLattice evalBinaryExpression(
			BinaryExpression expression,
			ExtendedIntervalLattice left,
			ExtendedIntervalLattice right,
			ProgramPoint pp,
			SemanticOracle oracle)
			throws SemanticException {

		if (left.isBottom() || right.isBottom())
			return ExtendedIntervalLattice.BOTTOM;

		MathNumber l1 = left.getLow();
		MathNumber u1 = left.getHigh();
		MathNumber l2 = right.getLow();
		MathNumber u2 = right.getHigh();

		if (expression.getOperator() instanceof AdditionOperator) {
			return new ExtendedIntervalLattice(
					l1.add(l2),
					u1.add(u2)
			);
		}

		if (expression.getOperator() instanceof SubtractionOperator) {
			return new ExtendedIntervalLattice(
					l1.subtract(u2),
					u1.subtract(l2)
			);
		}

		if (expression.getOperator() instanceof MultiplicationOperator) {
			return mul(left, right);
		}

		if (expression.getOperator() instanceof DivisionOperator) {
			if (right.includes(ExtendedIntervalLattice.ZERO))
				return ExtendedIntervalLattice.TOP;

			ExtendedIntervalLattice inv =
					new ExtendedIntervalLattice(
							MathNumber.ONE.divide(u2),
							MathNumber.ONE.divide(l2)
					);

			return mul(left, inv);
		}

		return ExtendedIntervalLattice.TOP;
	}

	private ExtendedIntervalLattice mul(ExtendedIntervalLattice a,
										ExtendedIntervalLattice b) {

		if (a.isBottom() || b.isBottom())
			return ExtendedIntervalLattice.BOTTOM;

		MathNumber l1 = a.getLow();
		MathNumber u1 = a.getHigh();
		MathNumber l2 = b.getLow();
		MathNumber u2 = b.getHigh();

		MathNumber ll = l1.multiply(l2);
		MathNumber lu = l1.multiply(u2);
		MathNumber ul = u1.multiply(l2);
		MathNumber uu = u1.multiply(u2);

		MathNumber min = ll.min(lu).min(ul).min(uu);
		MathNumber max = ll.max(lu).max(ul).max(uu);

		return new ExtendedIntervalLattice(min, max);
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(
			BinaryExpression expression,
			ExtendedIntervalLattice left,
			ExtendedIntervalLattice right,
			ProgramPoint pp,
			SemanticOracle oracle) {

		if (left.isTop() || right.isTop())
			return Satisfiability.UNKNOWN;

		BinaryOperator op = expression.getOperator();

		if (op == ComparisonEq.INSTANCE)
			return left.eq(right);

		else if (op == ComparisonGe.INSTANCE)
			return left.gt(right)
					.or(left.eq(right));

		else if (op == ComparisonGt.INSTANCE)
			return left.gt(right);

		else if (op == ComparisonLe.INSTANCE)
			return left.gt(right).negate();

		else if (op == ComparisonLt.INSTANCE)
			return left.gt(right)
					.or(left.eq(right))
					.negate();

		else if (op == ComparisonNe.INSTANCE)
			return left.eq(right).negate();

		return Satisfiability.UNKNOWN;
	}

	@Override
	public ValueEnvironment<ExtendedIntervalLattice> assumeBinaryExpression(
			ValueEnvironment<ExtendedIntervalLattice> environment,
			BinaryExpression expression,
			ProgramPoint src,
			ProgramPoint dest,
			SemanticOracle oracle)
			throws SemanticException {

		Satisfiability sat =
				satisfies(environment, expression, src, oracle);

		if (sat == Satisfiability.NOT_SATISFIED)
			return environment.bottom();

		if (sat == Satisfiability.SATISFIED)
			return environment;

		BinaryOperator operator = expression.getOperator();

		ValueExpression left =
				(ValueExpression) expression.getLeft();

		ValueExpression right =
				(ValueExpression) expression.getRight();

		Identifier id;
		ExtendedIntervalLattice eval;
		boolean rightIsExpr;

		if (left instanceof Identifier) {
			id = (Identifier) left;
			eval = eval(environment, right, src, oracle);
			rightIsExpr = true;
		} else if (right instanceof Identifier) {
			id = (Identifier) right;
			eval = eval(environment, left, src, oracle);
			rightIsExpr = false;
		} else {
			return environment;
		}

		ExtendedIntervalLattice current =
				environment.getState(id);

		if (current.isBottom() || eval.isBottom())
			return environment.bottom();

		ExtendedIntervalLattice refined = current;

		MathNumber cLow = eval.getLow();
		MathNumber cHigh = eval.getHigh();

		if (operator == ComparisonEq.INSTANCE) {

			refined = current.glb(eval);

		} else if (operator == ComparisonLt.INSTANCE) {

			if (rightIsExpr) {
				refined = new ExtendedIntervalLattice(
						current.getLow(),
						min(current.getHigh(), cHigh)
				);
			} else {
				refined = new ExtendedIntervalLattice(
						max(current.getLow(), cLow),
						current.getHigh()
				);
			}

		} else if (operator == ComparisonLe.INSTANCE) {

			if (rightIsExpr) {
				refined = new ExtendedIntervalLattice(
						current.getLow(),
						min(current.getHigh(), cHigh)
				);
			} else {
				refined = new ExtendedIntervalLattice(
						max(current.getLow(), cLow),
						current.getHigh()
				);
			}

		} else if (operator == ComparisonGt.INSTANCE) {

			if (rightIsExpr) {
				refined = new ExtendedIntervalLattice(
						max(current.getLow(), cLow),
						current.getHigh()
				);
			} else {
				refined = new ExtendedIntervalLattice(
						current.getLow(),
						min(current.getHigh(), cHigh)
				);
			}

		} else if (operator == ComparisonGe.INSTANCE) {

			if (rightIsExpr) {
				refined = new ExtendedIntervalLattice(
						max(current.getLow(), cLow),
						current.getHigh()
				);
			} else {
				refined = new ExtendedIntervalLattice(
						current.getLow(),
						min(current.getHigh(), cHigh)
				);
			}

		} else if (operator == ComparisonNe.INSTANCE) {

			return environment;
		}

		if (refined.getLow().gt(refined.getHigh()))
			return environment.bottom();

		return environment.putState(id, refined);
	}

	private MathNumber min(MathNumber a, MathNumber b) {
		return a.leq(b) ? a : b;
	}

	private MathNumber max(MathNumber a, MathNumber b) {
		return a.geq(b) ? a : b;
	}
}