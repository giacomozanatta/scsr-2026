package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericAbs;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

	private static final Logger LOG = LogManager.getLogger(Parity.class);

	@Override
	public ParityLattice top() {
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		return ParityLattice.BOTTOM;
	}

	@Override
	public ParityLattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (constant.getValue() instanceof Integer n) {
			if (n % 2 == 0)
				return ParityLattice.EVEN;
			return ParityLattice.ODD;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(
			UnaryExpression expression,
			ParityLattice arg,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() == NumericNegation.INSTANCE)
			return arg;
		else if (expression.getOperator() == NumericAbs.INSTANCE)
			return arg;
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(
			BinaryExpression expression,
			ParityLattice left,
			ParityLattice right,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		// Probably unnecessary but not sure
		if (left == null || right == null)
			return ParityLattice.BOTTOM;

		// TOP propagation
		if (left.isTop() || right.isTop())
			return ParityLattice.TOP;

		// Redundant, handled by parent class
		//if (left.isBottom() || right.isBottom())
		//	return ParityLattice.BOTTOM;

		if (expression.getOperator() instanceof AdditionOperator) {
			// Same as SubtractionOperator
			if (left == right)
				return ParityLattice.EVEN;
			else
				return ParityLattice.ODD;
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			else
				return ParityLattice.ODD;
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			// Same as AdditionOperator
			if (left == right)
				return ParityLattice.EVEN;
			else
				return ParityLattice.ODD;
		} else if (expression.getOperator() instanceof DivisionOperator) {
			return ParityLattice.TOP;
		} else if (expression.getOperator() instanceof ModuloOperator) {
			// Same as RemainderOperator
			if (right == ParityLattice.EVEN)
				return left;
			return ParityLattice.TOP;
		} else if (expression.getOperator() instanceof RemainderOperator) {
			// Same as ModuloOperator
			if (right == ParityLattice.EVEN)
				return left;
			return ParityLattice.TOP;
		}

		return ParityLattice.TOP;
	}

}