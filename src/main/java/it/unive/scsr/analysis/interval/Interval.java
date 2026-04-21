package it.unive.scsr.analysis.interval;

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
	public IntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if (constant.getValue() instanceof Integer n) {
			// I need to check the integer value to
			// assign the right approx value
			return new IntervalLattice(n, n);
		}

		return top();
	}

	@Override
	public IntervalLattice evalUnaryExpression(UnaryExpression expression, IntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (arg.i == null)
			return bottom();

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.i.getHigh();
			MathNumber l = arg.i.getLow();

			return new IntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
		}

		return top();
	}

	@Override
	public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left,
			IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (left.i == null || right.i == null)
			return bottom();

		MathNumber upperLeft = left.i.getHigh();
		MathNumber upperRight = right.i.getHigh();

		MathNumber lowerLeft = left.i.getLow();
		MathNumber lowerRight = right.i.getLow();

		BinaryOperator op = expression.getOperator();

		if (op instanceof AdditionOperator) {
			return new IntervalLattice(
					lowerLeft.add(lowerRight),
					upperLeft.add(upperRight));

		} else if (op instanceof MultiplicationOperator) {
			if (left.equals(IntervalLattice.ZERO) || right.equals(IntervalLattice.ZERO))
				return IntervalLattice.ZERO;

			List<MathNumber> bounds = Arrays.asList(
					lowerLeft.multiply(lowerRight),
					lowerLeft.multiply(upperRight),
					upperLeft.multiply(lowerRight),
					upperLeft.multiply(upperRight));

			return new IntervalLattice(
					Collections.min(bounds),
					Collections.max(bounds));

		} else if (op instanceof SubtractionOperator) {
			return new IntervalLattice(
					lowerLeft.subtract(upperRight),
					upperLeft.subtract(lowerRight));

		} else if (op instanceof DivisionOperator) {
			if (right.i.includes(IntInterval.ZERO))
				return (right.equals(IntervalLattice.ZERO)) ? bottom() : top();

			if (left.equals(IntervalLattice.ZERO))
				return IntervalLattice.ZERO;

			List<MathNumber> bounds = Arrays.asList(
					lowerLeft.divide(lowerRight),
					lowerLeft.divide(upperRight),
					upperLeft.divide(lowerRight),
					upperLeft.divide(upperRight));

			return new IntervalLattice(
					Collections.min(bounds).roundDown(),
					Collections.max(bounds).roundUp());
		}

		return top();
	}

}
