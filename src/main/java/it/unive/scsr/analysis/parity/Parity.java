package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	public ParityLattice top() { return ParityLattice.TOP; }

	@Override
	public ParityLattice bottom() { return ParityLattice.BOTTOM; }

	@Override
	public ParityLattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		if(constant.getValue() instanceof Integer i)
			return i % 2 == 0 ? ParityLattice.EVEN : ParityLattice.ODD;

		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice
	evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if(expression.getOperator() instanceof NumericNegation)
			return arg;

		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice
	evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if (left == ParityLattice.TOP || right == ParityLattice.TOP)
			return ParityLattice.TOP;
		if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
			return ParityLattice.BOTTOM;

		if(expression.getOperator() instanceof AdditionOperator
				|| expression.getOperator() instanceof SubtractionOperator) {
			if (left == ParityLattice.EVEN && right == ParityLattice.EVEN || left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.EVEN;
			if (left == ParityLattice.EVEN && right == ParityLattice.ODD || left == ParityLattice.ODD && right == ParityLattice.EVEN)
				return ParityLattice.ODD;
		}
		if (expression.getOperator() instanceof MultiplicationOperator) {
			if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.ODD;
			if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
		}

		return ParityLattice.TOP;
	}
}