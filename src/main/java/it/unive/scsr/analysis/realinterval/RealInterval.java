package it.unive.scsr.analysis.realinterval;

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

public class RealInterval implements BaseNonRelationalValueDomain<RealIntervalLattice> {

	@Override
	public RealIntervalLattice top() {
		return RealIntervalLattice.TOP;
	}

	@Override
	public RealIntervalLattice bottom() {
		return RealIntervalLattice.BOTTOM;
	}

	@Override
	public RealIntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		Object value = constant.getValue();
		if (value instanceof Number) {
			double number = ((Number) value).doubleValue();
			if (Double.isNaN(number))
				return RealIntervalLattice.TOP;
			return new RealIntervalLattice(number);
		}
		return RealIntervalLattice.TOP;
	}

	@Override
	public RealIntervalLattice evalUnaryExpression(UnaryExpression expression, RealIntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		if (arg.isBottom())
			return RealIntervalLattice.BOTTOM;
		if (expression.getOperator() == NumericNegation.INSTANCE)
			return new RealIntervalLattice(-arg.getHigh(), -arg.getLow());
		return RealIntervalLattice.TOP;
	}

	@Override
	public RealIntervalLattice evalBinaryExpression(BinaryExpression expression, RealIntervalLattice left,
			RealIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (left.isBottom() || right.isBottom())
			return RealIntervalLattice.BOTTOM;

		BinaryOperator op = expression.getOperator();
		if (op instanceof AdditionOperator)
			return new RealIntervalLattice(left.getLow() + right.getLow(), left.getHigh() + right.getHigh());
		if (op instanceof SubtractionOperator)
			return new RealIntervalLattice(left.getLow() - right.getHigh(), left.getHigh() - right.getLow());
		if (op instanceof MultiplicationOperator)
			return multiply(left, right);
		if (op instanceof DivisionOperator)
			return divide(left, right);
		if (op instanceof ModuloOperator || op instanceof RemainderOperator)
			return remainder(left, right);
		return RealIntervalLattice.TOP;
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, RealIntervalLattice left,
			RealIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (left.isBottom() || right.isBottom())
			return Satisfiability.BOTTOM;

		BinaryOperator op = expression.getOperator();
		if (op == ComparisonEq.INSTANCE) {
			if (left.getHigh() < right.getLow() || right.getHigh() < left.getLow())
				return Satisfiability.NOT_SATISFIED;
			if (left.getLow() == left.getHigh() && left.getLow() == right.getLow() && right.getLow() == right.getHigh())
				return Satisfiability.SATISFIED;
			return Satisfiability.UNKNOWN;
		}
		if (op == ComparisonNe.INSTANCE) {
			Satisfiability eq;
			if (left.getHigh() < right.getLow() || right.getHigh() < left.getLow())
				eq = Satisfiability.NOT_SATISFIED;
			else if (left.getLow() == left.getHigh() && left.getLow() == right.getLow()
					&& right.getLow() == right.getHigh())
				eq = Satisfiability.SATISFIED;
			else
				eq = Satisfiability.UNKNOWN;
			return eq.negate();
		}
		if (op == ComparisonGt.INSTANCE)
			return compare(left.getLow() > right.getHigh(), left.getHigh() <= right.getLow());
		if (op == ComparisonGe.INSTANCE)
			return compare(left.getLow() >= right.getHigh(), left.getHigh() < right.getLow());
		if (op == ComparisonLt.INSTANCE)
			return compare(left.getHigh() < right.getLow(), left.getLow() >= right.getHigh());
		if (op == ComparisonLe.INSTANCE)
			return compare(left.getHigh() <= right.getLow(), left.getLow() > right.getHigh());
		return Satisfiability.UNKNOWN;
	}

	private static Satisfiability compare(boolean definitelyTrue, boolean definitelyFalse) {
		if (definitelyTrue)
			return Satisfiability.SATISFIED;
		if (definitelyFalse)
			return Satisfiability.NOT_SATISFIED;
		return Satisfiability.UNKNOWN;
	}

	private static RealIntervalLattice multiply(RealIntervalLattice left, RealIntervalLattice right) {
		if (left.isZero() || right.isZero())
			return RealIntervalLattice.ZERO;
		double a = multiplyBound(left.getLow(), right.getLow());
		double b = multiplyBound(left.getLow(), right.getHigh());
		double c = multiplyBound(left.getHigh(), right.getLow());
		double d = multiplyBound(left.getHigh(), right.getHigh());
		return new RealIntervalLattice(min(a, b, c, d), max(a, b, c, d));
	}

	private static RealIntervalLattice divide(RealIntervalLattice left, RealIntervalLattice right) {
		if (right.isZero())
			return RealIntervalLattice.BOTTOM;
		if (right.containsZero())
			return RealIntervalLattice.TOP;
		RealIntervalLattice reciprocal = new RealIntervalLattice(1.0 / right.getHigh(), 1.0 / right.getLow());
		return multiply(left, reciprocal);
	}

	private static RealIntervalLattice remainder(RealIntervalLattice left, RealIntervalLattice right) {
		if (right.isZero())
			return RealIntervalLattice.BOTTOM;
		if (left.isZero())
			return RealIntervalLattice.ZERO;
		if (right.containsZero() || Double.isInfinite(right.getLow()) || Double.isInfinite(right.getHigh()))
			return RealIntervalLattice.TOP;
		double bound = Math.max(Math.abs(right.getLow()), Math.abs(right.getHigh()));
		return new RealIntervalLattice(-bound, bound);
	}

	private static double multiplyBound(double left, double right) {
		if (left == 0.0 || right == 0.0)
			return 0.0;
		return left * right;
	}

	private static double min(double a, double b, double c, double d) {
		return Math.min(Math.min(a, b), Math.min(c, d));
	}

	private static double max(double a, double b, double c, double d) {
		return Math.max(Math.max(a, b), Math.max(c, d));
	}
}
