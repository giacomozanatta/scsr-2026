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
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
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
	public ParityLattice evalConstant(Constant c, ProgramPoint pp, SemanticOracle o) throws SemanticException {

		if(c.getValue() instanceof Integer){
			Integer val = (Integer)c.getValue();

			if(val % 2 == 0)
				return ParityLattice.EVEN;
			else
				return ParityLattice.ODD;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression e, ParityLattice l, ProgramPoint pp, SemanticOracle o) throws SemanticException {
		if (e.getOperator() == NumericNegation.INSTANCE)
			return l;
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression e, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle o) throws SemanticException {
		if(left == top() || right == top())
			return ParityLattice.TOP;

		BinaryOperator op = e.getOperator();
		if(op instanceof AdditionOperator || op instanceof SubtractionOperator){
			if((left == ParityLattice.EVEN && right == ParityLattice.EVEN) || (left == ParityLattice.ODD && right == ParityLattice.ODD)){
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