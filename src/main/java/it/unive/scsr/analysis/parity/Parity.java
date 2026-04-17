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
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;

//Work done by Elia Stevanato 895598 and Francesco Pasqualato 897778
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
			int n = (Integer) constant.getValue();

			if (n % 2 == 0)
				return ParityLattice.EVEN;
			else
				return ParityLattice.ODD;
		}

		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg,
                                             ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return arg;
	}

	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() instanceof AdditionOperator) {

			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;

			if (left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;

			if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;

			if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.EVEN;

			return ParityLattice.ODD;
		}
		else if (expression.getOperator() instanceof SubtractionOperator) {

			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;

			if (left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;

			if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;

			if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.EVEN;

			return ParityLattice.ODD;
		}
		else if (expression.getOperator() instanceof MultiplicationOperator) {

			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;

			if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;

			if (left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;

			return ParityLattice.ODD;
		}
		else if (expression.getOperator() instanceof DivisionOperator) {

			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;

			if (right == ParityLattice.EVEN)
				return ParityLattice.TOP;

			if (left == ParityLattice.EVEN && right == ParityLattice.ODD)
				return ParityLattice.EVEN;

			if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.ODD;

			return ParityLattice.TOP;
		}
		return ParityLattice.TOP;
	}



}