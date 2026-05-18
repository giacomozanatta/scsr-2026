package it.unive.scsr.analysis.extended_sign;

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


/**
 * Abstract domain for the extended sign analysis.
 *
 * @see ExtendedSignLattice
 * 
 * @author Gianmaria Pizzo 872966
 */
public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

	@Override
	public ExtendedSignLattice top() {
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return ExtendedSignLattice.BOTTOM;
	}

	/**
	 * Maps an integer constant to its abstract sign value.
	 *
	 * @param constant the concrete constant
	 * @param pp the program point (unused here)
	 * @param oracle the semantic oracle (unused here)
	 * 
	 * @return NEG, ZERO, or POS for numeric constants. TOP otherwise
	 */
	@Override
	public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		Object value = constant.getValue();

		// Handle integer constants
		if (value instanceof Integer) {
			int n = (Integer) value;

			if (n == 0) {
				return ExtendedSignLattice.ZERO;
			}
			
			if (n >  0){ 
				return ExtendedSignLattice.POS;
			}

			return ExtendedSignLattice.NEG;
		}

		// Handle long costants
		if (value instanceof Long) {
			long n = (Long) value;

			if (n == 0L) {
				return ExtendedSignLattice.ZERO;
			}

			if (n >  0L) {
				return ExtendedSignLattice.POS;
			}

			return ExtendedSignLattice.NEG;
		}

		return ExtendedSignLattice.TOP;
	}

	/**
	 * Evaluates a unary expression over an abstract operand.
	 *
	 * @note Only numeric negation ({@code -x}) is handled, while all other return TOP.
	 *
	 * @param expression the unary expression node
	 * @param arg abstract value of the operand
	 * @param pp current program point
	 * @param oracle semantic oracle
	 * 
	 * @return abstract result of the unary operation
	 */
	@Override
	public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		// Consider only numeric negation
		if (expression.getOperator() == NumericNegation.INSTANCE){
			return arg.negation();
		}

		return ExtendedSignLattice.TOP;
	}

	/**
	 * Evaluates a binary arithmetic expression over abstract operands.
	 *
	 * @brief For reachable operands, the call is dispatched to the 
	 * 	corresponding {@link ExtendedSignLattice} operation. Unrecognised 
	 *  operators return TOP (sound over-approximation).
	 *
	 * @param expression the binary expression node
	 * @param left abstract value of the left operand
	 * @param right abstract value of the right operand
	 * @param pp current program point
	 * @param oracle semantic oracle
	 * 
	 * @return abstract result of the binary operation
	 */
	@Override
	public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (left.isBottom() || right.isBottom()){
			return bottom();
		}

		BinaryOperator operator = expression.getOperator();

		if (operator instanceof AdditionOperator) {
			return left.addition(right);
		} else if (operator instanceof SubtractionOperator) {
			return left.subtraction(right);
		} else if (operator instanceof MultiplicationOperator) {
			return left.multiplication(right);
		} else if (operator instanceof DivisionOperator) {
			return left.division(right);
		} else if (operator instanceof ModuloOperator) {
			return left.modulus(right);
		} else if (operator instanceof RemainderOperator) {
			return left.remainder(right);
		}

		return ExtendedSignLattice.TOP;
	}

	/**
	 * Decides whether a comparison between two abstract values can hold.
	 *
	 * @param expression the comparison expression
	 * @param left abstract value of the left operand
	 * @param right abstract value of the right operand
	 * @param pp current program point
	 * @param oracle semantic oracle
	 * 
	 * @return {@link Satisfiability} of the comparison: SATISFIED, NOT_SATISFIED, or UNKNOWN
	 */
	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) {
		if (left.isTop() || right.isTop()){
			return Satisfiability.UNKNOWN;
		}

		BinaryOperator operator = expression.getOperator();
		if (operator == ComparisonEq.INSTANCE) {
			return left.eq(right);
		}

		if (operator == ComparisonNe.INSTANCE) {
			return left.eq(right).negate();
		}

		if (operator == ComparisonGt.INSTANCE) {
			return left.gt(right);
		}

		if (operator == ComparisonGe.INSTANCE) {
			return left.gt(right).or(left.eq(right));
		}

		if (operator == ComparisonLt.INSTANCE) {
			return left.gt(right).negate().and(left.eq(right).negate());
		}

		if (operator == ComparisonLe.INSTANCE) {
			return left.gt(right).negate();
		}

		return Satisfiability.UNKNOWN;
	}

	// TODO: Down here

	/**
	 * Refines a variable's abstract state by assuming a comparison holds on a branch.
	 *
	 * <p>This is the key method for precision. It is called when LiSA evaluates
	 * the true branch of a conditional; the dual (false branch) is handled
	 * automatically by LiSA using the negated condition.
	 *
	 * <h3>Algorithm</h3>
	 * <ol>
	 *   <li>Check the satisfiability of the condition in the current environment.
	 *       If NOT_SATISFIED, the branch is dead — return {@code bottom()}.
	 *       If SATISFIED, the environment is unchanged.</li>
	 *   <li>Identify which side of the comparison is an {@link Identifier}
	 *       ({@code id}) and evaluate the other side to get {@code eval}.
	 *       If neither side is an identifier, no refinement is possible.</li>
	 *   <li>For equality ({@code ==}): {@code update = starting glb eval}.</li>
	 *   <li>For all other operators: iterate over the three <em>atomic</em>
	 *       elements {@code {NEG, ZERO, POS}} and collect those for which
	 *       the condition <em>might</em> hold (using
	 *       {@link Satisfiability#mightBeTrue} / {@link Satisfiability#mightBeFalse}).
	 *       Build {@code update} as {@code LUB} of {@code starting glb s} for
	 *       each qualifying atom {@code s}.</li>
	 * </ol>
	 *
	 * <h3>Why only atoms {NEG, ZERO, POS}?</h3>
	 * Using {@code glb(starting, s)} for an atom {@code s} extracts the
	 * single bit of {@code starting} that corresponds to {@code s}. The final
	 * LUB over all qualifying atoms then assembles exactly the right compound
	 * element. For example, for {@code x != 0} with {@code starting = TOP}:
	 * <ul>
	 *   <li>NEG qualifies ({@code NEG.eq(ZERO) = NOT_SATISFIED → mightBeFalse}):
	 *       {@code TOP glb NEG = NEG}</li>
	 *   <li>ZERO does not qualify ({@code ZERO.eq(ZERO) = SATISFIED → NOT mightBeFalse})</li>
	 *   <li>POS qualifies: {@code TOP glb POS = POS}</li>
	 *   <li>{@code NEG lub POS = NON_ZERO} — the compound element emerges automatically.</li>
	 * </ul>
	 * Adding compound elements to the iteration set would be redundant and less
	 * precise, because {@code s.gt(eval)} and {@code s.eq(eval)} return UNKNOWN
	 * more often for compound {@code s}, preventing useful refinement.
	 *
	 * <h3>Refinement rules per operator</h3>
	 * <pre>
	 *   ==   update = starting glb eval
	 *   >=   keep atoms s where (s >= eval) mightBeTrue
	 *   <=   keep atoms s where (s <= eval) mightBeTrue  ≡  (s > eval) mightBeFalse
	 *   &gt;   keep atoms s where (s > eval) mightBeTrue
	 *   &lt;   keep atoms s where (s < eval) mightBeTrue  ≡  (s >= eval) mightBeFalse
	 *   !=   keep atoms s where (s == eval) mightBeFalse
	 * </pre>
	 * When {@code rightIsExpr} is false (the identifier is on the right-hand side),
	 * the sense of the comparison is flipped accordingly.
	 *
	 * @param environment the pre-condition abstract environment
	 * @param expression  the branch condition
	 * @param src         source program point of the edge
	 * @param dest        destination program point of the edge
	 * @param oracle      semantic oracle
	 * @return refined environment valid on the branch where the condition holds
	 */
	@Override
	public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
		Satisfiability sat = satisfies(environment, expression, src, oracle);

		if (sat == Satisfiability.NOT_SATISFIED) {
			return environment.bottom();
		}

		if (sat == Satisfiability.SATISFIED) {
			return environment;
		}

		Identifier id;
		ExtendedSignLattice eval;
		boolean rightIsExpr; // true iff the identifier is on the left (right side is the expression)
		BinaryOperator operator = expression.getOperator();
		ValueExpression left  = (ValueExpression) expression.getLeft();
		ValueExpression right = (ValueExpression) expression.getRight();

		if (left instanceof Identifier) {
			id = (Identifier) left;
			eval = eval(environment, right, src, oracle);
			rightIsExpr = true;
		} else if (right instanceof Identifier) {
			id = (Identifier) right;
			eval = eval(environment, left, src, oracle);
			rightIsExpr = false;
		} else {
			return environment; // neither side is a bare variable; nothing to refine
		}

		ExtendedSignLattice starting = environment.getState(id);
		if (eval.isBottom() || starting.isBottom()){
			return environment.bottom();
		}

		// Atomic elements only — compound elements emerge from glb/lub automatically.
		// See Javadoc above for the rationale.
		ExtendedSignLattice[] atoms = {
			ExtendedSignLattice.NEG, ExtendedSignLattice.ZERO, ExtendedSignLattice.POS
		};

		ExtendedSignLattice update = null;

		if (operator == ComparisonEq.INSTANCE) {
			// Restrict the variable to values consistent with eval.
			update = starting.glb(eval);
		} 
		else if (operator	 == ComparisonGe.INSTANCE) {
			// id >= eval (or eval >= id when !rightIsExpr): keep atoms where this might hold.
			for (ExtendedSignLattice s : atoms)
				if ((rightIsExpr ? s.gt(eval).or(s.eq(eval)) : eval.gt(s).or(eval.eq(s))).mightBeTrue())
					update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
		} 
		else if (operator == ComparisonLe.INSTANCE) {
			// id <= eval: equivalent to NOT (id > eval), keep atoms where (s > eval) might be false.
			for (ExtendedSignLattice s : atoms)
				if ((rightIsExpr ? s.gt(eval) : eval.gt(s)).mightBeFalse())
					update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
		} 
		else if (operator == ComparisonLt.INSTANCE) {
			// id < eval: equivalent to NOT (id >= eval), keep atoms where (s >= eval) might be false.
			for (ExtendedSignLattice s : atoms)
				if ((rightIsExpr ? s.gt(eval).or(s.eq(eval)) : eval.gt(s).or(eval.eq(s))).mightBeFalse())
					update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
		} 
		else if (operator == ComparisonGt.INSTANCE) {
			// id > eval: keep atoms where (s > eval) might be true.
			for (ExtendedSignLattice s : atoms)
				if ((rightIsExpr ? s.gt(eval) : eval.gt(s)).mightBeTrue())
					update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
		} 
		else if (operator == ComparisonNe.INSTANCE) {
			// id != eval: keep atoms where (s == eval) might be false.
			// When eval = ZERO this drops the ZERO atom, leaving {NEG, POS} = NON_ZERO.
			for (ExtendedSignLattice s : atoms)
				if ((rightIsExpr ? s.eq(eval) : eval.eq(s)).mightBeFalse())
					update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
		}

		// If operator is not handled
		if (update == null) {
			return environment;
		}

		// If condition is contradictory
		if (update.isBottom()){
			return environment.bottom();
		}

		return environment.putState(id, update);
	}
}
