package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
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
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if (constant.getValue() instanceof Integer val)
			return ((val & 1) == 0) ? ParityLattice.EVEN : ParityLattice.ODD;

		return top();
	}

	@Override
	public ParityLattice evalUnaryExpression(
			UnaryExpression expression,
			ParityLattice arg,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		return (expression.getOperator() instanceof NumericNegation) ? arg : top();
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
			return bottom();

		BinaryOperator op = expression.getOperator();

		if (op instanceof AdditionOperator || op instanceof SubtractionOperator) {
			if (left == ParityLattice.EVEN)
				return right;

			if (left == ParityLattice.ODD) {
				if (right == ParityLattice.ODD)
					return ParityLattice.EVEN;

				if (right == ParityLattice.EVEN)
					return ParityLattice.ODD;
			}
		}

		if (op instanceof MultiplicationOperator) {
			if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;

			if (left == ParityLattice.ODD)
				return right;
		}

		if (op instanceof ModuloOperator || op instanceof RemainderOperator) {
			if (right == ParityLattice.EVEN)
				return left;
		}

		return top();
	}

	// @Override
	// public Satisfiability satisfiesBinaryExpression(
	// BinaryExpression expression,
	// ParityLattice left,
	// ParityLattice right,
	// ProgramPoint pp,
	// SemanticOracle oracle) {

	// if (left.isBottom() || right.isBottom())
	// return Satisfiability.BOTTOM;

	// BinaryOperator op = expression.getOperator();

	// if (op instanceof ComparisonEq)
	// return left.eq(right);

	// if (op instanceof ComparisonNe)
	// return left.eq(right).negate();

	// return Satisfiability.UNKNOWN;

	// }

	// @Override
	// public ValueEnvironment<ParityLattice> assumeBinaryExpression(
	// ValueEnvironment<ParityLattice> environment,
	// BinaryExpression expression,
	// ProgramPoint src,
	// ProgramPoint dest,
	// SemanticOracle oracle)
	// throws SemanticException {

	// BinaryOperator op = expression.getOperator();

	// if (!(op instanceof ComparisonEq || op instanceof ComparisonNe))
	// return environment;

	// ParityLattice evalued;

	// ValueExpression left = ((ValueExpression) expression.getLeft());
	// ValueExpression right = ((ValueExpression) expression.getRight());

	// if (left instanceof Identifier identifier) {
	// ParityLattice rval = eval(environment, right, src, oracle);

	// if (rval == ParityLattice.EVEN || rval == ParityLattice.ODD) {

	// if (op instanceof ComparisonEq)
	// evalued = rval;
	// else
	// evalued = (rval == ParityLattice.EVEN) ? ParityLattice.ODD :
	// ParityLattice.EVEN;

	// environment = environment.putState(identifier,
	// environment.getState(identifier).glb(evalued));

	// }
	// }

	// if (right instanceof Identifier identifier) {
	// ParityLattice lval = eval(environment, left, src, oracle);

	// if (lval == ParityLattice.EVEN || lval == ParityLattice.ODD) {

	// if (op instanceof ComparisonEq)
	// evalued = lval;

	// else
	// evalued = (lval == ParityLattice.EVEN) ? ParityLattice.ODD :
	// ParityLattice.EVEN;

	// environment = environment.putState(identifier,
	// environment.getState(identifier).glb(evalued));
	// }
	// }

	// return environment;
	// }
}