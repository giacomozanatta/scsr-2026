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

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	public ParityLattice top() {
		// TODO: homework
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		// TODO: homework
		return ParityLattice.BOTTOM;
	}

	//TODO add missing components

	@Override
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if(constant.getValue() instanceof Integer value){
			if(value % 2 == 0) return ParityLattice.EVEN;
			return ParityLattice.ODD;
		}
		return ParityLattice.TOP;
	}

	// negating the value does not change its parity (6 % 2 == -6%2 == 0)
	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return arg;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if(left.isTop() || right.isTop()) return ParityLattice.TOP;
		BinaryOperator op = expression.getOperator();
		if(op instanceof AdditionOperator || op instanceof SubtractionOperator){
			if(left == ParityLattice.EVEN && right == ParityLattice.EVEN){
				return ParityLattice.EVEN;
			}
			return ParityLattice.ODD;
		}
		if(op instanceof MultiplicationOperator){
			if(left == ParityLattice.EVEN || right == ParityLattice.EVEN){
				return ParityLattice.EVEN;
			}
			return ParityLattice.ODD;
		}
		return ParityLattice.TOP;
	}
}