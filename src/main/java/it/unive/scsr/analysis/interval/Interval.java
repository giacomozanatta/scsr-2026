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

//Work done by Elia Stevanato 895598 and Francesco Pasqualato 897778
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

		MathNumber u1 = left.i.getHigh();
		MathNumber u2 = right.i.getHigh();

		MathNumber l1 = left.i.getLow();
		MathNumber l2 = right.i.getLow();
		
		if(expression.getOperator() instanceof AdditionOperator) {
			return new IntervalLattice(l1.add(l2), u1.add(u2));
			
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			MathNumber x1 = l1.multiply(l2);
			MathNumber x2 = l1.multiply(u2);
			MathNumber x3 = u1.multiply(l2);
			MathNumber x4 = u1.multiply(u2);

			MathNumber min = x1.min(x2).min(x3).min(x4);
			MathNumber max = x1.max(x2).max(x3).max(x4);

			return new IntervalLattice(min, max);

		} else if (expression.getOperator() instanceof SubtractionOperator) {
			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));

		} else if (expression.getOperator() instanceof DivisionOperator) {
			if (right.i.includes(new IntInterval(MathNumber.ZERO, MathNumber.ZERO))) {
				return IntervalLattice.TOP;
			}

			MathNumber x1 = l1.divide(l2);
			MathNumber x2 = l1.divide(u2);
			MathNumber x3 = u1.divide(l2);
			MathNumber x4 = u1.divide(u2);

			MathNumber min = x1.min(x2).min(x3).min(x4);
			MathNumber max = x1.max(x2).max(x3).max(x4);

			return new IntervalLattice(min, max);
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
