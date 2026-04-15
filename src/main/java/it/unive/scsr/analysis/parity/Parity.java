package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	public ParityLattice top() {return ParityLattice.top;}

	@Override
	public ParityLattice bottom() {return ParityLattice.bottom;}

	@Override
	
	public ParityLattice evalUnaryExpression(
			UnaryExpression expression,
			ParityLattice arg,
			ProgramPoint pp,
			SemanticOracle oracle)
			throws SemanticException {
		if (expression.getOperator() instanceof NumericNegation) return arg;
		return arg.top();
	}


	@Override
	public ParityLattice evalBinaryExpression(
			BinaryExpression expression,
			ParityLattice left,
			ParityLattice right,
			ProgramPoint pp,
			SemanticOracle oracle)
			throws SemanticException {
		if (left.getParity().equals(ParityLattice.top.getParity()) || right.getParity().equals(ParityLattice.top.getParity())) return ParityLattice.top;

		if (expression.getOperator() instanceof AdditionOperator || expression.getOperator() instanceof SubtractionOperator) {
			if (left.equals(ParityLattice.even) && right.equals(ParityLattice.even)) return ParityLattice.even;
			if (left.equals(ParityLattice.odd) && right.equals(ParityLattice.odd)) return ParityLattice.even;
			if ((left.equals(ParityLattice.even) && right.equals(ParityLattice.odd)) || (left.equals(ParityLattice.odd) && right.equals(ParityLattice.even))) return ParityLattice.odd;
		}
		if (expression.getOperator() instanceof MultiplicationOperator) {
			if (left.equals(ParityLattice.even) || right.equals(ParityLattice.even)) return ParityLattice.even;
			if (left.equals(ParityLattice.odd) && right.equals(ParityLattice.odd)) return ParityLattice.odd;
		}
		return top();
	}

	@Override
	public ParityLattice evalConstant(Constant constant,ProgramPoint pp,SemanticOracle oracle) throws SemanticException {
		Object value = constant.getValue();
		if (value instanceof Integer) {
		if (((Integer) value) % 2 == 0) return ParityLattice.even; else return ParityLattice.odd;
		}
		return ParityLattice.top; 
}
		}