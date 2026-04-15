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

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
		
		if(expression.getOperator() instanceof AdditionOperator) {
			return new IntervalLattice(l1.add(l2), u1.add(u2));	
		} 
		else if (expression.getOperator() instanceof MultiplicationOperator) {
			// [a,b] * [c,d] = [min{ac,ad,bc,bd}, max{ac,ad,bc,bd}]
			// min in this case works that returns the object from where it was callled if it is the minimum, else returns other
			return new IntervalLattice(
					// left
					l1.multiply(l2).min(l1.multiply(u2)).min(u1.multiply(l2)).min(u1.multiply(u2)),
					// right
					l1.multiply(l2).max(l1.multiply(u2)).max(u1.multiply(l2)).max(u1.multiply(u2))
					);
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			// [a,b] - [c,d] = [a-d, b-c]
			return new IntervalLattice(l1.add(u2.multiply(new MathNumber(-1))), u1.add(l2.multiply(new MathNumber(-1))));
		} else if (expression.getOperator() instanceof DivisionOperator) {
			//same as multiplication, only we have to treat the case of division by zero, that is not defined, so we return bottom
			if (right.lessOrEqual(IntervalLattice.ZERO)) return IntervalLattice.BOTTOM;
			return new IntervalLattice(
					// left (floor)
					l1.divide(l2).min(l1.divide(u2)).min(u1.divide(l2)).min(u1.divide(u2)).roundDown(),
					// right (ceiling)
					l1.divide(l2).max(l1.divide(u2)).max(u1.divide(l2)).max(u1.divide(u2)).roundUp()
					);
		}
		// else idk what is going on because is too high for me to know it 
		return IntervalLattice.TOP;
	}

	
	
	
	
}
