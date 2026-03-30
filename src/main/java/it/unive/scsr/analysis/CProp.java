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

import java.util.HashSet;
import java.util.Set;

/**
 * Constant Propagation dataflow analysis.
 * Tracks which variables hold a known constant integer value at each program point.
 * Uses DefiniteSet (intersection-based): a variable is considered constant only if
 * it is constant along ALL incoming paths (correct behavior at if/else merge points).
 * Each state is a set of (variable, constant) pairs → e.g. { (x,1), (y,3) }
 * At every assignment the framework applies: out = (in \ kill) ∪ gen
 */
public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    /**
     * Creates the initial empty state for each CFG node.
     * Empty = no variable is yet known to be constant.
     */
    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    /**
     * Tries to evaluate a ValueExpression to a constant integer,
     * using the current state to resolve known variable values.
     * Returns null if the expression cannot be reduced to a constant integer.
     * Cases:
     *  - Constant    → integer literal, e.g. x = 5
     *  - Identifier  → variable lookup in state, e.g. y = x (if x is known)
     *  - Unary  (-x) → negation of a constant, e.g. y = -x
     *  - Binary      → arithmetic between constants, e.g. z = x + y
     */
    private Integer evaluate(ValueExpression expression, DefiniteSet<CPropSetElem> state) {

        // case: integer literal → e.g. x = 5
        if (expression instanceof Constant constant) {
            Object value = constant.getValue();
            if (value instanceof Integer i) {
                return i;
            }
            return null;
        }

        // case: variable → look up its value in the current state, e.g. y = x (if x is known)
        if (expression instanceof Identifier identifier) {
            for (CPropSetElem elem : state.getDataflowElements()) {
                if (elem.getId().equals(identifier)) {
                    return elem.getConstant();
                }
            }
            return null;
        }

        // case: unary negation → e.g. y = -x, evaluates x and then negates the result
        if (expression instanceof UnaryExpression unaryExpression) {
            if (unaryExpression.getOperator() instanceof NumericNegation) {
                Integer inner = evaluate((ValueExpression) unaryExpression.getExpression(), state);
                if (inner != null) {
                    return -inner;
                }
            }
            return null;
        }

        // case: binary arithmetic → e.g. z = x + y, z = x * 2, both sides must be constants
        if (expression instanceof BinaryExpression binaryExpression) {
            Integer left = evaluate((ValueExpression) binaryExpression.getLeft(), state);
            Integer right = evaluate((ValueExpression) binaryExpression.getRight(), state);

            // if either side is not a constant, the result is not a constant
            if (left == null ||  right == null) {
                return null;
            }

            if (binaryExpression.getOperator() instanceof AdditionOperator) {
                return left + right;
            }

            if (binaryExpression.getOperator() instanceof SubtractionOperator) {
                return left - right;
            }

            if (binaryExpression.getOperator() instanceof MultiplicationOperator) {
                return left * right;
            }

            if (binaryExpression.getOperator() instanceof DivisionOperator) {
                if (right == 0) {
                    return null;
                }
                return left / right;
            }
        }

        return null;
    }

    /**
     * GEN for assignments: id = expression
     * If the expression evaluates to a constant integer, generates the pair (id, value).
     * Otherwise, generates nothing (empty set).
     * Examples:
     *   x = 1        → gen = { (x,1) }
     *   y = x + 2    → gen = { (y,3) }   if (x,1) is in state
     *   z = input()  → gen = { }          cannot evaluate → not a constant
     */
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> generated = new HashSet<>();

        Integer value = evaluate(expression, state);

        if (value != null) {
            generated.add(new CPropSetElem(id, value));
        }

        return generated;
    }

    /**
     * GEN for statements without assignment (e.g. conditions, return).
     * No new constant pairs can be generated → always empty.
     */
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    /**
     * KILL for assignments: id = expression
     * Removes all pairs (id, *) from the state, since id is being reassigned
     * and its previous constant value is no longer valid.
     * Example:
     *   state = { (x,1), (y,3) }, statement = x = 7
     *   kill  = { (x,1) }
     */
    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> killed = new HashSet<>();
        for (CPropSetElem elem : state.getDataflowElements()) {
            if (elem.getId().equals(id)) {
                killed.add(elem);
            }
        }
        return killed;
    }

    /**
     * KILL for statements without assignment.
     * No variable is modified → nothing to kill.
     */
    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }


}


    // IMPLEMENTATION NOTE:

    // - Implement your solution using the DefiniteSet.
    //   - What would happen if you used a PossibleSet instead? Think about it (or try it), but remember to deliver the Definite version.
    // - Keep it simple: track only integer values. Any non-integer values should be ignored.
    // - To test your implementation, you can use the inputs/cprop.imp file or define your own test cases.
    // - Refer to the Java test methods discussed in class and adjust them accordingly to work with your domain.
    // - Constant is subclass of ValueExpression
    // - How should integer constant values be propagated?
    //   - Consider the following code snippet:
    //       1. x = 1
    //       2. y = x + 2
    //     The expected output should be:
    //       1. [x,1]
    //       2. [x,1] [y,3]
    //   - How can you retrieve the constant value of `x` to use at program point 2?
    //   - When working with an object of type `Constant`, you can obtain its value by calling the `getValue()` method.