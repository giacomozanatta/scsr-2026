package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericAbs;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {


	private ExtendedSignLattice negateSign(ExtendedSignLattice l) {
		if (l == ExtendedSignLattice.POS)
			return ExtendedSignLattice.NEG;
		if (l == ExtendedSignLattice.POSZERO)
			return ExtendedSignLattice.NEGZERO;
		if (l == ExtendedSignLattice.NEGZERO)
			return ExtendedSignLattice.POSZERO;
		if (l == ExtendedSignLattice.NEG)
			return ExtendedSignLattice.POS;
		// If none of the above, keep same sign
		return l;
	}

	private ExtendedSignLattice evalAdditionOperator(ExtendedSignLattice left, ExtendedSignLattice right) {
		/*
			// Not necessary, any case not specified defaults to TOP
			if (
					left.isTop() || right.isTop()
							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG
							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEGZERO
							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NONZERO
							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.NEG
							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.NEGZERO
							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.NONZERO
							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NONZERO
							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.NONZERO
			)
				return ExtendedSignLattice.TOP;*/

		if (left == ExtendedSignLattice.ZERO)
			return right;
		if (right == ExtendedSignLattice.ZERO)
			return left;

		if (
				left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS
//							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.ZERO
						|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POSZERO
//							|| left == ExtendedSignLattice.ZERO && right == ExtendedSignLattice.POS
						|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.POS
		)
			return ExtendedSignLattice.POS;

		if (
				left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.POSZERO
//							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.ZERO
//							|| left == ExtendedSignLattice.ZERO && right == ExtendedSignLattice.POSZERO
		)
			return ExtendedSignLattice.POSZERO;

//			if (
//					left == ExtendedSignLattice.ZERO && right == ExtendedSignLattice.ZERO
//			)
//				return ExtendedSignLattice.ZERO;

		if (
				left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.NEGZERO
//							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.ZERO
//							|| left == ExtendedSignLattice.ZERO && right == ExtendedSignLattice.NEGZERO
		)
			return ExtendedSignLattice.NEGZERO;

		if (
				left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG
//							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.ZERO
						|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEGZERO
//							|| left == ExtendedSignLattice.ZERO && right == ExtendedSignLattice.NEG
						|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.NEG
		)
			return ExtendedSignLattice.NEG;

		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice top() {
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return ExtendedSignLattice.BOTTOM;
	}

	@Override
	public ExtendedSignLattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (constant.getValue() instanceof Number) {
			double n = ((Number) constant.getValue()).doubleValue();
			if (n == 0.)
				return ExtendedSignLattice.ZERO;
			else if (n > 0.)
				return ExtendedSignLattice.POS;
			return ExtendedSignLattice.NEG;
		}

		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalUnaryExpression(
			UnaryExpression expression, ExtendedSignLattice arg,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			return negateSign(arg);
		}

		if (expression.getOperator() == NumericAbs.INSTANCE) {
			if (arg == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.POS;
			else if (arg == ExtendedSignLattice.NEGZERO)
				return ExtendedSignLattice.POSZERO;
			else
				return arg;
		}

		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right,
	                                                ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (expression.getOperator() instanceof AdditionOperator) {
			return evalAdditionOperator(left, right);
		} else if (expression.getOperator() instanceof MultiplicationOperator) {

			if (
					left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO
			)
				return ExtendedSignLattice.ZERO;

			if (
					left.isTop() && right != ExtendedSignLattice.ZERO
							|| left != ExtendedSignLattice.ZERO && right.isTop()
			)
				return ExtendedSignLattice.TOP;

			/*
			// Not necessary, any case not specified defaults to TOP
			if (
					left.isTop() || right.isTop()
			)
				return ExtendedSignLattice.TOP;*/

			if (
					left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG
			)
				return ExtendedSignLattice.POS;

			if (
					left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.NEG
							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.NEGZERO
							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEGZERO
			)
				return ExtendedSignLattice.POSZERO;

			if (
					left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.NEG
							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.NEGZERO
							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEGZERO
			)
				return ExtendedSignLattice.NEGZERO;

			if (
					left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG
			)
				return ExtendedSignLattice.NEG;

			if (
					left == ExtendedSignLattice.NONZERO && right.lessOrEqual(ExtendedSignLattice.NONZERO)
							|| left.lessOrEqual(ExtendedSignLattice.NONZERO) && right == ExtendedSignLattice.NONZERO
			)
				return ExtendedSignLattice.NONZERO;

		} else if (expression.getOperator() instanceof SubtractionOperator) {
			return evalAdditionOperator(left, negateSign(right));
		} else if (expression.getOperator() instanceof DivisionOperator) {
			/*
			// Not necessary, any case not specified defaults to TOP
			if (
					left.isTop() || right.isTop()
			)
				return ExtendedSignLattice.TOP;*/

			if (ExtendedSignLattice.ZERO.lessOrEqual(right))
				return ExtendedSignLattice.BOTTOM;

			if (left == ExtendedSignLattice.ZERO)
				return ExtendedSignLattice.ZERO;

			if (
					left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG
			)
				return ExtendedSignLattice.POS;

			if (
					left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.POS
//							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.POSZERO
//							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.NEG
//							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.NEGZERO
//							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEGZERO
			)
				return ExtendedSignLattice.POSZERO;

			if (
					left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.POS
//							|| left == ExtendedSignLattice.NEGZERO && right == ExtendedSignLattice.POSZERO
//							|| left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POSZERO
							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.NEG
//							|| left == ExtendedSignLattice.POSZERO && right == ExtendedSignLattice.NEGZERO
//							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEGZERO
			)
				return ExtendedSignLattice.NEGZERO;

			if (
					left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS
							|| left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG
			)
				return ExtendedSignLattice.NEG;

			if (
					left == ExtendedSignLattice.NONZERO && right.lessOrEqual(ExtendedSignLattice.NONZERO)
							|| left.lessOrEqual(ExtendedSignLattice.NONZERO) && right == ExtendedSignLattice.NONZERO
			)
				return ExtendedSignLattice.NONZERO;
		} else if (expression.getOperator() instanceof ModuloOperator) {
			if (ExtendedSignLattice.ZERO.lessOrEqual(right))
				return ExtendedSignLattice.BOTTOM;
			return right;
		} else if (expression.getOperator() instanceof RemainderOperator) {
			if (ExtendedSignLattice.ZERO.lessOrEqual(right))
				return ExtendedSignLattice.BOTTOM;
			return left;
		}

		return ExtendedSignLattice.TOP;
	}



	// Copied from Sign
	@Override
	public Satisfiability satisfiesBinaryExpression(
			BinaryExpression expression,
			ExtendedSignLattice left,
			ExtendedSignLattice right,
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
			return left.gt(right).negate();
		else if (operator == ComparisonLt.INSTANCE)
			return left.gt(right).negate().and(left.eq(right).negate());
		else if (operator == ComparisonNe.INSTANCE)
			return left.eq(right).negate();
		else
			return Satisfiability.UNKNOWN;
	}

	// Copied from Sign, added the new possible values (line 373)
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

			ExtendedSignLattice[] all = new ExtendedSignLattice[]{ExtendedSignLattice.NEG, ExtendedSignLattice.NEGZERO,
					ExtendedSignLattice.ZERO, ExtendedSignLattice.NONZERO, ExtendedSignLattice.POSZERO, ExtendedSignLattice.POS};
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
