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

            if (left.equals(IntervalLattice.ZERO)) {
                return IntervalLattice.ZERO;
            }
            else if (right.equals(IntervalLattice.ZERO)) {
                return IntervalLattice.ZERO;
            }

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            MathNumber n1 = l1.multiply(l2);
            MathNumber n2 = l1.multiply(u2);
            MathNumber n3 = u1.multiply(l2);
            MathNumber n4 = u1.multiply(u2);

            MathNumber min = n1.min(n2).min(n3).min(n4);
            MathNumber max = n1.max(n2).max(n3).max(n4);

            if (min.isNaN()) return IntervalLattice.TOP;
            else if (max.isNaN())  return IntervalLattice.TOP;

            return new IntervalLattice(min, max);
        } else if (expression.getOperator() instanceof SubtractionOperator) {

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            MathNumber ls = l1.subtract(u2);
            MathNumber us = u1.subtract(l2);

            if (ls.isNaN()) return IntervalLattice.TOP;
            else if (us.isNaN()) return IntervalLattice.TOP;

            return new IntervalLattice(ls, us);
        } else if (expression.getOperator() instanceof DivisionOperator) {

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();

            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            if (l2.isZero() && u2.isZero()) {
                return IntervalLattice.BOTTOM;
            }

            MathNumber n1 = l1.divide(l2);
            MathNumber n2 = l1.divide(u2);
            MathNumber n3 = u1.divide(l2);
            MathNumber n4 = u1.divide(u2);

            MathNumber min = n1.min(n2).min(n3).min(n4);
            MathNumber max = n1.max(n2).max(n3).max(n4);

            if (min.isNaN()) return IntervalLattice.TOP;
            else if (max.isNaN())  return IntervalLattice.TOP;

            return new IntervalLattice(min, max);
        }
		return IntervalLattice.TOP;
	}

	
	
	
	
}
