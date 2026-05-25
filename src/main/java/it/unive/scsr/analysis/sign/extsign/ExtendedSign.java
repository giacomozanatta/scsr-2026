package it.unive.scsr.analysis.sign.extsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice>{

	@Override
	public ExtendedSignLattice top() {
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return ExtendedSignLattice.BOTTOM;
	}

	@Override
	public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if(constant.getValue() instanceof Integer n) {
			if(n == 0)
				return ExtendedSignLattice.ZERO;
			else if(n > 0)
				return ExtendedSignLattice.POS;

			return ExtendedSignLattice.NEG;
		}
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression,
												   ExtendedSignLattice arg,
												   ProgramPoint pp,
												   SemanticOracle oracle) throws SemanticException {

		if(expression.getOperator() == NumericNegation.INSTANCE) {
			return evalNegate(arg);
		}

		return ExtendedSignLattice.TOP;
	}

	private ExtendedSignLattice evalNegate(ExtendedSignLattice arg){
			if(arg == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.POS;

			else if(arg == ExtendedSignLattice.POS)
				return ExtendedSignLattice.NEG;

			else if(arg == ExtendedSignLattice.ZERO)
				return ExtendedSignLattice.ZERO;

			else if(arg == ExtendedSignLattice.NEGZERO)
				return ExtendedSignLattice.POSZERO;

			else if(arg == ExtendedSignLattice.POSZERO)
				return ExtendedSignLattice.NEGZERO;

			else if(arg == ExtendedSignLattice.NOTZERO)
				return ExtendedSignLattice.NOTZERO;

			else if(arg == ExtendedSignLattice.TOP)
				return ExtendedSignLattice.TOP;

			return ExtendedSignLattice.BOTTOM;
	}

	// -----------------  BINARY OPERATIONS ---------------------------------

	@Override
	public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression,
													ExtendedSignLattice left,
													ExtendedSignLattice right,
													ProgramPoint pp,
													SemanticOracle oracle) throws SemanticException {

		if(expression.getOperator() instanceof AdditionOperator) {
			return evalAdd(left, right);
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			return evalMul(left, right);
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			return evalSub(left, right);
		} else if (expression.getOperator() instanceof DivisionOperator) {
			return evalDiv(left, right);
		} else if (expression.getOperator() instanceof ModuloOperator)
			return right;
		else if (expression.getOperator() instanceof RemainderOperator)
			return left;

		return ExtendedSignLattice.TOP;
	}

	private ExtendedSignLattice evalAdd(ExtendedSignLattice l, ExtendedSignLattice r){
		if(l == ExtendedSignLattice.BOTTOM || r == ExtendedSignLattice.BOTTOM)
			return ExtendedSignLattice.BOTTOM;

		if(l == ExtendedSignLattice.ZERO) return r;
		if(r == ExtendedSignLattice.ZERO) return l;

		if(l == ExtendedSignLattice.POS && r == ExtendedSignLattice.POS) return ExtendedSignLattice.POS;
		if(l == ExtendedSignLattice.NEG && r == ExtendedSignLattice.NEG) return ExtendedSignLattice.NEG;
		if(l == ExtendedSignLattice.POSZERO && r == ExtendedSignLattice.POSZERO) return ExtendedSignLattice.POSZERO;
		if(l == ExtendedSignLattice.NEGZERO && r == ExtendedSignLattice.NEGZERO) return ExtendedSignLattice.NEGZERO;

		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.POS, ExtendedSignLattice.POSZERO)) return ExtendedSignLattice.POS;
		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.NEG, ExtendedSignLattice.NEGZERO)) return ExtendedSignLattice.NEG;

		// All remaining cases like POS + NEG; POSZERO + NEGZERO; cases involving TOP or NOTZERO...
		return ExtendedSignLattice.TOP;
	}

	private ExtendedSignLattice evalMul(ExtendedSignLattice l, ExtendedSignLattice r){
		if(l == ExtendedSignLattice.BOTTOM || r == ExtendedSignLattice.BOTTOM)
			return ExtendedSignLattice.BOTTOM;

		// 0 * x = 0
		if(l == ExtendedSignLattice.ZERO || r == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;

		// positive * negative = negative
		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.POS, ExtendedSignLattice.NEG)) return ExtendedSignLattice.NEG;
		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.POSZERO, ExtendedSignLattice.NEG)) return ExtendedSignLattice.NEGZERO;
		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.POS, ExtendedSignLattice.NEGZERO)) return ExtendedSignLattice.NEGZERO;
		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.POSZERO, ExtendedSignLattice.NEGZERO)) return ExtendedSignLattice.NEGZERO;

		// pos * pos = pos
		if(l == ExtendedSignLattice.POS && r == ExtendedSignLattice.POS) return ExtendedSignLattice.POS;
		if(l == ExtendedSignLattice.POSZERO && r == ExtendedSignLattice.POSZERO) return ExtendedSignLattice.POSZERO;
		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.POSZERO, ExtendedSignLattice.POS)) return ExtendedSignLattice.POSZERO;

		// neg * neg = pos
		if(l == ExtendedSignLattice.NEG && r == ExtendedSignLattice.NEG) return ExtendedSignLattice.POS;
		if(l == ExtendedSignLattice.NEGZERO && r == ExtendedSignLattice.NEGZERO) return ExtendedSignLattice.POSZERO;
		if(ExtendedSignLattice.pair(l, r, ExtendedSignLattice.NEGZERO, ExtendedSignLattice.NEG)) return ExtendedSignLattice.NEGZERO;

		// both non zero -> result is not zero and that's the most we can know
		if(l == ExtendedSignLattice.NOTZERO && r == ExtendedSignLattice.NOTZERO) return ExtendedSignLattice.NOTZERO;

		// All other cases : unknown
		return ExtendedSignLattice.TOP;
	}

	private ExtendedSignLattice evalSub(ExtendedSignLattice l, ExtendedSignLattice r){
		return evalAdd(l, evalNegate(r));  // a - b  ==  a + (-b)
	}

	private ExtendedSignLattice evalDiv(ExtendedSignLattice l, ExtendedSignLattice r){
		if(l == ExtendedSignLattice.BOTTOM || r == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;

		// division by zero
		if(r == ExtendedSignLattice.ZERO) return ExtendedSignLattice.BOTTOM;

		// might divide by zero
		if(r == ExtendedSignLattice.POSZERO || r == ExtendedSignLattice.NEGZERO) return ExtendedSignLattice.TOP;

		// 0 divided = 0
		if(l == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;

		// cases where divisor might be zero (POSZERO OR NEGZERO) have already been eliminated by now when reaching this

		// pos/neg or neg/pos => neg or zero (because integers might be truncated towards 0)
		// only dividend might still be POSZERO or NEGZERO
		if(ExtendedSignLattice.pair(l,r, ExtendedSignLattice.POS, ExtendedSignLattice.NEG)) return ExtendedSignLattice.NEGZERO;
		if(l == ExtendedSignLattice.POSZERO && r == ExtendedSignLattice.NEG) return ExtendedSignLattice.NEGZERO;
		if(l == ExtendedSignLattice.NEGZERO && r == ExtendedSignLattice.POS) return ExtendedSignLattice.NEGZERO;

		// same sign for dividend / divisor => POSZERO (because integers might be floored to 0)
		// only dividend might still be POSZERO or NEGZERO
		if(l == ExtendedSignLattice.POS && r == ExtendedSignLattice.POS) return ExtendedSignLattice.POSZERO;
		if(l == ExtendedSignLattice.NEG && r == ExtendedSignLattice.NEG) return ExtendedSignLattice.POSZERO;
		if(l == ExtendedSignLattice.POSZERO && r == ExtendedSignLattice.POS) return ExtendedSignLattice.POSZERO;
		if(l == ExtendedSignLattice.NEGZERO && r == ExtendedSignLattice.NEG) return ExtendedSignLattice.POSZERO;

		// all other cases, including with NOTZERO, are unknown
		return ExtendedSignLattice.TOP;
	}


	// Some information can be inferred also checking boolean expression and guards!
	// a = T, b = +; b < a means that  a = +
	// These behaviors can be handled with the following implementations

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression,
													ExtendedSignLattice left,
													ExtendedSignLattice right,
													ProgramPoint pp,
													SemanticOracle oracle) throws SemanticException {

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
	public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignLattice> environment,
																		BinaryExpression expression,
																		ProgramPoint src,
																		ProgramPoint dest,
																		SemanticOracle oracle) throws SemanticException {

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

			ExtendedSignLattice[] all = new ExtendedSignLattice[] { ExtendedSignLattice.NEG, ExtendedSignLattice.ZERO,
					ExtendedSignLattice.POS, ExtendedSignLattice.POSZERO, ExtendedSignLattice.NEGZERO, ExtendedSignLattice.NOTZERO};

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
