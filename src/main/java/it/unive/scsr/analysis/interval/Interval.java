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

import java.math.BigDecimal;
import java.math.RoundingMode;

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

			MathNumber p1 = l1.multiply(l2);
			MathNumber p2 = l1.multiply(u2);
			MathNumber p3 = u1.multiply(l2);
			MathNumber p4 = u1.multiply(u2);

			MathNumber low = p1;
			if (p2.compareTo(low) < 0) low = p2;
			if (p3.compareTo(low) < 0) low = p3;
			if (p4.compareTo(low) < 0) low = p4;

			MathNumber high = p1;
			if (p2.compareTo(high) > 0) high = p2;
			if (p3.compareTo(high) > 0) high = p3;
			if (p4.compareTo(high) > 0) high = p4;

			return new IntervalLattice(low, high);


		} else if (expression.getOperator() instanceof SubtractionOperator) {
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			// Apply negation unary expression
			right =  new IntervalLattice(u2.multiply(MathNumber.MINUS_ONE),l2.multiply(MathNumber.MINUS_ONE));
			u2 = right.i.getHigh();
			l2 = right.i.getLow();

			return new IntervalLattice(l1.subtract(u2), u1.subtract(l2));

		} else if (expression.getOperator() instanceof DivisionOperator) {
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			if (l2.leq(MathNumber.ZERO)  && u2.geq(MathNumber.ZERO))
				return IntervalLattice.BOTTOM;

			// Compute all corner divisions
			MathNumber p1 = l1.divide(l2);
			MathNumber p2 = l1.divide(u2);
			MathNumber p3 = u1.divide(l2);
			MathNumber p4 = u1.divide(u2);

			// Define floor and ceil as lambdas
			java.util.function.Function<MathNumber, MathNumber> floor = (MathNumber n) -> {
				BigDecimal bd = n.getNumber();
				if (bd.scale() <= 0) {
					return n;
				}
				return new MathNumber(bd.setScale(0, java.math.RoundingMode.FLOOR));
			};

			java.util.function.Function<MathNumber, MathNumber> ceil = (MathNumber n) -> {
				BigDecimal bd = n.getNumber();
				if (bd.scale() <= 0) {
					return n;
				}
				return new MathNumber(bd.setScale(0, java.math.RoundingMode.CEILING));
			};

			MathNumber low = floor.apply(p1);
			if (floor.apply(p2).compareTo(low) < 0) low = floor.apply(p2);
			if (floor.apply(p3).compareTo(low) < 0) low = floor.apply(p3);
			if (floor.apply(p4).compareTo(low) < 0) low = floor.apply(p4);

			MathNumber high = ceil.apply(p1);
			if (ceil.apply(p2).compareTo(high) > 0) high = ceil.apply(p2);
			if (ceil.apply(p3).compareTo(high) > 0) high = ceil.apply(p3);
			if (ceil.apply(p4).compareTo(high) > 0) high = ceil.apply(p4);

			return new IntervalLattice(low, high);
		}
		return IntervalLattice.TOP;
	}

	
	
	
	
}
