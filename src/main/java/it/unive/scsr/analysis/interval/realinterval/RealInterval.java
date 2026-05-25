package it.unive.scsr.analysis.interval.realinterval;

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
import it.unive.lisa.symbolic.value.operator.unary.NumericAbs;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;


// Almost identical to the integer Interval domain
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
	public RealIntervalLattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if (constant.getValue() instanceof Number) {
			Number n = (Number) constant.getValue();
			return new RealIntervalLattice(n, n);
		}

		return RealIntervalLattice.TOP;
	}

	private RealIntervalLattice negateRealInterval(RealIntervalLattice arg) {
		if (arg.low == null || arg.high == null)
			return RealIntervalLattice.BOTTOM;

		return new RealIntervalLattice(
				arg.high.multiply(MathNumber.MINUS_ONE),
				arg.low.multiply(MathNumber.MINUS_ONE)
		);
	}

	@Override
	public RealIntervalLattice evalUnaryExpression(
			UnaryExpression expression, RealIntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (arg.low == null || arg.high == null)
			return RealIntervalLattice.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			return negateRealInterval(arg);
		}

		if (expression.getOperator() == NumericAbs.INSTANCE) {
			if (arg.low.isPositive() && arg.high.isPositive()) {
				return arg;
			} else if (arg.low.isNegative() && arg.high.isNegative()) {
				return negateRealInterval(arg);
			} else {
				// [Low < 0 , High > 0]
				MathNumber t = arg.low.multiply(MathNumber.MINUS_ONE);
				return new RealIntervalLattice(
						MathNumber.ZERO,
						arg.high.max(t)
				);
			}
		}

		return RealIntervalLattice.TOP;
	}

	@Override
	public RealIntervalLattice evalBinaryExpression(
			BinaryExpression expression, RealIntervalLattice left,
			RealIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (left.low == null || left.high == null || right.low == null || right.high == null)
			return RealIntervalLattice.BOTTOM;

		MathNumber l1 = left.low;
		MathNumber u1 = left.high;

		MathNumber l2 = right.low;
		MathNumber u2 = right.high;

		if (expression.getOperator() instanceof AdditionOperator) {
			return new RealIntervalLattice(l1.add(l2), u1.add(u2));
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			return new RealIntervalLattice(
					l1.multiply(l2)
							.min(l1.multiply(u2))
							.min(u1.multiply(l2))
							.min(u1.multiply(u2)),
					l1.multiply(l2)
							.max(l1.multiply(u2))
							.max(u1.multiply(l2))
							.max(u1.multiply(u2))
			);
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			RealIntervalLattice neg = negateRealInterval(right);
			return new RealIntervalLattice(
					l1.add(neg.low),
					u1.add(neg.high)
			);
		} else if (expression.getOperator() instanceof DivisionOperator) {
			if (RealIntervalLattice.ZERO.lessOrEqual(right))
				return RealIntervalLattice.BOTTOM;
			return new RealIntervalLattice(
					l1.divide(l2)
							.min(l1.divide(u2))
							.min(u1.divide(l2))
							.min(u1.divide(u2)),
					l1.divide(l2)
							.max(l1.divide(u2))
							.max(u1.divide(l2))
							.max(u1.divide(u2))
			);
		}

		return RealIntervalLattice.TOP;
	}


}
