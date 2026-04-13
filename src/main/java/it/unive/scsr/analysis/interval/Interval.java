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

import java.util.ArrayList;
import java.util.List;

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
		
		if(left.i == null || right == null)
			return IntervalLattice.BOTTOM;
		MathNumber ll = left.i.getLow();
		MathNumber lu = left.i.getHigh();

		MathNumber rl = right.i.getLow();
		MathNumber ru = right.i.getHigh();

		if(expression.getOperator() instanceof AdditionOperator) {
			return new IntervalLattice(ll.add(rl), lu.add(ru));

		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			List<MathNumber> mults = new ArrayList<>(
					List.of(
							ll.multiply(rl),
							ll.multiply(ru),
							lu.multiply(rl),
							lu.multiply(ru)
					)
			);
			MathNumber lo = mults.get(0), hi = mults.get(0);
			for(MathNumber mul : mults) {
				if (lo.compareTo(mul) > 0) lo = mul;
				if (hi.compareTo(mul) < 0) hi = mul;
			}
			return new IntervalLattice(lo, hi);

		} else if (expression.getOperator() instanceof SubtractionOperator) {
			MathNumber lo = ll.add(
					ru.multiply(MathNumber.MINUS_ONE)
			);
			MathNumber hi = lu.add(
					rl.multiply(MathNumber.MINUS_ONE)
			);

			return new IntervalLattice(lo, hi);

		} else if (expression.getOperator() instanceof DivisionOperator) {
			if(rl.compareTo(MathNumber.ZERO) <= 0 && ru.compareTo(MathNumber.ZERO) >= 0){
				return IntervalLattice.BOTTOM;
			}

			List<MathNumber> divs = new ArrayList<>(
					List.of(
							ll.divide(rl),
							ll.divide(ru),
							lu.divide(rl),
							lu.divide(ru)
					)
			);
			MathNumber min = divs.get(0), max = divs.get(0);
			for(MathNumber div : divs) {
				if (min.compareTo(div) > 0) min = div;
				if (max.compareTo(div) < 0) max = div;
			}

			// SHould we round the extremities since we are dealing with integers only?
			// Should we perhaps return bottom when dividing by a non-integer?
//			return new IntervalLattice(min, max);
			return new IntervalLattice(min.roundDown(), max.roundUp());

		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
