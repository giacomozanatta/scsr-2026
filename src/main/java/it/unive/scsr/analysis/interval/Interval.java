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

            MathNumber p1 = l1.multiply(l2);    // l1 x l2
            MathNumber p2 = l1.multiply(u2);    // l1 x u2
            MathNumber p3 = u1.multiply(l2);    // u1 x l2
            MathNumber p4 = u1.multiply(u2);    // u1 x u2

            MathNumber min = p1;
            MathNumber max = p1;

            if(p2.lt(min))  min = p2;
            if(p2.gt(max))  max = p2;

            if(p3.lt(min))  min =  p3;
            if(p3.gt(max))  max = p3;

            if(p4.lt(min))  min =  p4;
            if(p4.gt(max))  max =  p4;

            return new IntervalLattice(min, max);

		} else if (expression.getOperator() instanceof SubtractionOperator) {
            MathNumber l1 = left.i.getLow();
            MathNumber u1 = left.i.getHigh();
            MathNumber l2 = right.i.getLow();
            MathNumber u2 = right.i.getHigh();

            MathNumber lResult = l1.subtract(l2);
            MathNumber uResult = u1.subtract(u2);

            return new IntervalLattice(lResult, uResult);

		} else if (expression.getOperator() instanceof DivisionOperator) {
            if(left.i == null || right.i == null)
                return IntervalLattice.BOTTOM;

            MathNumber l2 = right.i.getLow();
            MathNumber u2 = right.i.getLow();

            MathNumber zero = new MathNumber(0);
            if((l2.leq(zero)) && zero.leq(u2))
                // l2 <= 0 <= u2; interval contains zero
                return IntervalLattice.BOTTOM;

            MathNumber invl = new MathNumber(1).divide(u2);
            MathNumber invu = new MathNumber(1).divide(l2);

            IntervalLattice inverse = new IntervalLattice(invl, invu);

            MathNumber l1 = left.i.getLow();
            MathNumber u1 = left.i.getHigh();

            MathNumber p1 = l1.multiply(invl);
            MathNumber p2 = l1.multiply(invu);
            MathNumber p3 = u1.multiply(invl);
            MathNumber p4 = u1.multiply(invu);

            MathNumber min = p1;
            MathNumber max = p1;

            if(p2.lt(min)) min = p2;
            if(p2.gt(max)) max = p2;
            if(p3.lt(min)) min = p3;
            if(p3.gt(max)) max = p3;
            if(p4.lt(min)) min = p4;
            if(p4.gt(max)) max = p4;

            return new IntervalLattice(min, max);
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
