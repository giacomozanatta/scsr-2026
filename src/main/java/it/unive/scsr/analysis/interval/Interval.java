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
			// -[l,u] = [-u, -l]
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
			// [l1,u1] + [l2,u2] = [l1+l2, u1+u2]
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			
			return new IntervalLattice(l1.add(l2), u1.add(u2));
			
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			// TODO: homework
			// [l1,u1] * [l2,u2] = [min(l1*l2, l1*u2, u1*l2, u1*u2), max(l1*l2, l1*u2, u1*l2, u1*u2)]
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			MathNumber l1l2 = l1.multiply(l2);
			MathNumber l1u2 = l1.multiply(u2);
			MathNumber u1l2 = u1.multiply(l2);
			MathNumber u1u2 = u1.multiply(u2);

			return new IntervalLattice(l1l2.min(l1u2).min(u1l2).min(u1u2), l1l2.max(l1u2).max(u1l2).max(u1u2));

		} else if (expression.getOperator() instanceof SubtractionOperator) {
			// TODO: homework
			// [l1,u1] - [l2,u2] = [l1,u1] + (-[l2,u2]) = [l1,u1] + [-u2,-l2] = [l1-u2, u1-l2]
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));

		} else if (expression.getOperator() instanceof DivisionOperator) {
			// TODO: homework
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			// if 0 in [l2, u2] -> return [-inf, +inf] = TOP
			if(l2.leq(MathNumber.ZERO) && u2.geq(MathNumber.ZERO)) {
				return IntervalLattice.TOP;
			}

			// if 0 not in [l2, u2] :
			// [l1, u1] / [l2, u2] = [l1, u1] * [1/l2 , 1/u2]
			// = [min(l1/l2, l1/u2, u1/l2, u1/u2), max(l1/l2, l1/u2, u1/l2, u1/u2)]
			MathNumber l1l2 = l1.divide(l2);
			MathNumber l1u2 = l1.divide(u2);
			MathNumber u1l2 = u1.divide(l2);
			MathNumber u1u2 = u1.divide(u2);

			return new IntervalLattice(l1l2.min(l1u2).min(u1l2).min(u1u2).roundDown(), l1l2.max(l1u2).max(u1l2).max(u1u2).roundUp());
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
