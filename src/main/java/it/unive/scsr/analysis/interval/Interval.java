package it.unive.scsr.analysis.interval;

import java.util.List;

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
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;

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
		
		MathNumber upperLeft = left.i.getHigh();
		MathNumber upperRight = right.i.getHigh();

		MathNumber lowerLeft = left.i.getLow();
		MathNumber lowerRight = right.i.getLow();

		BinaryOperator op = expression.getOperator();

		if (op instanceof AdditionOperator) {
			return new IntervalLattice(lowerLeft.add(lowerRight), upperLeft.add(upperRight));

		} else if (op instanceof SubtractionOperator) {
			return new IntervalLattice(lowerLeft.subtract(upperRight),upperLeft.subtract(lowerRight));

		} else if (op instanceof MultiplicationOperator) {
			if (left.equals(IntervalLattice.ZERO) || right.equals(IntervalLattice.ZERO))
				return IntervalLattice.ZERO;

			MathNumber n1 = lowerLeft.multiply(lowerRight);
			MathNumber n2 = lowerLeft.multiply(upperRight);
			MathNumber n3 = upperLeft.multiply(lowerRight);
			MathNumber n4 = upperLeft.multiply(upperRight);

			MathNumber min = n1.min(n2).min(n3).min(n4);
			MathNumber max = n1.max(n2).max(n3).max(n4);

			return new IntervalLattice(min, max);

		} else if (op instanceof DivisionOperator) {
			if (right.i.includes(IntInterval.ZERO)) {
    			if (right.equals(IntervalLattice.ZERO)) {
        			return IntervalLattice.BOTTOM;
    			} else {
        			return IntervalLattice.TOP;
    			}
			}

			if (left.equals(IntervalLattice.ZERO))
				return IntervalLattice.ZERO;

			MathNumber q1 = lowerLeft.divide(lowerRight);
			MathNumber q2 = lowerLeft.divide(upperRight);
			MathNumber q3 = upperLeft.divide(lowerRight);
			MathNumber q4 = upperLeft.divide(upperRight);

			MathNumber min = q1.min(q2).min(q3).min(q4);
			MathNumber max = q1.max(q2).max(q3).max(q4);

			return new IntervalLattice(min.roundDown(), max.roundUp());
		}
		return IntervalLattice.TOP;
	}
}
