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
		
		if(left.i == null || right == null)
			return IntervalLattice.BOTTOM;
		
		if(expression.getOperator() instanceof AdditionOperator) {
			
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			
			return new IntervalLattice(l1.add(l2), u1.add(u2));

		} else if (expression.getOperator() instanceof MultiplicationOperator) {

			MathNumber l1 = left.i.getLow();
			MathNumber u1 = left.i.getHigh();
			MathNumber l2 = right.i.getLow();
			MathNumber u2 = right.i.getHigh();

			// All four corner products — needed because of negative numbers
			MathNumber ll = l1.multiply(l2);
			MathNumber lu = l1.multiply(u2);
			MathNumber ul = u1.multiply(l2);
			MathNumber uu = u1.multiply(u2);

			// Result is [min of all corners, max of all corners]
			MathNumber lower = ll.min(lu).min(ul).min(uu);
			MathNumber upper = ll.max(lu).max(ul).max(uu);

			return new IntervalLattice(lower, upper);

		} else if (expression.getOperator() instanceof SubtractionOperator) {

			MathNumber l1 = left.i.getLow();
			MathNumber u1 = left.i.getHigh();
			MathNumber l2 = right.i.getLow();
			MathNumber u2 = right.i.getHigh();

			// [l1,u1] - [l2,u2] = [l1-u2, u1-l2]
			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));

		} else if (expression.getOperator() instanceof DivisionOperator) {

			MathNumber l2 = right.i.getLow();
			MathNumber u2 = right.i.getHigh();

			// If 0 is inside the divisor interval, result is unknown
			MathNumber zero = new MathNumber(0);
			if (l2.leq(zero) && zero.leq(u2))
				return IntervalLattice.TOP;

			MathNumber l1 = left.i.getLow();
			MathNumber u1 = left.i.getHigh();

			// Same corner approach as multiplication
			MathNumber ll = l1.divide(l2);
			MathNumber lu = l1.divide(u2);
			MathNumber ul = u1.divide(l2);
			MathNumber uu = u1.divide(u2);

			MathNumber lower = ll.min(lu).min(ul).min(uu);
			MathNumber upper = ll.max(lu).max(ul).max(uu);

			return new IntervalLattice(lower, upper);
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
