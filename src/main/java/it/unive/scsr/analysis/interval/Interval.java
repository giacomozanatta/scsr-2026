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

public class Interval implements BaseNonRelationalValueDomain<IntervalLattice>{

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
		
		if(constant.getValue() instanceof Integer) {
			//I need to check the integer value to 
			// assign the right approx value
			Integer n = (Integer) constant.getValue();

			return new IntervalLattice(n, n);
		}
			
		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice evalUnaryExpression(UnaryExpression expression, IntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if(arg.i == null)
			return IntervalLattice.BOTTOM;
		
		if(expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.i.getHigh();
			MathNumber l = arg.i.getLow();
			
			return new IntervalLattice(u.multiply(MathNumber.MINUS_ONE),l.multiply(MathNumber.MINUS_ONE));
		}
	
		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left,
			IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		
		if(left.i == null || right.i == null)
			return IntervalLattice.BOTTOM;
		
		if(expression.getOperator() instanceof AdditionOperator) {
			
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			
			return new IntervalLattice(l1.add(l2), u1.add(u2));
			
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			// [l1, u1] - [l2, u2] = [l1 - u2, u1 - l2]
			MathNumber l1 = left.i.getLow();
			MathNumber u1 = left.i.getHigh();
			MathNumber l2 = right.i.getLow();
			MathNumber u2 = right.i.getHigh();
			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));

		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			MathNumber l1 = left.i.getLow();
			MathNumber u1 = left.i.getHigh();
			MathNumber l2 = right.i.getLow();
			MathNumber u2 = right.i.getHigh();

			// Compute all four products and take [min, max]
			MathNumber a = l1.multiply(l2);
			MathNumber b = l1.multiply(u2);
			MathNumber c = u1.multiply(l2);
			MathNumber d = u1.multiply(u2);

			return new IntervalLattice(
					a.min(b).min(c).min(d),
					a.max(b).max(c).max(d));

		} else if (expression.getOperator() instanceof DivisionOperator) {
			MathNumber l1 = left.i.getLow();
			MathNumber u1 = left.i.getHigh();
			MathNumber l2 = right.i.getLow();
			MathNumber u2 = right.i.getHigh();

			// 0 / x = 0 for any x
			if (l1.isZero() && u1.isZero())
				return IntervalLattice.ZERO;

			// x / [0,0] is undefined -> TOP (sound over-approximation)
			if (l2.isZero() && u2.isZero())
				return IntervalLattice.TOP;

			// If 0 is strictly outside [l2, u2], we can safely compute the reciprocal
			if (l2.compareTo(MathNumber.ZERO) > 0 || u2.compareTo(MathNumber.ZERO) < 0) {
				// [l1,u1] / [l2,u2] = [l1,u1] * [1/u2, 1/l2]
				MathNumber invLow = MathNumber.ONE.divide(u2);
				MathNumber invHigh = MathNumber.ONE.divide(l2);

				MathNumber a = l1.multiply(invLow);
				MathNumber b = l1.multiply(invHigh);
				MathNumber c = u1.multiply(invLow);
				MathNumber d = u1.multiply(invHigh);

				return new IntervalLattice(
						a.min(b).min(c).min(d).roundDown(),
						a.max(b).max(c).max(d).roundUp());
			}

			// 0 is inside the denominator interval -> conservative approximation
			return IntervalLattice.TOP;
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
