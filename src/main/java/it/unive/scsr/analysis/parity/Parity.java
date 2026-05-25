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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

/**
 * Non-relational abstract domain for Parity analysis.
 *
 * Tracks whether integer variables are Even or Odd at each program point.
 * Noninteger values are abstracted to TOP (unknown parity).
 *
 * Arithmetic rules:
 *   Even + Even = Even,  Even + Odd = Odd,   Odd + Odd = Even
 *   Even - Even = Even,  Even - Odd = Odd,   Odd - Even = Odd,  Odd - Odd = Even
 *   Even * Even = Even,  Even * Odd = Even,  Odd * Odd = Odd
 *   -Even = Even,        -Odd = Odd
 */
public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	public ParityLattice top() {
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		return ParityLattice.BOTTOM;
	}

	/**
	 * Evaluates a literal constant to its parity.
	 * Noninteger constants (strings, floats) are abstracted to TOP.
	 */
	@Override
	public ParityLattice evalConstant(
			Constant constant,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (constant.getValue() instanceof Integer n)
			return (n % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD;

		// Noninteger constant — parity unknown
		return ParityLattice.TOP;
	}

	/**
	 * Evaluates unary expressions.
	 * Negation preserves parity: -Even = Even, -Odd = Odd.
	 */
	@Override
	public ParityLattice evalUnaryExpression(
			UnaryExpression expression,
			ParityLattice arg,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (expression.getOperator() instanceof NumericNegation) {
			// Negation preserves parity: -Even=Even, -Odd=Odd
			// TOP, BOTTOM, EVEN, ODD all map to themselves
			return arg;
		}

		return ParityLattice.TOP;
	}

	/**
	 * Evaluates binary expressions using parity arithmetic rules.
	 */
	@Override
	public ParityLattice evalBinaryExpression(
			BinaryExpression expression,
			ParityLattice left,
			ParityLattice right,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		// Propagate BOTTOM
		if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
			return ParityLattice.BOTTOM;

		// Propagate TOP
		if (left == ParityLattice.TOP || right == ParityLattice.TOP)
			return ParityLattice.TOP;

		if (expression.getOperator() instanceof AdditionOperator)
			return evalAddition(left, right);

		if (expression.getOperator() instanceof SubtractionOperator)
			return evalSubtraction(left, right);

		if (expression.getOperator() instanceof MultiplicationOperator)
			return evalMultiplication(left, right);

		if (expression.getOperator() instanceof DivisionOperator)
			// Division does not preserve parity in general (e.g. 5/2 = 2)
			return ParityLattice.TOP;

		return ParityLattice.TOP;
	}

	/**
	 * Addition parity rules:
	 *   Even + Even = Even
	 *   Even + Odd  = Odd
	 *   Odd  + Even = Odd
	 *   Odd  + Odd  = Even
	 */
	private ParityLattice evalAddition(ParityLattice left, ParityLattice right) {
		if (left == right) return ParityLattice.EVEN; // same parity → even
		return ParityLattice.ODD;                     // different parity → odd
	}

	/**
	 * Subtraction parity rules (same as addition):
	 *   Even - Even = Even
	 *   Even - Odd  = Odd
	 *   Odd  - Even = Odd
	 *   Odd  - Odd  = Even
	 */
	private ParityLattice evalSubtraction(ParityLattice left, ParityLattice right) {
		// Subtraction has same parity rules as addition
		return evalAddition(left, right);
	}

	/**
	 * Multiplication parity rules:
	 *   Even * anything = Even
	 *   Odd  * Odd      = Odd
	 */
	private ParityLattice evalMultiplication(ParityLattice left, ParityLattice right) {
		if (left == ParityLattice.EVEN || right == ParityLattice.EVEN)
			return ParityLattice.EVEN;
		return ParityLattice.ODD; // Odd * Odd = Odd
	}
}