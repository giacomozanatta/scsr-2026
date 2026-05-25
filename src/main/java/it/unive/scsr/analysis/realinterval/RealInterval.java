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

	/*
	 * Real interval abstract domain implementation. Each method maps
	 * concrete numeric operations to safe interval approximations.
	 *
	 * Key concepts used here:
	 * - TOP: unknown interval (could be any real number)
	 * - BOTTOM: inconsistent / unreachable state
	 * - Intervals are closed [low, high]
	 *
	 * The implementation tries to be conservative: when exact
	 * information cannot be determined (division by intervals
	 * containing zero, remainder with unbounded operands, etc.),
	 * it returns TOP or BOTTOM as appropriate.
	 */

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
			// NaN cannot be represented as a precise interval -> conservatively TOP
			if (Double.isNaN(number))
				return RealIntervalLattice.TOP;
			// Represent concrete numeric constant as the singleton interval [n, n]
			return new RealIntervalLattice(number);
		}
		return RealIntervalLattice.TOP;
	}

	@Override
	public RealIntervalLattice evalUnaryExpression(UnaryExpression expression, RealIntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		if (arg.isBottom())
			return RealIntervalLattice.BOTTOM;
		// Numeric negation flips the sign of the interval and swaps bounds
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
			// Addition: add lower bounds and upper bounds respectively
			return new RealIntervalLattice(left.getLow() + right.getLow(), left.getHigh() + right.getHigh());
		if (op instanceof SubtractionOperator)
			// Subtraction: left - right -> low = left.low - right.high,
			// high = left.high - right.low (conservative bounds)
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
			// Equality: if intervals are disjoint -> definitely false.
			// If both are exact singletons with same value -> definitely true.
			// Otherwise unknown.
			if (left.getHigh() < right.getLow() || right.getHigh() < left.getLow())
				return Satisfiability.NOT_SATISFIED;
			if (left.getLow() == left.getHigh() && left.getLow() == right.getLow() && right.getLow() == right.getHigh())
				return Satisfiability.SATISFIED;
			return Satisfiability.UNKNOWN;
		}
		if (op == ComparisonNe.INSTANCE) {
			// Not-equals is the negation of equals. Compute equals then negate.
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
			// left > right: definitely true when left.low > right.high,
			// definitely false when left.high <= right.low
			return compare(left.getLow() > right.getHigh(), left.getHigh() <= right.getLow());
		if (op == ComparisonGe.INSTANCE)
			// left >= right: similar logic but using >= and <
			return compare(left.getLow() >= right.getHigh(), left.getHigh() < right.getLow());
		if (op == ComparisonLt.INSTANCE)
			// left < right: definitely true when left.high < right.low,
			// definitely false when left.low >= right.high
			return compare(left.getHigh() < right.getLow(), left.getLow() >= right.getHigh());
		if (op == ComparisonLe.INSTANCE)
			// left <= right: analogous to >= case
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
		// Multiplication of intervals requires considering all combinations
		// of bounds because sign changes can move extremes to different products.
		// If either side is exactly zero, result is exact zero.
		if (left.isZero() || right.isZero())
			return RealIntervalLattice.ZERO;
		double a = multiplyBound(left.getLow(), right.getLow());
		double b = multiplyBound(left.getLow(), right.getHigh());
		double c = multiplyBound(left.getHigh(), right.getLow());
		double d = multiplyBound(left.getHigh(), right.getHigh());
		// The resulting interval is [min(all products), max(all products)]
		return new RealIntervalLattice(min(a, b, c, d), max(a, b, c, d));
	}

	private static RealIntervalLattice divide(RealIntervalLattice left, RealIntervalLattice right) {
		// Division must handle division by zero carefully.
		// If divisor is exactly zero -> inconsistent (BOTTOM).
		// If divisor interval contains zero -> result can be unbounded -> TOP.
		// Otherwise, compute division as multiplication by reciprocal interval.
		if (right.isZero())
			return RealIntervalLattice.BOTTOM;
		if (right.containsZero())
			return RealIntervalLattice.TOP;
		RealIntervalLattice reciprocal = new RealIntervalLattice(1.0 / right.getHigh(), 1.0 / right.getLow());
		return multiply(left, reciprocal);
	}

	private static RealIntervalLattice remainder(RealIntervalLattice left, RealIntervalLattice right) {
		// Remainder (mod) semantics: result magnitude is bounded by the maximum
		// absolute value of the divisor bounds. Handle edge cases conservatively:
		// - divisor zero -> BOTTOM
		// - left zero -> exact zero
		// - divisor containing zero or unbounded -> TOP (cannot safely bound)
		if (right.isZero())
			return RealIntervalLattice.BOTTOM;
		if (left.isZero())
			return RealIntervalLattice.ZERO;
		if (right.containsZero() || Double.isInfinite(right.getLow()) || Double.isInfinite(right.getHigh()))
			return RealIntervalLattice.TOP;
		double bound = Math.max(Math.abs(right.getLow()), Math.abs(right.getHigh()));
		// Remainder lies in [-bound, bound]
		return new RealIntervalLattice(-bound, bound);
	}

	private static double multiplyBound(double left, double right) {
		// Small helper: if one operand is exact zero, return zero to avoid
		// producing -0.0 or inf/nan artifacts in boundary calculations.
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
