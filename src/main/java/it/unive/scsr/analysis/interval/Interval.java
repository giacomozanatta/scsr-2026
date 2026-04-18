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

    /**
     * @author Mattia Acquilesi 896827
     * @author Alan Dal Col 895879
     */
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

            MathNumber a = left.i.getLow();
            MathNumber b = left.i.getHigh();
            MathNumber c = right.i.getLow();
            MathNumber d = right.i.getHigh();

            MathNumber ac = a.multiply(c);
            MathNumber ad = a.multiply(d);
            MathNumber bc = b.multiply(c);
            MathNumber bd = b.multiply(d);

            MathNumber lower = ac.min(ad).min(bc).min(bd);
            MathNumber upper = ac.max(ad).max(bc).max(bd);

            return new IntervalLattice(lower, upper);

        } else if (expression.getOperator() instanceof SubtractionOperator) {

            MathNumber a = left.i.getLow();
            MathNumber b = left.i.getHigh();
            MathNumber c = right.i.getLow();
            MathNumber d = right.i.getHigh();

            return new IntervalLattice(a.subtract(d), b.subtract(c));

        } else if (expression.getOperator() instanceof DivisionOperator) {

            MathNumber a = left.i.getLow();
            MathNumber b = left.i.getHigh();
            MathNumber c = right.i.getLow();
            MathNumber d = right.i.getHigh();

            if (c.equals(MathNumber.ZERO) && d.equals(MathNumber.ZERO))
                return IntervalLattice.BOTTOM;

            if (c.leq(MathNumber.ZERO) && MathNumber.ZERO.leq(d))
                return IntervalLattice.TOP;

            MathNumber ac = a.divide(c);
            MathNumber ad = a.divide(d);
            MathNumber bc = b.divide(c);
            MathNumber bd = b.divide(d);

            MathNumber lower = ac.min(ad).min(bc).min(bd);
            MathNumber upper = ac.max(ad).max(bc).max(bd);

            return new IntervalLattice(lower, upper);
        }

        return IntervalLattice.TOP;
    }

	
	
	
	
}
