package it.unive.scsr.analysis.interval;

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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;

public class Interval implements BaseNonRelationalValueDomain<IntervalLattice> {

	@Override
	public IntervalLattice top() {
		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice bottom() {
		return IntervalLattice.BOTTOM;
	}

	@Override
	public IntervalLattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if (constant.getValue() instanceof Integer) {
			//I need to check the integer value to 
			// assign the right approx value
			Integer n = (Integer) constant.getValue();

			return new IntervalLattice(n, n);
		}

		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice evalUnaryExpression(
			UnaryExpression expression, IntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (arg.i == null)
			return IntervalLattice.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.i.getHigh();
			MathNumber l = arg.i.getLow();

			return new IntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
		}

		return IntervalLattice.TOP;
	}

	private IntInterval negateInterval(IntInterval i) {
		if (i == null || i.isBottom())
			return i;
		MathNumber l = i.getLow();
		MathNumber u = i.getHigh();
		return new IntInterval(
				l.multiply(new MathNumber(-1))
						.min(u.multiply(new MathNumber(-1))),
				l.multiply(new MathNumber(-1))
						.min(u.multiply(new MathNumber(-1)))
		);
	}

	@Override
	public IntervalLattice evalBinaryExpression(
			BinaryExpression expression, IntervalLattice left,
			IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (left.i == null || right == null)
			return IntervalLattice.BOTTOM;

		MathNumber u1 = left.i.getHigh();
		MathNumber u2 = right.i.getHigh();

		MathNumber l1 = left.i.getLow();
		MathNumber l2 = right.i.getLow();

		if (expression.getOperator() instanceof AdditionOperator) {
			return new IntervalLattice(l1.add(l2), u1.add(u2));
			//return new IntervalLattice(left.i.plus(right.i));
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			return new IntervalLattice(
					l1.multiply(l2)
							.min(l1.multiply(u2))
							.min(u1.multiply(l2))
							.min(u1.multiply(u2)),
					l1.multiply(l2)
							.max(l1.multiply(u2))
							.max(u1.multiply(l2))
							.max(u1.multiply(u2))
			);
			//return new IntervalLattice(left.i.mul(right.i));
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			IntInterval neg = negateInterval(right.i);
			return new IntervalLattice(
					l1.add(neg.getLow()),
					u1.add(neg.getHigh())
			);
			//return new IntervalLattice(left.i.diff(right.i));
		} else if (expression.getOperator() instanceof DivisionOperator) {
			/*try {
				return new IntervalLattice(left.i.div(right.i, false, true));
			} catch (ArithmeticException e) {
				return IntervalLattice.BOTTOM;
			}*/
			if (right.lessOrEqual(IntervalLattice.ZERO))
				return IntervalLattice.BOTTOM;
			return new IntervalLattice(
					l1.divide(l2)
							.min(l1.divide(u2))
							.min(u1.divide(l2))
							.min(u1.divide(u2))
							.roundDown(),
					l1.divide(l2)
							.max(l1.divide(u2))
							.max(u1.divide(l2))
							.max(u1.divide(u2))
							.roundUp()
			);
		}

		return IntervalLattice.TOP;
	}


}
