package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.scsr.analysis.sign.SignLattice;

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	    public ParityLattice top() {
			return ParityLattice.TOP;
	    }

	    @Override
	    public ParityLattice bottom() {
			return ParityLattice.BTM;
	    }

	@Override
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (constant.getValue() instanceof Integer i) {
			return (i % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() == NumericNegation.INSTANCE)
			return arg;
		else
			return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		BinaryOperator operator = expression.getOperator();
		if(left == ParityLattice.BTM || right == ParityLattice.BTM)
			return ParityLattice.BTM;
		else if (left == ParityLattice.TOP || right == ParityLattice.TOP)
			return ParityLattice.TOP;
		else if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator)
			return left.equals(right) ? ParityLattice.EVEN : ParityLattice.ODD;
		else if (operator instanceof MultiplicationOperator)
			return left == ParityLattice.EVEN || right == ParityLattice.EVEN ? ParityLattice.EVEN : ParityLattice.ODD;
		else
			return ParityLattice.TOP;
	}



	/*
	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return BaseNonRelationalValueDomain.super.satisfiesBinaryExpression(expression, left, right, pp, oracle);
	}

	@Override
	public ValueEnvironment<ParityLattice> assumeBinaryExpression(ValueEnvironment<ParityLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
		return BaseNonRelationalValueDomain.super.assumeBinaryExpression(environment, expression, src, dest, oracle);
	}
	*/

	//TODO add missing components
}