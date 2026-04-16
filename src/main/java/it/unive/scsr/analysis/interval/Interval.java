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

import java.util.ArrayList;
import java.util.Collections;

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

		MathNumber l1 = left.i.getLow();
		MathNumber u1 = left.i.getHigh();

		MathNumber l2 = right.i.getLow();
		MathNumber u2 = right.i.getHigh();
		
		if(expression.getOperator() instanceof AdditionOperator) {

			return new IntervalLattice(l1.add(l2), u1.add(u2));

		} else if (expression.getOperator() instanceof MultiplicationOperator) {

			ArrayList<MathNumber> list = new ArrayList<>();

			list.add(l1.multiply(l2));
			list.add(l1.multiply(u2));
			list.add(u1.multiply(l2));
			list.add(u1.multiply(u2));

			return new IntervalLattice(Collections.min(list), Collections.max(list));

		} else if (expression.getOperator() instanceof SubtractionOperator) {

			return new IntervalLattice(l1.subtract(u2),u1.subtract(l2));

		} else if (expression.getOperator() instanceof DivisionOperator) {

			if (l2.compareTo(MathNumber.ZERO) <= 0 && u2.compareTo(MathNumber.ZERO) >= 0) {
				return IntervalLattice.TOP;
			}

			ArrayList<MathNumber> list = new ArrayList<>();

			list.add(l1.divide(l2));
			list.add(l1.divide(u2));
			list.add(u1.divide(l2));
			list.add(u1.divide(u2));

			return new IntervalLattice(Collections.min(list), Collections.max(list));
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
