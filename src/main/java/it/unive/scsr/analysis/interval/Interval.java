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

		if (left.isBottom() || right.isBottom())
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

			MathNumber v1 = safeMultiply(l1,l2);
			MathNumber v2 = safeMultiply(l1,u2);
			MathNumber v3 = safeMultiply(u1,l2);
			MathNumber v4 = safeMultiply(u1,u2);

			if (v1.isNaN() || v2.isNaN() || v3.isNaN() || v4.isNaN()) {
				return IntervalLattice.TOP;
			}

			return new IntervalLattice(v1.min(v2).min(v3).min(v4), v1.max(v2).max(v3).max(v4));
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			MathNumber lNew = l1.subtract(u2);
			MathNumber uNew = u1.subtract(l2);

			if(lNew.isNaN() || uNew.isNaN())
				return IntervalLattice.TOP;

			return new IntervalLattice(lNew,uNew);

		} else if (expression.getOperator() instanceof DivisionOperator) {
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();

			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			if (l2.leq(MathNumber.ZERO) && u2.geq(MathNumber.ZERO)) {
				return IntervalLattice.TOP;
			}

			MathNumber v1 = l1.divide(l2);
			MathNumber v2 = l1.divide(u2);
			MathNumber v3 = u1.divide(l2);
			MathNumber v4 = u1.divide(u2);

			if (v1.isNaN() || v2.isNaN() || v3.isNaN() || v4.isNaN())
				return IntervalLattice.TOP;

			return new IntervalLattice(v1.min(v2).min(v3).min(v4), v1.max(v2).max(v3).max(v4));
		}
		
		return IntervalLattice.TOP;
	}

	private MathNumber safeMultiply(MathNumber a, MathNumber b) {
		if (a.isNaN() || b.isNaN())
			return MathNumber.NaN;

		if ((a.isZero() && b.isInfinite()) || (a.isInfinite() && b.isZero()))
			return MathNumber.ZERO;

		return a.multiply(b);
	}
	
}
