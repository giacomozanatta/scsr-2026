package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;

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

    	if (constant.getValue() instanceof Integer val) {
        
        	if (val % 2 == 0) {
            	return ParityLattice.EVEN;
        	} 
        	else {
            	return ParityLattice.ODD;
        	}
    	}

    	return top();
	}

	@Override
	public ParityLattice evalUnaryExpression(
			UnaryExpression expression,
			ParityLattice arg,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if(expression.getOperator() instanceof NumericNegation) {
			return arg;
		}
		else {
			return top();
		}
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
			return bottom();

		BinaryOperator op = expression.getOperator();

		if (op instanceof AdditionOperator || op instanceof SubtractionOperator) {
			if (left == ParityLattice.EVEN)
				return right;

			if (left == ParityLattice.ODD) {
				if (right == ParityLattice.ODD)
					return ParityLattice.EVEN;

				if (right == ParityLattice.EVEN)
					return ParityLattice.ODD;
			}
		}

		if (op instanceof MultiplicationOperator) {
			if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
				return ParityLattice.EVEN;

			if (left == ParityLattice.ODD)
				return right;
		}

		if (op instanceof ModuloOperator || op instanceof RemainderOperator) {
			if (right == ParityLattice.EVEN)
				return left;
		}

		return top();
	}
}