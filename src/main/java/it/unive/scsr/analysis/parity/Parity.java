package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
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
		if (constant.getValue() instanceof Integer) {
			int value = (Integer) constant.getValue();
			return value % 2 == 0 ? ParityLattice.EVEN : ParityLattice.ODD;
		}

		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		if (arg == ParityLattice.BOTTOM)
			return ParityLattice.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE)
			return arg;

		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
			return ParityLattice.BOTTOM;

		if (expression.getOperator() instanceof AdditionOperator
				|| expression.getOperator() instanceof SubtractionOperator) {
			if (left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;
			return left == right ? ParityLattice.EVEN : ParityLattice.ODD;
		}

		if (expression.getOperator() instanceof MultiplicationOperator) {
			if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.ODD;
			return ParityLattice.TOP;
		}

		if (expression.getOperator() instanceof DivisionOperator) {
			if (right == ParityLattice.EVEN)
				return ParityLattice.TOP;
			if (right == ParityLattice.ODD)
				return left;
			return ParityLattice.TOP;
		}

		if (expression.getOperator() instanceof ModuloOperator
				|| expression.getOperator() instanceof RemainderOperator) {
			if (right == ParityLattice.ODD)
				return left;
			if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			return ParityLattice.TOP;
		}

		return ParityLattice.TOP;
	}
}
