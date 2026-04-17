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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.scsr.analysis.sign.SignLattice;

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	public ParityLattice top() {
		// DONE : homework
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		// DONE : homework
		return ParityLattice.BOTTOM;
	}

	//TODO add missing components

	@Override
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
			Integer n = (Integer) constant.getValue();
			if (n % 2 == 0)
				return ParityLattice.EVEN;
			else
				return ParityLattice.ODD;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() == NumericNegation.INSTANCE) {
			if (arg == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			else if (arg == ParityLattice.ODD)
				return ParityLattice.ODD;
			else if(arg == ParityLattice.TOP)
				return ParityLattice.TOP;
			else if(arg == ParityLattice.BOTTOM)
				return ParityLattice.BOTTOM;
		}
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		//TODO
		//Bottom
		if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
			return ParityLattice.BOTTOM;

		//TOP ("I don't know")
		else if (left == ParityLattice.TOP || right == ParityLattice.TOP)
			return ParityLattice.TOP;

		//Addition et soustraction
		if(expression.getOperator() instanceof AdditionOperator
			|| expression.getOperator() instanceof SubtractionOperator) {
			if(left == ParityLattice.EVEN && right == ParityLattice.EVEN
					|| left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.EVEN;
			else if(left == ParityLattice.EVEN && right == ParityLattice.ODD
					|| left == ParityLattice.ODD && right == ParityLattice.EVEN)
				return ParityLattice.ODD;

			// Multiplication
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			if(left == ParityLattice.EVEN && right == ParityLattice.EVEN
					|| left == ParityLattice.EVEN && right == ParityLattice.ODD
					|| left == ParityLattice.ODD && right == ParityLattice.EVEN)
				return ParityLattice.EVEN;
			else if (left == ParityLattice.ODD && right == ParityLattice.ODD)
				return ParityLattice.ODD;

			//Division
		} else if (expression.getOperator() instanceof DivisionOperator)
			return ParityLattice.TOP;
			// Modulo
		else if (expression.getOperator() instanceof ModuloOperator)
			return left;

			// Remainder
		else if (expression.getOperator() instanceof RemainderOperator)
			return right;

		return ParityLattice.TOP;
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		//TODO
		if (left.isTop() || right.isTop())
			return Satisfiability.UNKNOWN;
		BinaryOperator operator = expression.getOperator();
		if (operator instanceof ComparisonEq) {
			if (left.isTop() || right.isTop())
				return Satisfiability.UNKNOWN;
			if (left.isBottom() || right.isBottom())
				return Satisfiability.BOTTOM;
			return left.equals(right) ? Satisfiability.SATISFIED : Satisfiability.NOT_SATISFIED;
		}
		if (operator instanceof ComparisonNe) {
			if (left.isTop() || right.isTop())
				return Satisfiability.UNKNOWN;
			if (left.isBottom() || right.isBottom())
				return Satisfiability.BOTTOM;
			return left.equals(right) ? Satisfiability.NOT_SATISFIED : Satisfiability.SATISFIED;
		}
		return Satisfiability.UNKNOWN;
	}

	@Override
	public ValueEnvironment<ParityLattice> assumeBinaryExpression(ValueEnvironment<ParityLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
		//TODO
		BinaryOperator op = expression.getOperator();

		// On ne traite que ==
		if (!(op instanceof ComparisonEq))
			return environment;

		ValueExpression left = (ValueExpression) expression.getLeft();
		ValueExpression right = (ValueExpression) expression.getRight();

		// On ne peut restreindre que si au moins un côté est un identifiant
		if (!(left instanceof Identifier) && !(right instanceof Identifier))
			return environment;

		Identifier id;
		ParityLattice eval;

		if (left instanceof Identifier) {
			id = (Identifier) left;
			eval = eval(environment, right, src, oracle);
		} else {
			id = (Identifier) right;
			eval = eval(environment, left, src, oracle);
		}

		ParityLattice current = environment.getState(id);
		ParityLattice update = current.glb(eval);

		if (update.isBottom())
			return environment.bottom();

		return environment.putState(id, update);
	}
}