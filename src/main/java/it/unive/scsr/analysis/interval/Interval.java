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
		
		if(expression.getOperator() instanceof AdditionOperator) {
			
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			
			return new IntervalLattice(l1.add(l2), u1.add(u2));
			
		} else if (expression.getOperator() instanceof MultiplicationOperator) {

			//[a,b], [c,d]

			MathNumber u1 = left.i.getHigh(); //b
			MathNumber u2 = right.i.getHigh(); //d
			
			MathNumber l1 = left.i.getLow(); //a
			MathNumber l2 = right.i.getLow(); //c

			MathNumber newMin = new MathNumber(0);
			MathNumber newMax = new MathNumber(0);

			Integer m1 = 0;
			Integer m2 = 0;
			Integer m3 = 0;
			Integer m4 = 0;

			if(u1.isInfinite() || u2.isInfinite())
				newMax = MathNumber.PLUS_INFINITY;

			if(l1.isInfinite() || l2.isInfinite())
				newMin = MathNumber.MINUS_INFINITY;


			if(u1.isFinite() && u2.isFinite() && l1.isFinite() && l2.isFinite())
			{
				m1 = l1.getNumber().intValue() * l2.getNumber().intValue();
				m2 = l1.getNumber().intValue() * u2.getNumber().intValue();
				m3 = u1.getNumber().intValue() * l2.getNumber().intValue();
				m4 = u1.getNumber().intValue() * u2.getNumber().intValue();
			}

			if(u1.isFinite() && u2.isFinite())
				newMax = new MathNumber(maximumCounter(m1, m2, m3, m4));
			
			if(l1.isFinite() && l2.isFinite())
				newMin = new MathNumber(minimumCounter(m1, m2, m3, m4));
			
			return new IntervalLattice(newMin, newMax);

			// TODO: homework
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			
			//[a,b] - [c,d] = [a-d, b-c]
			MathNumber u1 = left.i.getHigh(); //b
			MathNumber u2 = right.i.getHigh(); //d
			
			MathNumber l1 = left.i.getLow(); //a
			MathNumber l2 = right.i.getLow(); //c

			MathNumber newMin = new MathNumber(0);
			MathNumber newMax = new MathNumber(0);

			if(u1.isInfinite() || u2.isInfinite())
				newMax = MathNumber.PLUS_INFINITY;

			if(l1.isInfinite() || l2.isInfinite())
				newMin = MathNumber.MINUS_INFINITY;


			if(u1.isFinite() && u2.isFinite() && l1.isFinite() && l2.isFinite())
			{
				newMin = new MathNumber(l1.getNumber().intValue()-u2.getNumber().intValue());
				newMax = new MathNumber(u1.getNumber().intValue()-l2.getNumber().intValue());
			}

			return new IntervalLattice(newMin, newMax);



			// TODO: homework
		} else if (expression.getOperator() instanceof DivisionOperator) {
			// TODO: homework

			MathNumber u1 = left.i.getHigh(); //b
			MathNumber u2 = right.i.getHigh(); //d
			
			MathNumber l1 = left.i.getLow(); //a
			MathNumber l2 = right.i.getLow(); //c

			//[a,b] / [c,d] = [min(a/c, a/d, b/c, b/d), max(a/c, a/d, b/c, b/d)]

			Integer m1 = 0;
			Integer m2 = 0;
			Integer m3 = 0;
			Integer m4 = 0;

			MathNumber newMin = new MathNumber(0);
			MathNumber newMax = new MathNumber(0);

			if(u1.isInfinite())
   				 newMax = MathNumber.PLUS_INFINITY;

			if(l1.isInfinite())
    			newMin = MathNumber.MINUS_INFINITY;

			if(u1.isFinite() && u2.isFinite() && l1.isFinite() && l2.isFinite())
			{
				if(l2.getNumber().intValue() <= 0 && u2.getNumber().intValue() >= 0)
					return IntervalLattice.BOTTOM;

				m1 = l1.getNumber().intValue() / l2.getNumber().intValue();
				m2 = l1.getNumber().intValue() / u2.getNumber().intValue();
				m3 = u1.getNumber().intValue() / l2.getNumber().intValue();
				m4 = u1.getNumber().intValue() / u2.getNumber().intValue();
			}

			if(u1.isFinite() && u2.isFinite())
				newMax = new MathNumber(maximumCounter(m1, m2, m3, m4));
			
			if(l1.isFinite() && l2.isFinite())
				newMin = new MathNumber(minimumCounter(m1, m2, m3, m4));

			return new IntervalLattice(newMin, newMax);
		}

		return IntervalLattice.TOP;
	}

	public int minimumCounter(int a, int b, int c, int d)
	{
		int newMin = 0;
		if(Math.min(a,b) < Math.min(c,d))	
			newMin = Math.min(a,b);

		else
			newMin = Math.min(c,d);

		return newMin;
	}

	public int maximumCounter(int a, int b, int c, int d)
	{
		int newMax = 0;
		if(Math.max(a,b) > Math.max(c,d))	
			newMax = Math.max(a,b);

		else
			newMax = Math.max(c,d);

		return newMax;
	}
	
	
	
}
