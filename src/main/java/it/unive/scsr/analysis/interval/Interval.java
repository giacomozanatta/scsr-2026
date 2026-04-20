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
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			MathNumber pot_l1 = u1.multiply(l2);
			MathNumber pot_l2 = u2.multiply(l1);

			MathNumber pot_u1 = u1.multiply(u2);
			MathNumber pot_u2 = l1.multiply(l2);

			return new IntervalLattice(pot_l1.min(pot_l2),pot_u1.max(pot_u2));
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			MathNumber lu = left.i.getHigh();
			MathNumber ru = right.i.getHigh();

			MathNumber ll = left.i.getLow();
			MathNumber rl = right.i.getLow();

			MathNumber pot_l1 = ll.subtract(ru);
			MathNumber pot_l2 = ll.subtract(rl);

			MathNumber pot_u1 = lu.subtract(ru);
			MathNumber pot_u2 = lu.subtract(rl);

			return new IntervalLattice(pot_l1.min(pot_l2),pot_u1.max(pot_u2));
		} else if (expression.getOperator() instanceof DivisionOperator) {
			MathNumber lu = left.i.getHigh();
			MathNumber ru = right.i.getHigh();

			MathNumber ll = left.i.getLow();
			MathNumber rl = right.i.getLow();

			MathNumber pot_l1 = ll.divide(ru);
			MathNumber pot_l2 = ll.divide(rl);

			MathNumber pot_u1 = lu.divide(ru);
			MathNumber pot_u2 = lu.divide(rl);

			return new IntervalLattice(pot_l1.min(pot_l2),pot_u1.max(pot_u2));
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
