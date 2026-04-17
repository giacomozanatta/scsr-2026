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
	public ParityLattice top() {
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		return ParityLattice.BOTTOM;
	}

	@Override
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if(constant.getValue() instanceof Integer n) {
			return n % 2 == 0 ? ParityLattice.EVEN : ParityLattice.ODD;
		}
		return top();
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() instanceof NumericNegation)
			return arg;
		return top();
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		var op = expression.getOperator();
		if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
			return ParityLattice.BOTTOM;

		if(op instanceof AdditionOperator || op instanceof SubtractionOperator) {
			if(left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;
			return (left == right) ? ParityLattice.EVEN: ParityLattice.ODD;
		}

		else if (op instanceof MultiplicationOperator) {
			if(left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			if(left == ParityLattice.ODD  && right == ParityLattice.ODD)
				return  ParityLattice.ODD;
			if(left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;
		}

		else if (op instanceof DivisionOperator) {
			return ParityLattice.TOP;
		}

		else if (op instanceof ModuloOperator || op instanceof RemainderOperator){
			if(right == ParityLattice.EVEN)
				return left;
			if (right == ParityLattice.ODD)
				return ParityLattice.TOP;
			if(left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;
		}

		return ParityLattice.TOP;

	}
}