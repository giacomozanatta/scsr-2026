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
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.IntInterval;
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

		if (left.i == null || right == null || right.i == null)
			return IntervalLattice.BOTTOM;

		if (left.i.isBottom() || right.i.isBottom())
			return IntervalLattice.BOTTOM;

		BinaryOperator op = expression.getOperator();

		// LiSA: division can still yield ZERO or BOTTOM with TOP operands; other ops widen to TOP.
		if (!(op instanceof DivisionOperator) && (left.i.isTop() || right.i.isTop()))
			return IntervalLattice.TOP;

		if (op instanceof AdditionOperator) {
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			return new IntervalLattice(l1.add(l2), u1.add(u2));
		} else if (op instanceof SubtractionOperator) {
			IntInterval d = left.i.diff(right.i);
			return new IntervalLattice(d.getLow(), d.getHigh());
		} else if (op instanceof MultiplicationOperator) {
			if (left.i.is(0) || right.i.is(0))
				return new IntervalLattice(0, 0);
			if (left.i.isTop() || right.i.isTop())
				return IntervalLattice.TOP;
			IntInterval m = left.i.mul(right.i);
			return new IntervalLattice(m.getLow(), m.getHigh());
		} else if (op instanceof DivisionOperator) {
			if (right.i.is(0))
				return IntervalLattice.BOTTOM;
			if (left.i.is(0))
				return new IntervalLattice(0, 0);
			if (left.i.isTop() || right.i.isTop())
				return IntervalLattice.TOP;
			IntInterval q = left.i.div(right.i, false, false);
			if (q.isBottom())
				return IntervalLattice.BOTTOM;
			return new IntervalLattice(q.getLow(), q.getHigh());
		}

		return IntervalLattice.TOP;
	}

	
	
	
	
}
