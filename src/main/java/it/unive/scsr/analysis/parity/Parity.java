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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	private static final Logger log = LoggerFactory.getLogger(Parity.class);

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
		if(constant.getValue() instanceof Integer){
			Integer value = (Integer) constant.getValue();
			return (value%2==0)? ParityLattice.EVEN : ParityLattice.ODD;
		}
		return ParityLattice.BOTTOM;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if(expression.getOperator() instanceof NumericNegation){
			// Negation preserve the parity
			return arg;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() instanceof AdditionOperator
				|| expression.getOperator() instanceof SubtractionOperator) {
			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;
			if (left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;
			if (left == ParityLattice.EVEN && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			if ((left == ParityLattice.EVEN && right == ParityLattice.ODD) ||
					(left == ParityLattice.ODD && right == ParityLattice.EVEN))
				return ParityLattice.ODD;
			if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.EVEN;

		}
		else if (expression.getOperator() instanceof MultiplicationOperator) {
			if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;
			if (left == ParityLattice.TOP || right == ParityLattice.TOP)
				return ParityLattice.TOP;
			if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.ODD;
		}
		else if (expression.getOperator() instanceof DivisionOperator) {
			// Integer division parity is not easily determined
			// Division result depends on the divisor
			return ParityLattice.TOP;
		}
		else if (expression.getOperator() instanceof ModuloOperator) {
			// Modulo result depends on the divisor
			return ParityLattice.TOP;

		}
		else if (expression.getOperator() instanceof RemainderOperator) {
			// Remainder result depends on the divisor
			return ParityLattice.TOP;
		}

		// For any other operation, return TOP
		return ParityLattice.TOP;
	}
}