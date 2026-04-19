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
            // 0 * infinity
            if (left.equals(IntervalLattice.ZERO) || right.equals(IntervalLattice.ZERO)) {
                return IntervalLattice.ZERO;
            }

            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            MathNumber p1 = l1.multiply(l2);
            MathNumber p2 = l1.multiply(u2);
            MathNumber p3 = u1.multiply(l2);
            MathNumber p4 = u1.multiply(u2);

            MathNumber minV = p1.min(p2).min(p3).min(p4);
            MathNumber maxV = p1.max(p2).max(p3).max(p4);

            if (minV.isNaN() || maxV.isNaN()) return IntervalLattice.TOP;

            return new IntervalLattice(minV, maxV);

		} else if (expression.getOperator() instanceof SubtractionOperator) {
            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            // [l1, u1] - [l2, u2] = [l1 - u2, u1 - l2]
            MathNumber lSub = l1.subtract(u2);
            MathNumber uSub = u1.subtract(l2);

            // check infinity - infinity
            if (lSub.isNaN() || uSub.isNaN()) return IntervalLattice.TOP;

            return new IntervalLattice(lSub, uSub);

		} else if (expression.getOperator() instanceof DivisionOperator) {
            MathNumber u1 = left.i.getHigh();
            MathNumber u2 = right.i.getHigh();
            MathNumber l1 = left.i.getLow();
            MathNumber l2 = right.i.getLow();

            // 0
            if (l2.isZero() && u2.isZero()) {
                return IntervalLattice.BOTTOM;
            }

            // divisor with 0 [-2, 5] then [-Inf, +Inf]
            if (l2.compareTo(MathNumber.ZERO) <= 0 && u2.compareTo(MathNumber.ZERO) >= 0) {
                return IntervalLattice.TOP;
            }

            // no 0:
            MathNumber p1 = l1.divide(l2);
            MathNumber p2 = l1.divide(u2);
            MathNumber p3 = u1.divide(l2);
            MathNumber p4 = u1.divide(u2);

            MathNumber minV = p1.min(p2).min(p3).min(p4);
            MathNumber maxV = p1.max(p2).max(p3).max(p4);

            if (minV.isNaN() || maxV.isNaN()) return IntervalLattice.TOP;

            return new IntervalLattice(minV, maxV);

		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
