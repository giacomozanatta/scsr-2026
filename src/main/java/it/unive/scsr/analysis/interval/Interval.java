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
		
		if(left.i == null || right.i == null)
			return IntervalLattice.BOTTOM;
		
		MathNumber l1 = left.i.getLow();
		MathNumber l2 = right.i.getLow();
		
		MathNumber u1 = left.i.getHigh();
		MathNumber u2 = right.i.getHigh();
		
		if(expression.getOperator() instanceof AdditionOperator) {	
			return new IntervalLattice(l1.add(l2), u1.add(u2));
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			return mul(left, right);
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));
		} else if (expression.getOperator() instanceof DivisionOperator) {
			if (left.equals(IntervalLattice.ZERO))
				return IntervalLattice.ZERO;
			if (right.equals(IntervalLattice.ZERO))
				return IntervalLattice.TOP;

			if (!includes(right, IntervalLattice.ZERO))
				return round(mul(left, new IntervalLattice(MathNumber.ONE.divide(u2), MathNumber.ONE.divide(l2))));
			else if (u2.isZero())
				return round(mul(left, new IntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2))));
			else if (l2.isZero())
				return round(mul(left, new IntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY)));
			else {
				IntervalLattice lower = mul(left, new IntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2)));
				IntervalLattice higher = mul(left, new IntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY));

				if (includes(lower, higher))
					return lower;
				else if (includes(higher, lower))
					return higher;
				else {
					MathNumber l = lower.i.getLow().compareTo(higher.i.getLow()) > 0 ? higher.i.getLow() : lower.i.getLow();
					MathNumber u = lower.i.getHigh().compareTo(higher.i.getHigh()) < 0 ? higher.i.getHigh() : lower.i.getHigh();
					return round(new IntervalLattice(l, u));
				}
			}
		}
		
		return IntervalLattice.TOP;
	}

	private IntervalLattice round(IntervalLattice intervalLattice) {
		if (intervalLattice.i.isBottom() || intervalLattice.i.isTop())
			return intervalLattice;
		return new IntervalLattice(intervalLattice.i.getLow().roundDown(), intervalLattice.i.getHigh().roundUp());
	}

	private IntervalLattice mul(IntervalLattice left, IntervalLattice right) {
		MathNumber l1 = left.i.getLow();
		MathNumber l2 = right.i.getLow();
		
		MathNumber u1 = left.i.getHigh();
		MathNumber u2 = right.i.getHigh();
		
		if (left.equals(IntervalLattice.ZERO) || right.equals(IntervalLattice.ZERO))
			return IntervalLattice.ZERO;
		else {
			if (l1.compareTo(MathNumber.ZERO) >= 0 && l2.compareTo(MathNumber.ZERO) >= 0)
				return new IntervalLattice(u1.multiply(u2), l1.multiply(l2));
			
			MathNumber ll = l1.multiply(l2);
			MathNumber lh = l1.multiply(u2);
			MathNumber hl = u1.multiply(l2);
			MathNumber hh = u1.multiply(u2);
			
			return new IntervalLattice(min(ll, lh, hl, hh), max(ll, lh, hl, hh));
			}	
		}

	private static MathNumber min(MathNumber... nums) {
		if (nums.length == 0)
			throw new IllegalArgumentException("No numbers provided");

		MathNumber min = nums[0];
		for (int i = 1; i < nums.length; i++)
			min = min.min(nums[i]);

		return min;
	}

	private static MathNumber max(MathNumber... nums) {
		if (nums.length == 0)
			throw new IllegalArgumentException("No numbers provided");

		MathNumber max = nums[0];
		for (int i = 1; i < nums.length; i++)
			max = max.max(nums[i]);

		return max;
	}
	
	public boolean includes(IntervalLattice a,
			IntervalLattice b) {
		if (a.isBottom() || b.isBottom())
			return false;
		return a.i.getLow().compareTo(b.i.getLow()) <= 0 && a.i.getHigh().compareTo(b.i.getHigh()) >= 0;
	}

}
