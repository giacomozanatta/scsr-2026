package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Constant Propagation dataflow analysis.
 *
 * Tracks pairs (variable, constant) that are guaranteed to hold
 * at each program point. Uses a DefiniteSet (intersection-based join),
 * meaning a pair is kept only if it holds on ALL incoming paths.
 *
 * Supported expressions: constants, variables, +, -, *, /, unary negation.
 */
public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    /**
     * Tries to evaluate a {@link ValueExpression} to a concrete integer constant,
     * given the current dataflow state.
     * <p>
     * Returns {@code null} if the expression cannot be resolved to a constant
     * (e.g. unknown variable, noninteger value, unsupported operator).
     *
     * @param state      the current set of known (variable, constant) pairs
     * @param expression the expression to evaluate
     * @return the integer value, or {@code null} if not computable
     */
    public Integer evaluate(DefiniteSet<CPropSetElem> state, ValueExpression expression) {

        // Base case: the expression is a literal constant (e.g. x = 5)
        if (expression instanceof Constant c) {
            Object val = c.getValue();
            if (val instanceof Integer)
                return (Integer) val;
            // Noninteger constants (e.g. strings, floats) are not tracked
            return null;
        }

        // Base case: the expression is a variable — look up its known value
        if (expression instanceof Identifier id) {
            for (CPropSetElem elem : state.getDataflowElements())
                if (elem.getId().equals(id))
                    return elem.getValue();
            // Variable is not known to be a constant at this point
            return null;
        }

        // Recursive case: binary expression (e.g. x + y, x * 2)
        if (expression instanceof BinaryExpression b) {
            Integer left  = evaluate(state, (ValueExpression) b.getLeft());
            Integer right = evaluate(state, (ValueExpression) b.getRight());

            // If either operand is unknown, the result is also unknown
            if (left == null || right == null)
                return null;

            if (b.getOperator() instanceof AdditionOperator)       return left + right;
            if (b.getOperator() instanceof SubtractionOperator)    return left - right;
            if (b.getOperator() instanceof MultiplicationOperator) return left * right;
            if (b.getOperator() instanceof DivisionOperator) {
                // Guard against division by zero — treat result as unknown
                if (right == 0)
                    return null;
                return left / right;
            }

            // Unsupported binary operator
            return null;
        }

        // Recursive case: unary negation (e.g. -x)
        if (expression instanceof UnaryExpression u) {
            if (u.getOperator() instanceof NumericNegation) {
                Integer inner = evaluate(state, (ValueExpression) u.getExpression());
                if (inner != null)
                    return -inner;
            }
            // Unsupported unary operator or unknown operand
            return null;
        }

        // Any other expression type (e.g. PushAny, Skip) — not a constant
        return null;
    }

    /**
     * Generates new dataflow facts for an assignment {@code id = expression}.
     * <p>
     * If the right-hand side evaluates to a constant, we generate the pair
     * (id, constant). Otherwise, nothing is generated.
     */
    @Override
    public Set<CPropSetElem> gen(
            DefiniteSet<CPropSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {

        Integer val = evaluate(state, expression);
        if (val != null)
            return Collections.singleton(new CPropSetElem(id, val));
        return Collections.emptySet();
    }

    /**
     * No facts are generated for non-assignment expressions
     * (e.g. standalone conditions, method calls).
     */
    @Override
    public Set<CPropSetElem> gen(
            DefiniteSet<CPropSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    /**
     * Kills all existing facts about {@code id} when it gets reassigned.
     * <p>
     * This ensures stale constant information is removed before
     * the new gen fact (if any) is added.
     */
    @Override
    public Set<CPropSetElem> kill(
            DefiniteSet<CPropSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {

        Set<CPropSetElem> result = new HashSet<>();
        for (CPropSetElem elem : state.getDataflowElements())
            if (elem.getId().equals(id))
                result.add(elem);
        return result;
    }

    /**
     * No facts are killed for non-assignment expressions.
     */
    @Override
    public Set<CPropSetElem> kill(
            DefiniteSet<CPropSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    /**
     * Returns a fresh empty DefiniteSet to be used as the bottom element
     * of the lattice (no constants known).
     */
    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }
}