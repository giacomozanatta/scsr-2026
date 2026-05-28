package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

	@Override
	public ExtendedSignLattice top() {
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return ExtendedSignLattice.BOTTOM;
	}

	@Override
	public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		Object value = constant.getValue();
		if (value instanceof Number) {
			double number = ((Number) value).doubleValue();
			if (Double.isNaN(number))
				return ExtendedSignLattice.TOP;
			if (number < 0)
				return ExtendedSignLattice.NEG;
			if (number > 0)
				return ExtendedSignLattice.POS;
			return ExtendedSignLattice.ZERO;
		}
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() == NumericNegation.INSTANCE)
			return negate(arg);
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
			ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		BinaryOperator operator = expression.getOperator();
		if (left.isBottom() || right.isBottom())
			return ExtendedSignLattice.BOTTOM;
		if (operator instanceof AdditionOperator)
			return lift(left, right, ExtendedSign::addAtoms);
		if (operator instanceof SubtractionOperator)
			return lift(left, negate(right), ExtendedSign::addAtoms);
		if (operator instanceof MultiplicationOperator)
			return lift(left, right, ExtendedSign::multiplyAtoms);
		if (operator instanceof DivisionOperator)
			return liftDivision(left, right);
		if (operator instanceof ModuloOperator || operator instanceof RemainderOperator)
			return liftRemainder(left, right);
		return ExtendedSignLattice.TOP;
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
			ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) {
		BinaryOperator operator = expression.getOperator();
		if (operator == ComparisonEq.INSTANCE)
			return left.eq(right);
		if (operator == ComparisonGe.INSTANCE)
			return left.eq(right).or(left.gt(right));
		if (operator == ComparisonGt.INSTANCE)
			return left.gt(right);
		if (operator == ComparisonLe.INSTANCE)
			return left.gt(right).negate();
		if (operator == ComparisonLt.INSTANCE)
			return left.gt(right).or(left.eq(right)).negate();
		if (operator == ComparisonNe.INSTANCE)
			return left.eq(right).negate();
		return Satisfiability.UNKNOWN;
	}

	@Override
	public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(
			ValueEnvironment<ExtendedSignLattice> environment, BinaryExpression expression, ProgramPoint src,
			ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
		Satisfiability sat = satisfies(environment, expression, src, oracle);
		if (sat == Satisfiability.NOT_SATISFIED)
			return environment.bottom();
		if (sat == Satisfiability.SATISFIED)
			return environment;

		BinaryOperator operator = expression.getOperator();
		ValueExpression left = (ValueExpression) expression.getLeft();
		ValueExpression right = (ValueExpression) expression.getRight();
		Identifier id;
		ExtendedSignLattice eval;
		boolean identifierOnLeft;

		if (left instanceof Identifier) {
			id = (Identifier) left;
			eval = eval(environment, right, src, oracle);
			identifierOnLeft = true;
		} else if (right instanceof Identifier) {
			id = (Identifier) right;
			eval = eval(environment, left, src, oracle);
			identifierOnLeft = false;
		} else
			return environment;

		ExtendedSignLattice starting = environment.getState(id);
		if (eval.isBottom() || starting.isBottom())
			return environment.bottom();

		ExtendedSignLattice update = null;
		for (ExtendedSignLattice atom : ExtendedSignLattice.atoms())
			if (starting.contains(atom)) {
				Satisfiability atomSat = identifierOnLeft ? satisfiesAtom(operator, atom, eval)
						: satisfiesAtom(operator, eval, atom);
				if (atomSat.mightBeTrue()) {
					ExtendedSignLattice candidate = starting.glb(atom);
					update = update == null ? candidate : update.lub(candidate);
				}
			}

		if (update == null || update.isBottom())
			return environment.bottom();
		return environment.putState(id, update);
	}

	private static Satisfiability satisfiesAtom(BinaryOperator operator, ExtendedSignLattice left,
			ExtendedSignLattice right) {
		if (operator == ComparisonEq.INSTANCE)
			return left.eq(right);
		if (operator == ComparisonGe.INSTANCE)
			return left.eq(right).or(left.gt(right));
		if (operator == ComparisonGt.INSTANCE)
			return left.gt(right);
		if (operator == ComparisonLe.INSTANCE)
			return left.gt(right).negate();
		if (operator == ComparisonLt.INSTANCE)
			return left.gt(right).or(left.eq(right)).negate();
		if (operator == ComparisonNe.INSTANCE)
			return left.eq(right).negate();
		return Satisfiability.UNKNOWN;
	}

	private interface AtomOperator {
		ExtendedSignLattice apply(ExtendedSignLattice left, ExtendedSignLattice right) throws SemanticException;
	}

	private static ExtendedSignLattice lift(ExtendedSignLattice left, ExtendedSignLattice right, AtomOperator operator)
			throws SemanticException {
		ExtendedSignLattice result = ExtendedSignLattice.BOTTOM;
		for (ExtendedSignLattice l : ExtendedSignLattice.atoms())
			if (left.contains(l))
				for (ExtendedSignLattice r : ExtendedSignLattice.atoms())
					if (right.contains(r))
						result = result.lub(operator.apply(l, r));
		return result;
	}

	private static ExtendedSignLattice liftDivision(ExtendedSignLattice left, ExtendedSignLattice right)
			throws SemanticException {
		ExtendedSignLattice result = ExtendedSignLattice.BOTTOM;
		for (ExtendedSignLattice l : ExtendedSignLattice.atoms())
			if (left.contains(l))
				for (ExtendedSignLattice r : ExtendedSignLattice.atoms())
					if (right.contains(r) && r != ExtendedSignLattice.ZERO)
						result = result.lub(multiplyAtoms(l, r));
		return result;
	}

	private static ExtendedSignLattice liftRemainder(ExtendedSignLattice left, ExtendedSignLattice right)
			throws SemanticException {
		if (right.equals(ExtendedSignLattice.ZERO))
			return ExtendedSignLattice.BOTTOM;
		ExtendedSignLattice result = ExtendedSignLattice.BOTTOM;
		for (ExtendedSignLattice atom : ExtendedSignLattice.atoms())
			if (left.contains(atom))
				result = result.lub(atom).lub(ExtendedSignLattice.ZERO);
		return result;
	}

	private static ExtendedSignLattice addAtoms(ExtendedSignLattice left, ExtendedSignLattice right)
			throws SemanticException {
		if (left == ExtendedSignLattice.ZERO)
			return right;
		if (right == ExtendedSignLattice.ZERO)
			return left;
		if (left == right)
			return left;
		return ExtendedSignLattice.TOP;
	}

	private static ExtendedSignLattice multiplyAtoms(ExtendedSignLattice left, ExtendedSignLattice right) {
		if (left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO)
			return ExtendedSignLattice.ZERO;
		if (left == right)
			return ExtendedSignLattice.POS;
		return ExtendedSignLattice.NEG;
	}

	private static ExtendedSignLattice negate(ExtendedSignLattice sign) throws SemanticException {
		ExtendedSignLattice result = ExtendedSignLattice.BOTTOM;
		if (sign.contains(ExtendedSignLattice.NEG))
			result = result.lub(ExtendedSignLattice.POS);
		if (sign.contains(ExtendedSignLattice.ZERO))
			result = result.lub(ExtendedSignLattice.ZERO);
		if (sign.contains(ExtendedSignLattice.POS))
			result = result.lub(ExtendedSignLattice.NEG);
		return result;
	}
}
