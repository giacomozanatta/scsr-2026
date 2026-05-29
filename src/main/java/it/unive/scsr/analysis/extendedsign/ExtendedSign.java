package it.unive.scsr.analysis.extendedsign;

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
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

    @Override
	public ExtendedSignLattice top() {return ExtendedSignLattice.TOP;}

	@Override
	public ExtendedSignLattice bottom() {return ExtendedSignLattice.BOTTOM;}

	@Override
	public ExtendedSignLattice 
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if(constant.getValue() instanceof Integer) {
			Integer n = (Integer) constant.getValue();
            if (n < 0) return ExtendedSignLattice.LT_ZERO;
            if (n == 0) return ExtendedSignLattice.ZERO;
            if (n > 0) return ExtendedSignLattice.GT_ZERO;
		}
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		if(expression.getOperator() == NumericNegation.INSTANCE) {
			if(arg == ExtendedSignLattice.LT_ZERO) return ExtendedSignLattice.GT_ZERO;
			else if(arg == ExtendedSignLattice.GT_ZERO) return ExtendedSignLattice.LT_ZERO;
			else if(arg == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;
			else if(arg == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;
			else if(arg == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;
			else if(arg == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
			else if(arg == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
			else if(arg == ExtendedSignLattice.NOT_ZERO) return ExtendedSignLattice.NOT_ZERO;
			else return ExtendedSignLattice.TOP;
		}
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if(expression.getOperator() instanceof AdditionOperator) {

            if(left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;

			if(left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;	

			if (left == ExtendedSignLattice.LT_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.LEQ_ZERO) 
                    return ExtendedSignLattice.LT_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.GEQ_ZERO || right == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            if (left == ExtendedSignLattice.ZERO) return right; 

            if (left == ExtendedSignLattice.LEQ_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.LEQ_ZERO) 
                    return ExtendedSignLattice.LEQ_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.GEQ_ZERO || right == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            if (left == ExtendedSignLattice.GT_ZERO) {
                if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.GEQ_ZERO) 
                    return ExtendedSignLattice.GT_ZERO;
                else if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            if (left == ExtendedSignLattice.NOT_ZERO) {
                if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.NOT_ZERO 
                    || right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.GEQ_ZERO)
                    return ExtendedSignLattice.TOP;
                else if (right == ExtendedSignLattice.ZERO)
                    return ExtendedSignLattice.NOT_ZERO;
            }

            if (left == ExtendedSignLattice.GEQ_ZERO) {
                if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.GEQ_ZERO) 
                    return ExtendedSignLattice.GEQ_ZERO;
                else if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            return ExtendedSignLattice.TOP;
		} 

        else if (expression.getOperator() instanceof MultiplicationOperator) {

			if(left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;

			if(left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;	

            if (right == ExtendedSignLattice.ZERO || left == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;

            if (right == ExtendedSignLattice.NOT_ZERO || left == ExtendedSignLattice.NOT_ZERO) return ExtendedSignLattice.NOT_ZERO; 

			if (left == ExtendedSignLattice.LT_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO) return ExtendedSignLattice.GT_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO) return ExtendedSignLattice.LT_ZERO;
                else if (right == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
                else if (right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
            }

            if (left == ExtendedSignLattice.LEQ_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO|| right == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
            }

            if (left == ExtendedSignLattice.GT_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO) return ExtendedSignLattice.LT_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO) return ExtendedSignLattice.GT_ZERO;
                else if (right == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
                else if (right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
            }

            if (left == ExtendedSignLattice.GEQ_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO|| right == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
            }

            return ExtendedSignLattice.TOP;
		}

        else if (expression.getOperator() instanceof SubtractionOperator) {

            if(left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;

		    if(left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;	

			if (left == ExtendedSignLattice.LT_ZERO) {
                if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.GEQ_ZERO) 
                    return ExtendedSignLattice.LT_ZERO;
                else if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.LEQ_ZERO && left == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            if (left == ExtendedSignLattice.ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO) return ExtendedSignLattice.GT_ZERO;
                else if (right == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO) return ExtendedSignLattice.LT_ZERO; 
                else if (right == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
                else if (right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
				else if (right == ExtendedSignLattice.NOT_ZERO) return ExtendedSignLattice.NOT_ZERO;
            }

            if (left == ExtendedSignLattice.LEQ_ZERO) {
                if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.GEQ_ZERO) 
                    return ExtendedSignLattice.LEQ_ZERO;
                else if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            if (left == ExtendedSignLattice.GT_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.LEQ_ZERO) 
                    return ExtendedSignLattice.GT_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.GEQ_ZERO || right == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            if (left == ExtendedSignLattice.NOT_ZERO) {
                if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.NOT_ZERO 
                    || right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.GEQ_ZERO)
                    return ExtendedSignLattice.TOP;
                else if (right == ExtendedSignLattice.ZERO)
                    return ExtendedSignLattice.NOT_ZERO;
            }
            
            if (left == ExtendedSignLattice.GEQ_ZERO) {
                if (right == ExtendedSignLattice.LT_ZERO || right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.LEQ_ZERO) 
                    return ExtendedSignLattice.GEQ_ZERO;
                else if (right == ExtendedSignLattice.GT_ZERO || right == ExtendedSignLattice.GEQ_ZERO || right == ExtendedSignLattice.NOT_ZERO) 
                    return ExtendedSignLattice.TOP;
            }

            return ExtendedSignLattice.TOP;

		} else if (expression.getOperator() instanceof DivisionOperator) {

			if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;
			if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;	
            if (right == ExtendedSignLattice.ZERO) return ExtendedSignLattice.TOP;
            if (left == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;
            if (right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.TOP; 
            if (right == ExtendedSignLattice.NOT_ZERO || left == ExtendedSignLattice.NOT_ZERO) return ExtendedSignLattice.TOP;

            if (right == ExtendedSignLattice.LT_ZERO) {
                if (left == ExtendedSignLattice.LT_ZERO || left == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
                else if (left == ExtendedSignLattice.GT_ZERO || left == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
            }
            if (right == ExtendedSignLattice.GT_ZERO) {
                if (left == ExtendedSignLattice.LT_ZERO || left == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
                else if (left == ExtendedSignLattice.GT_ZERO || left == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
            }
            return ExtendedSignLattice.TOP;

		} 
        else if (expression.getOperator() instanceof ModuloOperator) {

            if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;
            if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;

            if (left == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;
            if (right == ExtendedSignLattice.ZERO) return ExtendedSignLattice.TOP;
            if (right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.TOP;

            if (right == ExtendedSignLattice.NOT_ZERO) return ExtendedSignLattice.TOP;

            if (right == ExtendedSignLattice.LT_ZERO) return ExtendedSignLattice.LEQ_ZERO;
            if (right == ExtendedSignLattice.GT_ZERO) return ExtendedSignLattice.GEQ_ZERO;

            return ExtendedSignLattice.TOP;
        }

		else if (expression.getOperator() instanceof RemainderOperator) {
			if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;
            if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;

            if (left == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;
            if (right == ExtendedSignLattice.ZERO) return ExtendedSignLattice.BOTTOM;

            if (right == ExtendedSignLattice.LEQ_ZERO || right == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.TOP;
            if (left == ExtendedSignLattice.NOT_ZERO) return ExtendedSignLattice.TOP;

            if (left == ExtendedSignLattice.LT_ZERO || left == ExtendedSignLattice.LEQ_ZERO) return ExtendedSignLattice.LEQ_ZERO;
            if (left == ExtendedSignLattice.GT_ZERO || left == ExtendedSignLattice.GEQ_ZERO) return ExtendedSignLattice.GEQ_ZERO;
		
		return ExtendedSignLattice.TOP;
        }

        return ExtendedSignLattice.TOP;
	}
	
	// Some information can be inferred also checking boolean expression and guards!
	// a = T, b = +; b < a means that  a = + 
	// These behaviors can be handeled with the following implementations
	
	@Override
	public Satisfiability satisfiesBinaryExpression( BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) {
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
	public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(
			ValueEnvironment<ExtendedSignLattice> environment,
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
		ExtendedSignLattice eval;
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

		ExtendedSignLattice starting = environment.getState(id);
		if (eval.isBottom() || starting.isBottom())
			return environment.bottom();

		ExtendedSignLattice update = null;
		if (operator == ComparisonEq.INSTANCE)
			update = starting.glb(eval);
		else {
			// the rule for an operator op is:
			// - if `start op eval`, `update = U { start n v | v op eval, v in {
			// +, 0, -} }`
			// - if `eval op start`, `update = U { start n v | eval op v, v in {
			// +, 0, -} }`

			ExtendedSignLattice[] all = new ExtendedSignLattice[] { ExtendedSignLattice.LT_ZERO, ExtendedSignLattice.ZERO, ExtendedSignLattice.GT_ZERO,
                    ExtendedSignLattice.LEQ_ZERO, ExtendedSignLattice.GEQ_ZERO, ExtendedSignLattice.NOT_ZERO };
			if (operator == ComparisonGe.INSTANCE)
				if (rightIsExpr) {
					for (ExtendedSignLattice s : all)
						if (s.gt(eval).or(s.eq(eval)).mightBeTrue())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (ExtendedSignLattice s : all)
						if (eval.gt(s).or(eval.eq(s)).mightBeTrue())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				}
			else if (operator == ComparisonLe.INSTANCE)
				if (rightIsExpr) {
					for (ExtendedSignLattice s : all)
						// we invert <= to > and look at the failing ones
						if (s.gt(eval).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (ExtendedSignLattice s : all)
						// we invert <= to > and look at the failing ones
						if (eval.gt(s).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				}
			else if (operator == ComparisonLt.INSTANCE)
				if (rightIsExpr) {
					for (ExtendedSignLattice s : all)
						// we invert < to >= and look at the failing ones
						if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (ExtendedSignLattice s : all)
						// we invert < to >= and look at the failing ones
						if (eval.gt(s).or(eval.eq(s)).mightBeFalse())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				}
			else if (operator == ComparisonGt.INSTANCE)
				if (rightIsExpr) {
					for (ExtendedSignLattice s : all)
						if (s.gt(eval).mightBeTrue())
							update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
				} else {
					for (ExtendedSignLattice s : all)
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
