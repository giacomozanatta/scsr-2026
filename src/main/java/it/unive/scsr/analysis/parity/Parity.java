package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
        public ParityLattice top() {
            return ParityLattice.TOP;
        }

        @Override
        public ParityLattice bottom() {
            return ParityLattice.BOTTOM;
        }

        @Override
	public ParityLattice 
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {		
            if(constant.getValue() instanceof Integer n) {
                if(n % 2 == 0)
                    return ParityLattice.EVEN;
                else
                    return ParityLattice.ODD;
            }			
            return ParityLattice.TOP;
	}    
        
        @Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException 
        {
            if(expression.getOperator() == NumericNegation.INSTANCE)
            {
                if(arg == ParityLattice.BOTTOM)
                    return ParityLattice.BOTTOM;
                else if(arg == ParityLattice.TOP)
                    return ParityLattice.TOP;
                else if(arg == ParityLattice.EVEN)
                    return ParityLattice.EVEN;
                else if(arg == ParityLattice.ODD)
                    return ParityLattice.ODD;
            }
            return ParityLattice.TOP;
        }
        
        @Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
            if(expression.getOperator() instanceof AdditionOperator || expression.getOperator() instanceof SubtractionOperator)
            {
                if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
                    return ParityLattice.BOTTOM;
                else if(left == ParityLattice.TOP || right == ParityLattice.TOP)
                    return ParityLattice.TOP;
                else if((left == ParityLattice.EVEN && right == ParityLattice.ODD) || 
                        (left == ParityLattice.ODD && right == ParityLattice.EVEN))
                    return ParityLattice.ODD;
                else if((left == ParityLattice.EVEN && right == ParityLattice.EVEN) || 
                        (left == ParityLattice.ODD && right == ParityLattice.ODD))
                    return ParityLattice.EVEN;
            }
            if(expression.getOperator() instanceof MultiplicationOperator)
            {
                if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
                    return ParityLattice.BOTTOM;
                else if(left == ParityLattice.TOP || right == ParityLattice.TOP)
                    return ParityLattice.TOP;
                else if(left == ParityLattice.EVEN || right == ParityLattice.EVEN)
                    return ParityLattice.EVEN;
                else if(left == ParityLattice.ODD && right == ParityLattice.ODD)
                    return ParityLattice.ODD;
            }
            if(expression.getOperator() instanceof DivisionOperator)
            {
                if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
                    return ParityLattice.BOTTOM;
                return ParityLattice.TOP;
            }
            return ParityLattice.TOP;
        }
           
        @Override
	public Satisfiability satisfiesBinaryExpression(
			BinaryExpression expression,
			ParityLattice left,
			ParityLattice right,
			ProgramPoint pp,
			SemanticOracle oracle) {
		if (left.isTop() || right.isTop())
			return Satisfiability.UNKNOWN;

		BinaryOperator operator = expression.getOperator();
		if (operator == ComparisonEq.INSTANCE)
			return left.eq(right);
		else if (operator == ComparisonGe.INSTANCE)
			return left.eq(right).or(left.gt(right));
		else if (operator == ComparisonGt.INSTANCE)
			return left.gt(right);
		else if (operator == ComparisonLe.INSTANCE)
			// e1 <= e2 same as !(e1 > e2)
			return left.gt(right).negate();
		else if (operator == ComparisonLt.INSTANCE)
			// e1 < e2 -> !(e1 >= e2) && !(e1 == e2)
			return left.gt(right).negate().and(left.eq(right).negate());
		else if (operator == ComparisonNe.INSTANCE)
			return left.eq(right).negate();
		else
			return Satisfiability.UNKNOWN;
	}
}