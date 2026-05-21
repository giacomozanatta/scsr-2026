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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;

import java.math.BigDecimal;
import java.math.BigInteger;

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

		if (constant.getValue() instanceof Number n) {
			MathNumber m = numberToMathNumber(n);
			return new RealIntervalLattice(m,m);
		}

		return RealIntervalLattice.TOP;
	}

	@Override
	public RealIntervalLattice evalUnaryExpression(UnaryExpression expression, RealIntervalLattice arg, ProgramPoint pp,
												   SemanticOracle oracle) throws SemanticException {

		if (arg.getLow() == null || arg.getHigh() == null)
			return RealIntervalLattice.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.getHigh();
			MathNumber l = arg.getLow();

			return new RealIntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE));
		}

		return RealIntervalLattice.TOP;
	}

	@Override
	public RealIntervalLattice evalBinaryExpression(BinaryExpression expression, RealIntervalLattice left,
													RealIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		
		if(left.getLow() == null || left.getHigh() == null || right.getLow() == null || right.getHigh() == null)
			return RealIntervalLattice.BOTTOM;
		
		MathNumber l1 = left.getLow();
		MathNumber l2 = right.getLow();
		
		MathNumber u1 = left.getHigh();
		MathNumber u2 = right.getHigh();
		
		if(expression.getOperator() instanceof AdditionOperator) {	
			return new RealIntervalLattice(l1.add(l2), u1.add(u2));
		}
		else if (expression.getOperator() instanceof MultiplicationOperator) {
			return mul(left, right);
		}
		else if (expression.getOperator() instanceof SubtractionOperator) {
			return new RealIntervalLattice(l1.subtract(u2), u1.subtract(l2));
		}
		else if (expression.getOperator() instanceof DivisionOperator) {
			if (left.equals(RealIntervalLattice.ZERO)) // |0,0] divided -> return ZERO [0,0]
				return RealIntervalLattice.ZERO;

			if (right.equals(RealIntervalLattice.ZERO)) // division by [0,0] -> return TOP ([-inf, +inf])
				return RealIntervalLattice.TOP;

			if (!includes(right, RealIntervalLattice.ZERO))
				return mul(left, new RealIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.ONE.divide(l2)));

			else if (u2.isZero())
				return mul(left, new RealIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2)));

			else if (l2.isZero())
				return mul(left, new RealIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY));

			else {
				RealIntervalLattice lower = mul(left, new RealIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2)));
				RealIntervalLattice higher = mul(left, new RealIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY));

				if (includes(lower, higher))
					return lower;
				else if (includes(higher, lower))
					return higher;
				else {
					MathNumber l = lower.getLow().compareTo(higher.getLow()) > 0 ? higher.getLow() : lower.getLow();
					MathNumber u = lower.getHigh().compareTo(higher.getHigh()) < 0 ? higher.getHigh() : lower.getHigh();
					return new RealIntervalLattice(l, u);
				}
			}
		}
		
		return RealIntervalLattice.TOP;
	}

	private RealIntervalLattice mul(RealIntervalLattice left, RealIntervalLattice right) {
		MathNumber l1 = left.getLow();
		MathNumber l2 = right.getLow();
		
		MathNumber u1 = left.getHigh();
		MathNumber u2 = right.getHigh();
		
		if (left.equals(RealIntervalLattice.ZERO) || right.equals(RealIntervalLattice.ZERO))
			return RealIntervalLattice.ZERO;
		else {
			if (l1.compareTo(MathNumber.ZERO) >= 0 && l2.compareTo(MathNumber.ZERO) >= 0)
				return new RealIntervalLattice(u1.multiply(u2), l1.multiply(l2));
			
			MathNumber ll = l1.multiply(l2);
			MathNumber lh = l1.multiply(u2);
			MathNumber hl = u1.multiply(l2);
			MathNumber hh = u1.multiply(u2);
			
			return new RealIntervalLattice(min(ll, lh, hl, hh), max(ll, lh, hl, hh));
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
	
	public boolean includes(RealIntervalLattice a,
							RealIntervalLattice b) {
		if (a.isBottom() || b.isBottom())
			return false;
		return a.getLow().compareTo(b.getLow()) <= 0 && a.getHigh().compareTo(b.getHigh()) >= 0;
	}

	// util method to avoid doubles' floating point imprecision when parsing a concrete value
	// instead, convert directly from the number's string representation, which is always exact
	private MathNumber numberToMathNumber(Number n) {
		// Integer types are always exact so we can use longValue safely
		if (n instanceof Integer || n instanceof Long
				|| n instanceof Short || n instanceof Byte) {
			return new MathNumber(n.longValue());
		}

		// for Float and Double, go through String to avoid binary floating-point representation problems
		if (n instanceof Float) {
			// Float.toString will give back "0.5", not "0.4999..." for example
			return new MathNumber(new BigDecimal(Float.toString((Float) n)));
		}

		if (n instanceof Double) {
			return new MathNumber(new BigDecimal(Double.toString((Double) n)));
		}

		if (n instanceof BigDecimal bd) {
			return new MathNumber(bd); // MathNumber is already a wrapper for BigDecimal
		}

		if (n instanceof BigInteger bi) {
			return new MathNumber(new BigDecimal(bi));
		}

		// in case somehow the parsed number didn't match any case
		return new MathNumber(new BigDecimal(n.toString()));
	}
}
