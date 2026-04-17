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
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.IntInterval;
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
		
		BinaryOperator operator = expression.getOperator();
		IntInterval a = left.i;
		IntInterval b = right.i;
		MathNumber l1 = a.getLow();
		MathNumber l2 = b.getLow();
		MathNumber u1 = a.getHigh();
		MathNumber u2 = b.getHigh();

		if(operator instanceof AdditionOperator) {
			// [l1, u1] + [l2, u2] becomes [l1 + l2, u1 + u2]
			return new IntervalLattice(l1.add(l2), u1.add(u2));
		} 
		else if (operator instanceof SubtractionOperator){
			// [l1, u1] - [l2, u2] becomes [l1 - u2, u1 - l2]
			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));
			
		} else if (operator instanceof MultiplicationOperator){
			// Compute all cross products
			MathNumber p1 = l1.multiply(l2);
			MathNumber p2 = l1.multiply(u2);
			MathNumber p3 = u1.multiply(l2);
			MathNumber p4 = u1.multiply(u2);

			// The new lower bound is the minimum among the 4 products
			MathNumber newLower = p1.min(p2).min(p3).min(p4);
			// The new upper bound is the maximum among the 4 products
			MathNumber newUpper = p1.max(p2).max(p3).max(p4);

			// [l1, u1] * [l2, u2] becomes [min(p1, p2, p3, p4), max(p1, p2, p3, p4)]
			return new IntervalLattice(newLower, newUpper);
			
		} else if (operator instanceof DivisionOperator) {
			// Prevent division by zero
			// if the interval of the divisor includes zero (l2 <= 0 && u2 >= 0), we return TOP due to uncertainty/error
			if (l2.leq(MathNumber.ZERO) && u2.geq(MathNumber.ZERO)) {
				return top();
			}

			// Compute all cross divisions
			MathNumber p1 = l1.divide(l2);
			MathNumber p2 = l1.divide(u2);
			MathNumber p3 = u1.divide(l2);
			MathNumber p4 = u1.divide(u2);

			// The new lower bound is the minimum among the 4 divisions
			MathNumber newLower = p1.min(p2).min(p3).min(p4);
			// The new upper bound is the maximum among the 4 divisions
			MathNumber newUpper = p1.max(p2).max(p3).max(p4);

			// [l1, u1] / [l2, u2] becomes [min(p1, p2, p3, p4), max(p1, p2, p3, p4)]
			return new IntervalLattice(newLower.roundDown(), newUpper.roundUp());
		}

		return top();
	}

	
	
	
	
}
