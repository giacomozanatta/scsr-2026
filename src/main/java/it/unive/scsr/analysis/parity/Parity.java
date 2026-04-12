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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	public ParityLattice top() {
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		return ParityLattice.BOTTOM;
	}

	@Override
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) {
		if (constant.getValue() instanceof Integer n) {
			return (n & 1) == 0 ? ParityLattice.EVEN : ParityLattice.ODD;
		}
		return top();
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		// negation does not change parity
		if (expression.getOperator() instanceof NumericNegation) {
			return arg;
		}
		return top();
	}

	@Override

	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (left.isTop() || right.isTop()) {
			return top();
		}

		if (expression.getOperator() instanceof AdditionOperator
				|| expression.getOperator() instanceof SubtractionOperator) {
			// EVEN +- EVEN = EVEN, ODD +- ODD = EVEN, EVEN +- ODD = ODD
			return left.equals(right) ? ParityLattice.EVEN : ParityLattice.ODD;
		}

		if (expression.getOperator() instanceof MultiplicationOperator) {
			// EVEN * anything = EVEN, ODD * ODD = ODD
			if (left.equals(ParityLattice.EVEN) || right.equals(ParityLattice.EVEN)) {
				return ParityLattice.EVEN;
			}
			return ParityLattice.ODD;
		}
		//division is more complex, we return top since we cannot be sure of the result
        return top();
	}
}