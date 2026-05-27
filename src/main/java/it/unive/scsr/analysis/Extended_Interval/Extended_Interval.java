package it.unive.scsr.analysis.Extended_Interval;

import java.math.BigDecimal;

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

public class Extended_Interval implements BaseNonRelationalValueDomain<Extended_IntervalLattice>{

	@Override
	public Extended_IntervalLattice top() {
		return Extended_IntervalLattice.TOP;
	}

	@Override
	public Extended_IntervalLattice bottom() {
		return Extended_IntervalLattice.BOTTOM;
	}
	
	@Override
	public Extended_IntervalLattice 
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		
		if(constant.getValue() instanceof Integer) {
			Integer n = (Integer) constant.getValue();
			return new Extended_IntervalLattice(n, n);
		}
		else if(constant.getValue() instanceof Double) {
			Double n = (Double) constant.getValue();
			return new Extended_IntervalLattice(n, n);
		}
		else if(constant.getValue() instanceof Float) {
			Float n = (Float) constant.getValue();
			return new Extended_IntervalLattice(n, n);
		}
			
		return Extended_IntervalLattice.TOP;
	}

	@Override
	public Extended_IntervalLattice evalUnaryExpression(UnaryExpression expression, Extended_IntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if(arg.i == null)
			return Extended_IntervalLattice.BOTTOM;
		
		if(expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.i.getHigh();
			MathNumber l = arg.i.getLow();
			
			return new Extended_IntervalLattice(u.multiply(MathNumber.MINUS_ONE),l.multiply(MathNumber.MINUS_ONE));
		}
	
		return Extended_IntervalLattice.TOP;
	}

	@Override
	public Extended_IntervalLattice evalBinaryExpression(BinaryExpression expression, Extended_IntervalLattice left,
			Extended_IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		
		if(left.i == null || right.i == null)
			return Extended_IntervalLattice.BOTTOM;
		
		if(expression.getOperator() instanceof AdditionOperator) {
			
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			
			return new Extended_IntervalLattice(l1.add(l2), u1.add(u2));
			
		} else if (expression.getOperator() instanceof MultiplicationOperator) {

			//[a,b], [c,d]

			MathNumber u1 = left.i.getHigh(); //b
			MathNumber u2 = right.i.getHigh(); //d
			
			MathNumber l1 = left.i.getLow(); //a
			MathNumber l2 = right.i.getLow(); //c

			if(l1.isInfinite() || u1.isInfinite() || l2.isInfinite() || u2.isInfinite())
    			return Extended_IntervalLattice.TOP;

			MathNumber newMin = new MathNumber(0);
			MathNumber newMax = new MathNumber(0);

			Double m1 = 0.0;
			Double m2 = 0.0;
			Double m3 = 0.0;
			Double m4 = 0.0;

			m1 = l1.getNumber().doubleValue() * l2.getNumber().doubleValue();
			m2 = l1.getNumber().doubleValue() * u2.getNumber().doubleValue();
			m3 = u1.getNumber().doubleValue() * l2.getNumber().doubleValue();
			m4 = u1.getNumber().doubleValue() * u2.getNumber().doubleValue();
		
			newMax = new MathNumber(BigDecimal.valueOf(maximumCounter(m1, m2, m3, m4)));
			newMin = new MathNumber(BigDecimal.valueOf(minimumCounter(m1, m2, m3, m4)));
			
			return new Extended_IntervalLattice(newMin, newMax);
			
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
				newMin = new MathNumber(l1.getNumber().doubleValue()-u2.getNumber().doubleValue());
				newMax = new MathNumber(u1.getNumber().doubleValue()-l2.getNumber().doubleValue());
			}

			return new Extended_IntervalLattice(newMin, newMax);

		} else if (expression.getOperator() instanceof DivisionOperator) {

			MathNumber u1 = left.i.getHigh(); //b
			MathNumber u2 = right.i.getHigh(); //d
			
			MathNumber l1 = left.i.getLow(); //a
			MathNumber l2 = right.i.getLow(); //c

			if(l1.isInfinite() || u1.isInfinite() || l2.isInfinite() || u2.isInfinite())
    			return Extended_IntervalLattice.TOP;

			//[a,b] / [c,d] = [min(a/c, a/d, b/c, b/d), max(a/c, a/d, b/c, b/d)]

			Double m1 = 0.0;
			Double m2 = 0.0;
			Double m3 = 0.0;
			Double m4 = 0.0;

			MathNumber newMin = new MathNumber(0);
			MathNumber newMax = new MathNumber(0);

			if(l2.getNumber().doubleValue() <= 0 && u2.getNumber().doubleValue() >= 0)
				return Extended_IntervalLattice.BOTTOM;

			m1 = l1.getNumber().doubleValue() / l2.getNumber().doubleValue();
			m2 = l1.getNumber().doubleValue() / u2.getNumber().doubleValue();
			m3 = u1.getNumber().doubleValue() / l2.getNumber().doubleValue();
			m4 = u1.getNumber().doubleValue() / u2.getNumber().doubleValue();
			
			newMax = new MathNumber(maximumCounter(m1, m2, m3, m4));
			newMin = new MathNumber(minimumCounter(m1, m2, m3, m4));

			return new Extended_IntervalLattice(newMin, newMax);
		}

		return Extended_IntervalLattice.TOP;
	}

	public double minimumCounter(double a, double b, double c, double d)
	{
		double newMin = 0;
		if(Math.min(a,b) < Math.min(c,d))	
			newMin = Math.min(a,b);

		else
			newMin = Math.min(c,d);

		return newMin;
	}

	public double maximumCounter(double a, double b, double c, double d)
	{
		double newMax = 0;
		if(Math.max(a,b) > Math.max(c,d))	
			newMax = Math.max(a,b);

		else
			newMax = Math.max(c,d);

		return newMax;
	}
}

