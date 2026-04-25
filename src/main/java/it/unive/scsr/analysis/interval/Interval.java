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

        private IntervalLattice multLattices(IntervalLattice left, IntervalLattice right)
        {
            MathNumber a1 = left.i.getLow();
            MathNumber a2 = left.i.getHigh();

            MathNumber b1 = right.i.getLow();
            MathNumber b2 = right.i.getHigh();

            MathNumber[] n = new MathNumber[4];
            n[0] = a1.multiply(b1);
            n[1] = a1.multiply(b2);
            n[2] = a2.multiply(b1);
            n[3] = a2.multiply(b2);
            MathNumber n1 = n[0];
            MathNumber n2 = n[0];
            for(int i = 1; i < n.length; i++)
            {
                if(n[i].leq(n1))
                    n1 = n[i];
                if(n[i].geq(n2))
                    n2 = n[i];
            }     
            return new IntervalLattice(n1, n2);
        }
        
        private IntervalLattice divLattices(IntervalLattice left, IntervalLattice right)
        {
            MathNumber a1 = left.i.getLow();
            MathNumber a2 = left.i.getHigh();

            MathNumber b1 = right.i.getLow();
            MathNumber b2 = right.i.getHigh();
            
            if(b1.leq(MathNumber.ZERO) && b2.geq(MathNumber.ZERO))
            {
                return IntervalLattice.BOTTOM;
            }
            MathNumber[] n = new MathNumber[4];
            n[0] = a1.divide(b1);
            n[1] = a1.divide(b2);
            n[2] = a2.divide(b1);
            n[3] = a2.divide(b2);
            MathNumber n1 = n[0];
            MathNumber n2 = n[0];
            for(int i = 1; i < n.length; i++)
            {
                if(n[i].leq(n1))
                    n1 = n[i];
                if(n[i].geq(n2))
                    n2 = n[i];
            }     
            return new IntervalLattice(n1, n2);
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
			return multLattices(left, right);
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			
			return new IntervalLattice(l1.subtract(l2), u1.subtract(u2));
		} else if (expression.getOperator() instanceof DivisionOperator) {
			return divLattices(left, right);
		}
		
		return IntervalLattice.TOP;
	}

	
	
	
	
}
