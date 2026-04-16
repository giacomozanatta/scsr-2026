package it.unive.scsr.analysis.parity;

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
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (constant.getValue() instanceof Integer i) {
			return (i % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD ;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression,
											 ParityLattice arg,
											 ProgramPoint pp,
	                                         SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() == NumericNegation.INSTANCE) {
			return arg;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression,
											  ParityLattice left,
											  ParityLattice right,
	                                          ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() instanceof AdditionOperator ||
			expression.getOperator() instanceof SubtractionOperator) {
			if (left == ParityLattice.TOP || right == ParityLattice.TOP) {
				return ParityLattice.TOP;
			} else if (left == right) {
				return ParityLattice.EVEN;
			} else if ((left == ParityLattice.EVEN && right == ParityLattice.ODD) ||
				       (left == ParityLattice.ODD && right == ParityLattice.EVEN)) {
				return ParityLattice.ODD;
			} else if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM) {
				return ParityLattice.BOTTOM;
			}
			return ParityLattice.TOP;
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			if (left == ParityLattice.TOP || right == ParityLattice.TOP) {
				return ParityLattice.TOP;
			} else if (left == ParityLattice.EVEN || right == ParityLattice.EVEN) {
				return ParityLattice.EVEN;
			} else if (left == ParityLattice.ODD && right == ParityLattice.ODD) {
				return ParityLattice.ODD;
			} else if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM) {
				return ParityLattice.BOTTOM;
			}
			return ParityLattice.TOP;
		} else if (expression.getOperator() instanceof ModuloOperator ||
				   expression.getOperator() instanceof RemainderOperator) {
			if (left == ParityLattice.EVEN && right == ParityLattice.EVEN) {
				return ParityLattice.EVEN;
			}
			return ParityLattice.TOP;
		}

		return ParityLattice.TOP;
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression,
													ParityLattice left,
													ParityLattice right,
													ProgramPoint pp,
													SemanticOracle oracle) {
		if (left.isTop() || right.isTop())
			return Satisfiability.UNKNOWN;
		BinaryOperator operator = expression.getOperator();
		if (operator == ComparisonEq.INSTANCE) {
			return left.eq(right);
		}
		// Pretty much everything else is unknown since we just know the parity
		// But the functions are useful later
		else if (operator == ComparisonGe.INSTANCE) {
			return left.gt(right).or(left.eq(right));
		} else if (operator == ComparisonGt.INSTANCE) {
			return left.gt(right);
		} else if (operator == ComparisonLe.INSTANCE) {
			return left.gt(right).negate();
		} else if (operator == ComparisonLt.INSTANCE) {
			return (left.gt(right).or(left.eq(right))).negate();
		} else if (operator == ComparisonNe.INSTANCE) {
			return left.eq(right).negate();
		} else {
			return Satisfiability.UNKNOWN;
		}
	}

	@Override
	public ValueEnvironment<ParityLattice> assumeBinaryExpression(
			ValueEnvironment<ParityLattice> environment,
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
		ParityLattice expr_eval;
		boolean rightIsExpr;
		BinaryOperator operator = expression.getOperator();
		ValueExpression left = (ValueExpression) expression.getLeft();
		ValueExpression right = (ValueExpression) expression.getRight();
		if (left instanceof Identifier) {
			expr_eval = eval(environment, right, src, oracle);
			id = (Identifier) left;
			rightIsExpr = true;
		} else if (right instanceof Identifier) {
			expr_eval = eval(environment, left, src, oracle);
			id = (Identifier) right;
			rightIsExpr = false;
		} else
			return environment;

		ParityLattice starting = environment.getState(id);
		if (expr_eval.isBottom() || starting.isBottom()) {
			return environment.bottom();
		}

		ParityLattice update = null;
		if (operator == ComparisonEq.INSTANCE) {
			update = starting.glb(expr_eval);
		} else {
			ParityLattice e = ParityLattice.EVEN;
			ParityLattice o = ParityLattice.ODD;
			if (operator == ComparisonGe.INSTANCE)
				if (rightIsExpr) {
					if (e.gt(expr_eval).or(e.eq(expr_eval)).mightBeTrue()) {
						update = starting.glb(e);
					}
					if (o.gt(expr_eval).or(o.eq(expr_eval)).mightBeTrue()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
				} else {
					if (expr_eval.gt(e).or(expr_eval.eq(e)).mightBeTrue()) {
						update = starting.glb(e);
					}
					if (expr_eval.gt(o).or(expr_eval.eq(o)).mightBeTrue()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
				}
			else if (operator == ComparisonLe.INSTANCE)
				if (rightIsExpr) {
					if (e.gt(expr_eval).mightBeFalse()) {
						update = starting.glb(e);
					}
					if (o.gt(expr_eval).mightBeFalse()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
				} else {
					if (expr_eval.gt(e).mightBeFalse()) {
						update = starting.glb(e);
					}
					if (expr_eval.gt(o).mightBeFalse()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
				}
			else if (operator == ComparisonLt.INSTANCE)
				if (rightIsExpr) {
					if (e.gt(expr_eval).or(e.eq(expr_eval)).mightBeFalse()) {
						update = starting.glb(e);
					}
					if (o.gt(expr_eval).or(o.eq(expr_eval)).mightBeFalse()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
				} else {
					if (expr_eval.gt(e).or(expr_eval.eq(e)).mightBeFalse()) {
						update = starting.glb(e);
					}
					if (expr_eval.gt(o).or(expr_eval.eq(o)).mightBeFalse()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
				}
			else if (operator == ComparisonGt.INSTANCE)
				if (rightIsExpr) {
					if (e.gt(expr_eval).mightBeTrue()) {
						update = starting.glb(e);
					}
					if (o.gt(expr_eval).mightBeTrue()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
				} else {
					if (expr_eval.gt(e).mightBeTrue()) {
						update = starting.glb(e);
					}
					if (expr_eval.gt(o).mightBeTrue()) {
						update = update == null ? starting.glb(o) : update.lub(starting.glb(o));
					}
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