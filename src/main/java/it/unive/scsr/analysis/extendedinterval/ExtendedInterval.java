package it.unive.scsr.analysis.extendedinterval;

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
import java.util.ArrayList;
import java.util.List;

public class ExtendedInterval implements BaseNonRelationalValueDomain<ExtendedIntervalLattice> {

	@Override
	public ExtendedIntervalLattice top() {
		return ExtendedIntervalLattice.TOP;
	}

	@Override
	public ExtendedIntervalLattice bottom() {
		return ExtendedIntervalLattice.BOTTOM;
	}

	@Override
	public ExtendedIntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		if (constant.getValue() instanceof Number i) {
			MathNumber n = new MathNumber(new BigDecimal(i.toString()));
            return new ExtendedIntervalLattice(n, n, false);
		}

		return ExtendedIntervalLattice.TOP;
	}

	@Override
	public ExtendedIntervalLattice evalUnaryExpression(UnaryExpression expression, ExtendedIntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (arg.bot)
			return ExtendedIntervalLattice.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.u;
			MathNumber l = arg.l;

			return new ExtendedIntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE), false);
		}

		return ExtendedIntervalLattice.TOP;
	}

	@Override
	public ExtendedIntervalLattice evalBinaryExpression(BinaryExpression expression, ExtendedIntervalLattice left,
			ExtendedIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		
		if(left.bot || right.bot)
			return ExtendedIntervalLattice.BOTTOM;
		
		MathNumber l1 = left.l;
		MathNumber l2 = right.l;
		
		MathNumber u1 = left.u;
		MathNumber u2 = right.u;
		
		if(expression.getOperator() instanceof AdditionOperator) {
			MathNumber pl = l1.add(l2);
			MathNumber ph = u1.add(u2);
			if (pl.isNaN() || ph.isNaN()) {
				return ExtendedIntervalLattice.TOP;
			}
			return new ExtendedIntervalLattice(pl,ph, false);
		}
		else if (expression.getOperator() instanceof MultiplicationOperator) {
			return mul(left, right);
		}
		else if (expression.getOperator() instanceof SubtractionOperator) {
			return new ExtendedIntervalLattice(l1.subtract(u2), u1.subtract(l2), false);
		}
		else if (expression.getOperator() instanceof DivisionOperator) {
			if (left.equals(ExtendedIntervalLattice.ZERO))
				return ExtendedIntervalLattice.ZERO;
			if (right.equals(ExtendedIntervalLattice.ZERO))
				return ExtendedIntervalLattice.TOP;

			if (includes(right, ExtendedIntervalLattice.ZERO)) {
				if (l2.compareTo(new MathNumber(0)) < 0 && u2.compareTo(new MathNumber(0)) > 0) {
					return ExtendedIntervalLattice.TOP;
				}
				MathNumber potential_upper;
				MathNumber potential_lower;
				MathNumber ul = u1.divide(l2);
				MathNumber uu = u1.divide(u2);
				MathNumber ll = l1.divide(l2);
				MathNumber lu = l1.divide(u2);
				List<MathNumber> maxes = new ArrayList<>();
				List<MathNumber> mins = new ArrayList<>();
				if (!ul.isNaN()) {
					maxes.add(ul);
					mins.add(ul);
				}
				if (!uu.isNaN()) {
					maxes.add(uu);
					mins.add(uu);
				}
				if (!lu.isNaN()) {
					maxes.add(lu);
					mins.add(lu);
				}
				if (!ll.isNaN()) {
					maxes.add(ll);
					mins.add(ll);
				}
				potential_upper = max(maxes);
				potential_lower = min(mins);
				if (l2.isZero()) {
					if (l1.isPositive() && u1.isPositive()) {
						potential_upper = MathNumber.PLUS_INFINITY;
					}
					else if (l1.isNegative() && u1.isNegative()) {
						potential_lower = MathNumber.MINUS_INFINITY;
					}
					else {
						potential_upper = MathNumber.PLUS_INFINITY;
						potential_lower = MathNumber.MINUS_INFINITY;
					}
				}
				if (u2.isZero()) {
					if (l1.isPositive() && u1.isPositive()) {
						potential_lower = MathNumber.MINUS_INFINITY;
					}
					else if (l1.isNegative() && u1.isNegative()) {
						potential_upper = MathNumber.PLUS_INFINITY;
					}
					else {
						potential_upper = MathNumber.PLUS_INFINITY;
						potential_lower = MathNumber.MINUS_INFINITY;
					}
				}
				return new ExtendedIntervalLattice(potential_lower,potential_upper,false);
			}
			else {
				MathNumber ll = l1.divide(l2);
				MathNumber lu = l1.divide(u2);
				MathNumber ul = u1.divide(l2);
				MathNumber uu = u1.divide(u2);
				return new ExtendedIntervalLattice(min(ll,lu,ul,uu), max(ll,lu,ul,uu), false);
			}
		}
		return ExtendedIntervalLattice.TOP;
	}

	private ExtendedIntervalLattice mul(ExtendedIntervalLattice left, ExtendedIntervalLattice right) {
		MathNumber l1 = left.l;
		MathNumber l2 = right.l;
		
		MathNumber u1 = left.u;
		MathNumber u2 = right.u;
		
		if (left.equals(ExtendedIntervalLattice.ZERO) || right.equals(ExtendedIntervalLattice.ZERO))
			return ExtendedIntervalLattice.ZERO;
		else {
			MathNumber ll = l1.multiply(l2);
			MathNumber lu = l1.multiply(u2);
			MathNumber ul = u1.multiply(l2);
			MathNumber uu = u1.multiply(u2);
			if (ll.isNaN() || lu.isNaN() || ul.isNaN() || uu.isNaN()) {
				return ExtendedIntervalLattice.TOP;
			}
			return new ExtendedIntervalLattice(min(ll,lu,ul,uu), max(ll,lu,ul,uu), false);
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

	private static MathNumber min(List<MathNumber> nums) {
		if (nums.isEmpty())
			throw new IllegalArgumentException("No numbers provided");

		MathNumber min = nums.get(0);
		for (int i = 1; i < nums.size(); i++)
			min = min.min(nums.get(i));

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

	private static MathNumber max(List<MathNumber> nums) {
		if (nums.isEmpty())
			throw new IllegalArgumentException("No numbers provided");

		MathNumber max = nums.get(0);
		for (int i = 1; i < nums.size(); i++)
			max = max.max(nums.get(i));

		return max;
	}
	
	public boolean includes(ExtendedIntervalLattice a,
			ExtendedIntervalLattice b) {
		if (a.isBottom() || b.isBottom())
			return false;
		return a.l.compareTo(b.l) <= 0 && a.u.compareTo(b.u) >= 0;
	}

}
