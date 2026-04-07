package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;

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


	@Override
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		if(constant.getValue() instanceof Integer n) {
			return (n % 2 == 0)	? ParityLattice.EVEN : ParityLattice.ODD;
		}
		//non-integer constant so we don't know
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
										   SemanticOracle oracle) throws SemanticException {

		return arg; // sign change but no change of parity
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
											ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM){
			return ParityLattice.BOTTOM; // error state will always propagate
		}

		else if(expression.getOperator() instanceof AdditionOperator || expression.getOperator() instanceof SubtractionOperator) {
			if(left == ParityLattice.TOP || right == ParityLattice.TOP){
				// T +- x = T
				// x +- T = T
				return ParityLattice.TOP;
			}

			else if (left == right){
				// EVEN +- EVEN = EVEN
				// ODD +- ODD = EVEN
				return ParityLattice.EVEN;
			}

			// EVEN +- ODD ; ODD +- EVEN
			return ParityLattice.ODD;
		} else if (expression.getOperator() instanceof MultiplicationOperator) {

			if(left == ParityLattice.EVEN || right == ParityLattice.EVEN){
				// EVEN * x = EVEN
				// x * EVEN = EVEN
				return ParityLattice.EVEN;
			}

			else if (left == ParityLattice.TOP || right == ParityLattice.TOP){
				// if either is T and neither is even : propagate T
				return ParityLattice.TOP;
			}

			// both are odd => result is odd
			return ParityLattice.ODD;
		} else if (expression.getOperator() instanceof DivisionOperator) {
			// division doesn't preserve parity in any predictable way
			return ParityLattice.TOP;

		} else if (expression.getOperator() instanceof ModuloOperator) {
			// modulo parity is unpredictable in general
			return ParityLattice.TOP;
		}
		else if (expression.getOperator() instanceof RemainderOperator) {
			// remainder parity is unpredictable in general
			return ParityLattice.TOP;
		}
		return ParityLattice.TOP;
	}

	//TODO add missing components

	@Override
	public Satisfiability satisfiesBinaryExpression(
			BinaryExpression expression,
			ParityLattice left,
			ParityLattice right,
			ProgramPoint pp,
			SemanticOracle oracle) {

		if (left.isBottom() || right.isBottom())
			return Satisfiability.BOTTOM;

		if (left.isTop() || right.isTop())
			return Satisfiability.UNKNOWN;

		BinaryOperator operator = expression.getOperator();

		if (operator == ComparisonEq.INSTANCE)
			// delegate to eq() to check for equality
			return left.eq(right);
		else if (operator == ComparisonNe.INSTANCE)
			// != operator so delegate to eq() and negate
			return left.eq(right).negate();
		else
			// parity gives 0 info about all other ordering comparisons (<,>,<=,>=)
			return Satisfiability.UNKNOWN;
	}


	// if (x == y) is true, we know x and y have the same parity
	// it seems to be the only case in which we can update what we know about the parity of a variable
	@Override
	public ValueEnvironment<ParityLattice> assumeBinaryExpression(
			ValueEnvironment<ParityLattice> environment,
			BinaryExpression expression,
			ProgramPoint src,
			ProgramPoint dest,
			SemanticOracle oracle)
			throws SemanticException {

		Satisfiability sat = satisfies(environment, expression, src, oracle);
		if (sat == Satisfiability.NOT_SATISFIED)
			return environment.bottom();
		if (sat == Satisfiability.SATISFIED)
			return environment;

		BinaryOperator operator = expression.getOperator();
		ValueExpression left = (ValueExpression) expression.getLeft();
		ValueExpression right = (ValueExpression) expression.getRight();

		if (operator != ComparisonEq.INSTANCE)
			// <, >, <=, >= : parity tells us nothing, no refinement
			return environment;

		// we can only refine if one side is a variable and the operator is ==
		if (left instanceof Identifier id) {
			ParityLattice evalRight = eval(environment, right, src, oracle);
			ParityLattice current = environment.getState(id);

			if (evalRight.isBottom() || current.isBottom())
				return environment.bottom();

			// glb: if x = TOP and expr = EVEN -> we now know x is even too
			// if x = EVEN and expr = even -> stays EVEN
			// if x = even and expr = odd -> return bottom (error state)
			ParityLattice update = current.glb(evalRight);

			if (update.isBottom())
				return environment.bottom();
			return environment.putState(id, update);

		} else if (right instanceof Identifier id) {
			// Symmetric case: expr == x
			ParityLattice evalLeft = eval(environment, left, src, oracle);
			ParityLattice current  = environment.getState(id);

			if (evalLeft.isBottom() || current.isBottom())
				return environment.bottom();

			ParityLattice update = current.glb(evalLeft);

			if (update.isBottom())
				return environment.bottom();
			return environment.putState(id, update);
		}

		return environment;
	}
}