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

		MathNumber u1 = left.i.getHigh();
		MathNumber u2 = right.i.getHigh();
		MathNumber l1 = left.i.getLow();
		MathNumber l2 = right.i.getLow();
		
		if(expression.getOperator() instanceof AdditionOperator) {
			return new IntervalLattice(l1.add(l2), u1.add(u2));
			
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			MathNumber p1 = l1.multiply(l2);
			MathNumber p2 = l1.multiply(u2);
			MathNumber p3 = u1.multiply(l2);
			MathNumber p4 = u1.multiply(u2);

			MathNumber min = p1;
			if (p2.leq(min))
				min = p2;
			if (p3.leq(min))
				min = p3;
			if (p4.leq(min))
				min = p4;

			MathNumber max = p1;
			if (p2.geq(max))
				max = p2;
			if (p3.geq(max))
				max = p3;
			if (p4.geq(max))
				max = p4;

			return new IntervalLattice(min, max);
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));
		} else if (expression.getOperator() instanceof DivisionOperator) {
			MathNumber zero = MathNumber.ZERO;
			boolean divisorMayBeZero = l2.leq(zero) && u2.geq(zero);
			if (divisorMayBeZero)
				return IntervalLattice.TOP;

			MathNumber d1 = l1.divide(l2);
			MathNumber d2 = l1.divide(u2);
			MathNumber d3 = u1.divide(l2);
			MathNumber d4 = u1.divide(u2);

			MathNumber min = d1;
			if (d2.leq(min))
				min = d2;
			if (d3.leq(min))
				min = d3;
			if (d4.leq(min))
				min = d4;

			MathNumber max = d1;
			if (d2.geq(max))
				max = d2;
			if (d3.geq(max))
				max = d3;
			if (d4.geq(max))
				max = d4;

			return new IntervalLattice(min, max);
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}