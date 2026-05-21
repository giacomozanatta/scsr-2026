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

public class IntervalRealAlternative implements BaseNonRelationalValueDomain<IntervaRealLatticeAlternative> {

	@Override
	public IntervaRealLatticeAlternative top() {
		return IntervaRealLatticeAlternative.TOP;
	}

	@Override
	public IntervaRealLatticeAlternative bottom() {
		return IntervaRealLatticeAlternative.BOTTOM;
	}

	@Override
	public IntervaRealLatticeAlternative evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if (constant.getValue() instanceof Integer) {
			// I need to check the integer value to
			// assign the right approx value
			Integer n = (Integer) constant.getValue();

			return new IntervaRealLatticeAlternative(n, n);
		}

        if (constant.getValue() instanceof Double) {
            Double n = (Double) constant.getValue();
            MathNumber m = new MathNumber(n);
            return new IntervaRealLatticeAlternative(m, m);
        }
        if (constant.getValue() instanceof Float) {
            Float n = (Float) constant.getValue();
            MathNumber m = new MathNumber(n);
            return new IntervaRealLatticeAlternative(m, m);
        }
		return IntervaRealLatticeAlternative.TOP;
	}

	@Override
	public IntervaRealLatticeAlternative evalUnaryExpression(UnaryExpression expression, IntervaRealLatticeAlternative arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (arg.i == null)
			return IntervaRealLatticeAlternative.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.i.getHigh();
			MathNumber l = arg.i.getLow();

			return new IntervaRealLatticeAlternative(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
		}

		return IntervaRealLatticeAlternative.TOP;
	}

	@Override
	public IntervaRealLatticeAlternative evalBinaryExpression(BinaryExpression expression, IntervaRealLatticeAlternative left,
			IntervaRealLatticeAlternative right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if(left.i == null || right.i == null)
			return IntervaRealLatticeAlternative.BOTTOM;

		if (expression.getOperator() instanceof AdditionOperator) {

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			return new IntervaRealLatticeAlternative(l1.add(l2), u1.add(u2));

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

			return new IntervaRealLatticeAlternative(p1.min(p2).min(p3).min(p4), p1.max(p2).max(p3).max(p4));
			// TODO: homework
		} else if (expression.getOperator() instanceof SubtractionOperator) {

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			return new IntervaRealLatticeAlternative(l1.subtract(u2), u1.subtract(l2));

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
			return new IntervaRealLatticeAlternative(p1.min(p2).min(p3).min(p4),p1.max(p2).max(p3).max(p4));
		}

		return IntervaRealLatticeAlternative.TOP;
	}

}
