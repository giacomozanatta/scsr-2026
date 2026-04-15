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
        if(constant.getValue() instanceof Integer num) {
            return (num % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression exp, ParityLattice elem, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(exp.getOperator() == NumericNegation.INSTANCE) {
            return elem;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression exp, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) {
        BinaryOperator op = exp.getOperator();

        if(right == ParityLattice.BOTTOM || left == ParityLattice.BOTTOM) {
            return ParityLattice.BOTTOM;
        }
        else if(op instanceof MultiplicationOperator) {
            if(right == ParityLattice.TOP || left == ParityLattice.TOP) {
                return ParityLattice.TOP;
            }
            else if(right == ParityLattice.EVEN || left == ParityLattice.EVEN) {
                return ParityLattice.EVEN;
            }
            else return ParityLattice.ODD;
        }
        else if(right == ParityLattice.TOP || left == ParityLattice.TOP) {
            return ParityLattice.TOP;
        }
        else if(op instanceof AdditionOperator || op instanceof SubtractionOperator) {
            if(left.equals(right)) {
                return ParityLattice.EVEN;
            }
            else return ParityLattice.ODD;
        }
        else return ParityLattice.TOP;
    }
}