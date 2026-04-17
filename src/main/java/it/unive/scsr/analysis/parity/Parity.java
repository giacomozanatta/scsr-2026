package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
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

		public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

			if(constant.getValue() instanceof Integer) 
			{
				Integer n = (Integer) constant.getValue();
				if(n%2==0)
					return ParityLattice.EVEN;
				else 
					return ParityLattice.ODD;
			}
			return ParityLattice.TOP;
		}


		@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		
		if(arg == ParityLattice.BOTTOM)
			return ParityLattice.BOTTOM;
		
		if(expression.getOperator() == NumericNegation.INSTANCE) {
			if(arg == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			else if(arg == ParityLattice.ODD)
				return ParityLattice.ODD;
		}

		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if(expression.getOperator() instanceof AdditionOperator) 
		{
			if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;
			
			if(left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;

			if(left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.EVEN;

			if(left == ParityLattice.ODD && right == ParityLattice.EVEN || left == ParityLattice.EVEN && right == ParityLattice.ODD)
				return ParityLattice.ODD;

			if(left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			
		}
		else if(expression.getOperator() instanceof SubtractionOperator) 
		{
			if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;
			
			if(left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;

			if(left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.EVEN;

			if(left == ParityLattice.ODD && right == ParityLattice.EVEN || left == ParityLattice.EVEN && right == ParityLattice.ODD)
				return ParityLattice.ODD;

			if(left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
		}
		else if (expression.getOperator() instanceof MultiplicationOperator) 
		{
			if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;
			
			else if(left == ParityLattice.TOP && right == ParityLattice.TOP)
				return ParityLattice.TOP;

			else if(left == ParityLattice.TOP && right == ParityLattice.EVEN || left == ParityLattice.EVEN && right == ParityLattice.TOP)
				return ParityLattice.EVEN;

			else if(left == ParityLattice.TOP && right == ParityLattice.ODD || left == ParityLattice.ODD && right == ParityLattice.TOP)
				return ParityLattice.TOP;
	
			else if(left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
 
			else if(left == ParityLattice.EVEN && right == ParityLattice.ODD || left == ParityLattice.ODD && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;

			else if(left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.ODD;

		} 
		
		else if (expression.getOperator() instanceof DivisionOperator) {
			
			if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;
			
			else if(left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;

			else if(left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.TOP;

			else if(left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.TOP;
		} 
		
		else if (expression.getOperator() instanceof ModuloOperator)
		{
			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
        		return ParityLattice.BOTTOM;

			return ParityLattice.TOP;
		}

		else if (expression.getOperator() instanceof RemainderOperator)
		{
			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
        		return ParityLattice.BOTTOM;

			return ParityLattice.TOP;
		}
			
		return ParityLattice.TOP;
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expr, ParityLattice left, ParityLattice right, ProgramPoint pp,
        SemanticOracle oracle) throws SemanticException 
	{
		BinaryOperator op = expr.getOperator();

		if (left.isBottom() || right.isBottom())
        	return Satisfiability.BOTTOM;

    	if (op instanceof ComparisonEq)
        	return left.eq(right);  

    	if (op instanceof ComparisonNe)
        	return left.neq(right); 

    	return Satisfiability.UNKNOWN; //this takes in cosideration all other operators since it's impossible to know if a EVEN/ODD 
		                               //is greater than another even in the abstract domain
	}

	@Override
	public ValueEnvironment<ParityLattice> assumeBinaryExpression(ValueEnvironment<ParityLattice> env, BinaryExpression expr,
        ProgramPoint pp1, ProgramPoint pp2, SemanticOracle oracle) throws SemanticException 
	{
    	BinaryOperator op = expr.getOperator();

    	if (!(expr.getLeft() instanceof Identifier) || !(expr.getRight() instanceof Identifier))
        	return env;

    	Identifier left  = (Identifier) expr.getLeft();
    	Identifier right = (Identifier) expr.getRight();

    	ParityLattice lval = env.getState(left);
    	ParityLattice rval = env.getState(right);

    	if (op instanceof ComparisonEq) 
		{
        	ParityLattice refined = lval.glb(rval);
        	env = env.putState(left, refined);
        	env = env.putState(right, refined);
		} 
		else if (op instanceof ComparisonNe) 
		{
        	if (lval.equals(rval) && !lval.isTop() && !lval.isBottom()) 
			{
            	env = env.putState(left, bottom());
            	env = env.putState(right, bottom());
        	}
    	}
    	return env;
	}
}