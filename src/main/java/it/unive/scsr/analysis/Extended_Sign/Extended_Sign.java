package it.unive.scsr.analysis.Extended_Sign;

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

public class Extended_Sign implements BaseNonRelationalValueDomain<Extended_SignLattice>{

	@Override
	public Extended_SignLattice top() {
		return Extended_SignLattice.TOP;
	}

	@Override
	public Extended_SignLattice bottom() {
		return Extended_SignLattice.BOTTOM;
	}

	@Override
	public Extended_SignLattice 
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		
		if(constant.getValue() instanceof Integer) 
		{
			Integer n = (Integer) constant.getValue();
			
			if(n == 0)
				return Extended_SignLattice.ZERO;

			else if(n > 0)
				return Extended_SignLattice.STRICT_POS;

			return Extended_SignLattice.STRICT_NEG;
		}
			
		return Extended_SignLattice.TOP;
	}

	@Override
	public Extended_SignLattice evalUnaryExpression(UnaryExpression expression, Extended_SignLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		
		if(expression.getOperator() == NumericNegation.INSTANCE) {
			if(arg == Extended_SignLattice.NEG)
				return Extended_SignLattice.POS;
			else if(arg == Extended_SignLattice.POS)
				return Extended_SignLattice.NEG;
			else if(arg == Extended_SignLattice.ZERO)
				return Extended_SignLattice.ZERO;
			else if(arg == Extended_SignLattice.TOP)
				return Extended_SignLattice.TOP;
			else if(arg == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.STRICT_NEG;
			else if(arg == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.STRICT_POS;
			else if(arg == Extended_SignLattice.BOTTOM)
				return Extended_SignLattice.BOTTOM;
		}
	
		return Extended_SignLattice.TOP;
	}

	@Override
	public Extended_SignLattice evalBinaryExpression(BinaryExpression expression, Extended_SignLattice left, Extended_SignLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if(expression.getOperator() instanceof AdditionOperator) 
		{
			if(left == Extended_SignLattice.BOTTOM || right == Extended_SignLattice.BOTTOM)
				return Extended_SignLattice.BOTTOM;

			if(left == Extended_SignLattice.TOP || right == Extended_SignLattice.TOP)
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.POS && (right == Extended_SignLattice.NEG || right == Extended_SignLattice.STRICT_NEG) || left == Extended_SignLattice.NEG && (right == Extended_SignLattice.POS || right == Extended_SignLattice.STRICT_POS))
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.POS && (right == Extended_SignLattice.POS || right == Extended_SignLattice.ZERO || right == Extended_SignLattice.STRICT_POS))
				return Extended_SignLattice.POS;

			if((left == Extended_SignLattice.POS || left == Extended_SignLattice.ZERO || left == Extended_SignLattice.STRICT_POS) && right == Extended_SignLattice.POS)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.NEG && (right == Extended_SignLattice.NEG || right == Extended_SignLattice.ZERO || right == Extended_SignLattice.STRICT_NEG))
				return Extended_SignLattice.NEG;

			if((left == Extended_SignLattice.NEG || left == Extended_SignLattice.ZERO || left == Extended_SignLattice.STRICT_NEG) && right == Extended_SignLattice.NEG)
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.ZERO)
				return Extended_SignLattice.ZERO;

			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.STRICT_NEG;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.ZERO)
				return Extended_SignLattice.STRICT_NEG;

			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.ZERO)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.STRICT_NEG;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.TOP;
		} 
		else if (expression.getOperator() instanceof MultiplicationOperator) 
		{
			if(left == Extended_SignLattice.BOTTOM || right == Extended_SignLattice.BOTTOM)
				return Extended_SignLattice.BOTTOM;

			if(left == Extended_SignLattice.ZERO || right == Extended_SignLattice.ZERO)
				return Extended_SignLattice.ZERO;

			if(left == Extended_SignLattice.TOP || right == Extended_SignLattice.TOP)
				return Extended_SignLattice.TOP;	

			if(left == Extended_SignLattice.POS && right == Extended_SignLattice.POS)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.POS && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.POS && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.POS)
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.NEG)
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.NEG && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.POS)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.NEG && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.NEG)
				return Extended_SignLattice.POS;
			
			if(left == Extended_SignLattice.POS && right == Extended_SignLattice.NEG || left == Extended_SignLattice.NEG  && right == Extended_SignLattice.POS)
				return  Extended_SignLattice.NEG;

			if((left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.STRICT_NEG) || (left == Extended_SignLattice.STRICT_NEG  && right == Extended_SignLattice.STRICT_POS))
				return  Extended_SignLattice.STRICT_NEG;

			if(left == Extended_SignLattice.NEG && right == Extended_SignLattice.NEG)
				return Extended_SignLattice.POS;	
		} 
		else if (expression.getOperator() instanceof SubtractionOperator) 
		{
			if(left == Extended_SignLattice.BOTTOM || right == Extended_SignLattice.BOTTOM)
				return Extended_SignLattice.BOTTOM;

			if(left == Extended_SignLattice.TOP || right == Extended_SignLattice.TOP)
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.POS && (right == Extended_SignLattice.NEG || right == Extended_SignLattice.STRICT_NEG || right == Extended_SignLattice.ZERO))
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.POS && (right == Extended_SignLattice.POS || right == Extended_SignLattice.STRICT_POS))
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.NEG && (right == Extended_SignLattice.NEG || right == Extended_SignLattice.STRICT_NEG))
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.NEG && (right == Extended_SignLattice.POS || right == Extended_SignLattice.STRICT_POS || right == Extended_SignLattice.ZERO))
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.STRICT_POS && (right == Extended_SignLattice.NEG || right == Extended_SignLattice.STRICT_NEG || right == Extended_SignLattice.ZERO))
				return Extended_SignLattice.STRICT_POS;
			
			if(left == Extended_SignLattice.STRICT_POS && (right == Extended_SignLattice.POS || right == Extended_SignLattice.STRICT_POS))
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.STRICT_NEG && (right == Extended_SignLattice.NEG || right == Extended_SignLattice.STRICT_NEG))
				return Extended_SignLattice.TOP;
			
			if(left == Extended_SignLattice.STRICT_NEG && (right == Extended_SignLattice.POS || right == Extended_SignLattice.STRICT_POS || right == Extended_SignLattice.ZERO))
				return Extended_SignLattice.STRICT_NEG;
			
			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.ZERO)
				return Extended_SignLattice.ZERO;

			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.NEG)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.POS)
					return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.ZERO && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.STRICT_NEG;
		} 
		else if (expression.getOperator() instanceof DivisionOperator) 
		{
			if(left == Extended_SignLattice.BOTTOM || right == Extended_SignLattice.BOTTOM)
				return Extended_SignLattice.BOTTOM;

			if(left == Extended_SignLattice.ZERO && (right == Extended_SignLattice.POS || right == Extended_SignLattice.NEG))
    			return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.ZERO && (right == Extended_SignLattice.STRICT_NEG || right == Extended_SignLattice.STRICT_POS))
				return Extended_SignLattice.ZERO;

			if(right == Extended_SignLattice.ZERO)
				return Extended_SignLattice.BOTTOM;

			if(left == Extended_SignLattice.TOP || right == Extended_SignLattice.TOP)
				return Extended_SignLattice.TOP;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.STRICT_NEG;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.STRICT_POS)
				return Extended_SignLattice.STRICT_NEG;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.STRICT_NEG)
				return Extended_SignLattice.STRICT_POS;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.POS)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.STRICT_POS && right == Extended_SignLattice.NEG)
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.POS)
				return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.STRICT_NEG && right == Extended_SignLattice.NEG)
				return Extended_SignLattice.POS;

			if(left == Extended_SignLattice.POS && right == Extended_SignLattice.STRICT_POS)
    			return Extended_SignLattice.POS;
			
			if(left == Extended_SignLattice.POS && right == Extended_SignLattice.STRICT_NEG)
    			return Extended_SignLattice.NEG;

			if(left == Extended_SignLattice.NEG && right == Extended_SignLattice.STRICT_POS)
    			return Extended_SignLattice.NEG;
			
			if(left == Extended_SignLattice.NEG && right == Extended_SignLattice.STRICT_NEG)
    			return Extended_SignLattice.POS;

			if(right == Extended_SignLattice.POS || right == Extended_SignLattice.NEG)
    			return Extended_SignLattice.TOP;
		} 
		else if (expression.getOperator() instanceof ModuloOperator)
			return right;

		else if (expression.getOperator() instanceof RemainderOperator)
			return left;
		
		return Extended_SignLattice.TOP;
	}
	
	// Some information can be inferred also checking boolean expression and guards!
	// a = T, b = +; b < a means that  a = + 
	// These behaviors can be handeled with the following implementations
	
	@Override
	public Satisfiability satisfiesBinaryExpression(
			BinaryExpression expression,
			Extended_SignLattice left,
			Extended_SignLattice right,
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

	@Override
	public ValueEnvironment<Extended_SignLattice> assumeBinaryExpression(
			ValueEnvironment<Extended_SignLattice> environment,
			BinaryExpression expression,
			ProgramPoint src,
			ProgramPoint dest,
			SemanticOracle oracle)
			throws SemanticException {
		Satisfiability sat = satisfies(environment, expression, src, oracle);
		if (sat == Satisfiability.NOT_SATISFIED)
			return environment.bottom();
		if (sat == Satisfiability.SATISFIED)
			return environment;

		Identifier id;
		Extended_SignLattice eval;
		boolean rightIsExpr;
		BinaryOperator operator = expression.getOperator();
		ValueExpression left = (ValueExpression) expression.getLeft();
		ValueExpression right = (ValueExpression) expression.getRight();
		if (left instanceof Identifier) {
			eval = eval(environment, right, src, oracle);
			id = (Identifier) left;
			rightIsExpr = true;
		} else if (right instanceof Identifier) {
			eval = eval(environment, left, src, oracle);
			id = (Identifier) right;
			rightIsExpr = false;
		} else
			return environment;

		Extended_SignLattice starting = environment.getState(id);
		if (eval.isBottom() || starting.isBottom())
			return environment.bottom();

		Extended_SignLattice update = null;
		if (operator == ComparisonEq.INSTANCE)
			update = starting.glb(eval);
		else {
			
			Extended_SignLattice[] all = new Extended_SignLattice[] { 
    			Extended_SignLattice.STRICT_NEG, Extended_SignLattice.NEG, Extended_SignLattice.ZERO, 
    			Extended_SignLattice.POS, Extended_SignLattice.STRICT_POS };

			if (operator == ComparisonGe.INSTANCE)
				if (rightIsExpr) {
					for (Extended_SignLattice s : all)
						if (s.gt(eval).or(s.eq(eval)).mightBeTrue())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (Extended_SignLattice s : all)
						if (eval.gt(s).or(eval.eq(s)).mightBeTrue())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				}
			else if (operator == ComparisonLe.INSTANCE)
				if (rightIsExpr) {
					for (Extended_SignLattice s : all)
						// we invert <= to > and look at the failing ones
						if (s.gt(eval).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (Extended_SignLattice s : all)
						// we invert <= to > and look at the failing ones
						if (eval.gt(s).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				}
			else if (operator == ComparisonLt.INSTANCE)
				if (rightIsExpr) {
					for (Extended_SignLattice s : all)
						// we invert < to >= and look at the failing ones
						if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (Extended_SignLattice s : all)
						// we invert < to >= and look at the failing ones
						if (eval.gt(s).or(eval.eq(s)).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				}
			else if (operator == ComparisonGt.INSTANCE)
				if (rightIsExpr) {
					for (Extended_SignLattice s : all)
						if (s.gt(eval).mightBeTrue())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (Extended_SignLattice s : all)
						if (eval.gt(s).mightBeTrue())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				}
		}

		if (update == null)
			return environment;
		else if (update.isBottom())
			return environment.bottom();
		else
			return environment.putState(id, update);
	}

}
