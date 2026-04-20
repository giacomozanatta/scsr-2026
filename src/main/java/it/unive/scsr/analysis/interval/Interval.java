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

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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

	private MathNumber mux(MathNumber a, MathNumber b) {
		if (a.isZero() || b.isZero()) return MathNumber.ZERO;
		return a.multiply(b);
	}

	private <T> T fold(T[] nums, BiFunction<T, T, T> func) {
		T res = nums[0];

		for (int i = 1; i < nums.length; i++) {
			res = func.apply(res, nums[i]);
		}

		return res;
	}

	private IntervalLattice division(MathNumber a, MathNumber b, MathNumber c, MathNumber d) throws SemanticException {
		if (c == MathNumber.ZERO || b == MathNumber.ZERO)
			return IntervalLattice.BOTTOM;

		if (c.leq(MathNumber.ZERO) && d.geq(MathNumber.ZERO)) {
			IntervalLattice neg = c.leq(new MathNumber(-1)) ? division(a, b, c, new MathNumber(-1)) : IntervalLattice.BOTTOM;
			IntervalLattice pos = d.geq(new MathNumber( 1)) ? division(a, b, new MathNumber( 1), d) : IntervalLattice.BOTTOM;

			return neg.lub(pos);

		} else {
			var divs = new MathNumber[]{a.divide(c), a.divide(d), b.divide(c), b.divide(d)};
			MathNumber min = fold(divs, MathNumber::min);
			MathNumber max = fold(divs, MathNumber::max);

			return new IntervalLattice(min, max);
		}
	}

	@Override
	public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left,
			IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		
		if(left.i == null || right == null)
			return IntervalLattice.BOTTOM;
		if(left.isBottom() || right.isBottom())
			return IntervalLattice.BOTTOM;

		MathNumber a = left.i.getLow();
		MathNumber b = left.i.getHigh();

		MathNumber c = right.i.getLow();
		MathNumber d = right.i.getHigh();

		if(expression.getOperator() instanceof AdditionOperator) {
			if (a.add(c).isNaN() || b.add(d).isNaN())
				return IntervalLattice.BOTTOM;

			return new IntervalLattice(a.add(c), b.add(d));

		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			var muxes = new MathNumber[]{mux(a, c), mux(a, d), mux(b, c), mux(b, d)};
			MathNumber min = fold(muxes, MathNumber::min);
			MathNumber max = fold(muxes, MathNumber::max);

			return new IntervalLattice(min, max);

		} else if (expression.getOperator() instanceof SubtractionOperator) {
			if (a.subtract(d).isNaN() || b.subtract(c).isNaN())
				return IntervalLattice.BOTTOM;
			
			return new IntervalLattice(a.subtract(d), b.subtract(c));

		} else if (expression.getOperator() instanceof DivisionOperator) {
			return division(a, b, c, d);
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
