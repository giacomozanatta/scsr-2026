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

		if (constant.getValue() instanceof Integer) {
			// I need to check the integer value to
			// assign the right approx value
			Integer n = (Integer) constant.getValue();

			return new IntervalLattice(n, n);
		}

		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice evalUnaryExpression(UnaryExpression expression, IntervalLattice arg, ProgramPoint pp,
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

	@Override
	public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left,
			IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (left.i == null || right == null)
			return IntervalLattice.BOTTOM;

		if (expression.getOperator() instanceof AdditionOperator) {

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			return new IntervalLattice(l1.add(l2), u1.add(u2));

		} else if (expression.getOperator() instanceof MultiplicationOperator) {

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			// we have to find all the products between the bounds of the two intervals
			MathNumber p1 = l1.multiply(l2);
			MathNumber p2 = l1.multiply(u2);
			MathNumber p3 = u1.multiply(l2);
			MathNumber p4 = u1.multiply(u2);
			// the new interval will be the one between the min and the max of these

			return new IntervalLattice(p1.min(p2).min(p3).min(p4), p1.max(p2).max(p3).max(p4));
			// TODO: homework
		} else if (expression.getOperator() instanceof SubtractionOperator) {

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));

		} else if (expression.getOperator() instanceof DivisionOperator) {
			// TODO: homework
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			// if 0 ∈ [l2, u2] → top
			if (l2.leq(MathNumber.ZERO) && MathNumber.ZERO.leq(u2))
				return top();
			//else we find the min and the max of the divisions between the bounds of the two intervals
			//i found this information on the wikipedia page of interva arithmetic
			MathNumber p1 = l1.divide(l2);
			MathNumber p2 = l1.divide(u2);
			MathNumber p3 = u1.divide(l2);
			MathNumber p4 = u1.divide(u2);
			//[l1, u1] / [l2, u2] =  [l1,u1] * [1/u2, 1/l2] 
			//we round down the lower bound and round up the upper bound to be sure to include all the possible values of the division
			return new IntervalLattice(p1.min(p2).min(p3).min(p4).roundDown(),p1.max(p2).max(p3).max(p4).roundUp());
		}

		return IntervalLattice.TOP;
	}

}
