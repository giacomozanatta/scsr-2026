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
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (constant.getValue() instanceof Integer val) {
			return (val % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD;
		}
		return top();
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return (expression.getOperator() instanceof NumericNegation) ? arg : top();
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (left.isTop() || right.isTop()) return top();
		if (left.isBottom() || right.isBottom()) return bottom();

		var op = expression.getOperator();

		if (op instanceof AdditionOperator || op instanceof SubtractionOperator) {
			if (left.equals(right)) return ParityLattice.EVEN; // E+E=E, O+O=E
			return ParityLattice.ODD; // E+O=O, O+E=O
		}

		if (op instanceof MultiplicationOperator) {
			if (left.equals(ParityLattice.EVEN) || right.equals(ParityLattice.EVEN))
				return ParityLattice.EVEN;
			return ParityLattice.ODD;
		}

		return top();
	}
}