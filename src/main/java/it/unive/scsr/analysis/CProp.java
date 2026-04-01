package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;

import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.TernaryExpression;

import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Constant;

import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;

import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Class representing the constant propagation analysis domain.
 * 
 * @note Docstrings are mostly auto-generated and may need to be revised for clarity and accuracy. This is becaus I had some difficullties to make sense of the surrounding code.
 * 
 * @author Gianmaria Pizzo 872966
 */
public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem>{

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    /**
     * Returns a collection of identifiers involved in this element.
     * 
     * @param id the identifier to be replaced
     * @param state the current state of the analysis
     * 
     * @return the integer value assigned to the identifier in the current state, or null if the identifier is not assigned a constant value.
     */
    private static Integer get_value_of(Identifier id, DefiniteSet<CPropSetElem> state) {
        // Loop through elems
        for (CPropSetElem cprop_elem : state.getDataflowElements()) {
            // If id matches
            if (cprop_elem.getId().equals(id)) {
                // Return the integer value
                return cprop_elem.getConstant();
            }
        }

        return null;
    }

    /**
     * Extracts the integer value of a given expression based on the current state of the analysis.
     * 
     * @param expression the expression to extract the value from
     * @param state the current state of the analysis
     * 
     * @return the integer value of the expression, or null if the expression cannot be evaluated to a constant integer value.
     */
    private static Integer extract_expression_value(SymbolicExpression expression, DefiniteSet<CPropSetElem> state) {
        // Case null expression
        if (expression == null) {
            return null;
        }

        // Case constant expression (ex: 2)
        if (expression instanceof Constant) {
            // Get value
            Object value = ((Constant) expression).getValue();

            // Check if integer
            if (value instanceof Integer) {
                return (Integer) value;
            }

            // Ignore if not integer

        }

        // Case expression is an identifier (ex x;)
        if (expression instanceof Identifier) {
            // Find its value and return it (can be null)
            return get_value_of((Identifier) expression, state);
        }

        // Case unary expression
        if (expression instanceof UnaryExpression) {
            // From unary expression extract the operator and expression
            UnaryExpression unary_expr = (UnaryExpression) expression;
            UnaryOperator unary_operator = unary_expr.getOperator();
            ValueExpression arg = (ValueExpression) unary_expr.getExpression();

            // Extract expression value
            Integer value = extract_expression_value(arg, state);

            // Base case
            if (value == null) {
                return null;
            }

            // Case "-" operator
            if (unary_operator instanceof NumericNegation) {
                // Return negated value
                return -value;
            }
        }

        // Case binary expression
        if (expression instanceof BinaryExpression) {
            // Extract binary expression, operator and left/right values
            BinaryExpression binary_expr = (BinaryExpression) expression;
            BinaryOperator binary_operator = binary_expr.getOperator();

            Integer left = extract_expression_value(binary_expr.getLeft(), state);
            Integer right = extract_expression_value(binary_expr.getRight(), state);

            // Base case
            if (left == null || right == null) {
                return null;
            }

            if (binary_operator instanceof AdditionOperator){
                return left + right;
            }

            if (binary_operator instanceof SubtractionOperator){
                return left - right;
            }

            if (binary_operator instanceof MultiplicationOperator){
                return left * right;
            }

            if (binary_operator instanceof DivisionOperator) {

                // Division by zero
                if (right == 0) {
                    // Return null
                    return null;
                }

                // Enforce integer
                return ((int) left / right);
            }

            if (binary_operator instanceof ModuloOperator){

                // Modulo by zero
                if (right == 0) {
                    // Return null
                    return null;
                }

                // Enforce integer
                return ((int) left % right);
            }
        }

        return null;
    }

    /**
     * Generates a set of elements based on the given state and expression.
     *
     * @param state the current state of the analysis
     * @param id the identifier to be replaced
     * @param expression the expression to extract the value from
     * @param pp the program point
     * 
     * @return the set of generated elements
     * 
     * @throws SemanticException if a semantic error occurs
     */
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        // Build new gen set
        Set<CPropSetElem> generated = new HashSet<>();

        // extract the expression's value as an integer (hopefully)
        Integer value = extract_expression_value(expression, state);

        // If the val is not null
        if (value != null){
            // Add new element of the set with the id and the found integer value
            generated.add(new CPropSetElem(id, value));
        }

        // Returns either empty set or with new element
        return generated;
    }

    /**
     * Generates a set of elements based on the given state and expression.
     *
     * @note This method is not used in the current implementation of the analysis, but it is required by the interface. It returns an empty set by default.
     * 
     * @param state the current state of the analysis
     * @param expression the expression to extract the value from
     * @param pp the program point
     * 
     * @return the set of generated elements
     * 
     * @throws SemanticException if a semantic error occurs
     */
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        // Missing id, return empty set by default
        return Collections.emptySet();
    }

    /**
     * Replaces an identifier with another in this element.
     * 
     * @param source the identifier to be replaced
     * @param target the identifier to replace with
     * 
     * @return a new element with the identifier replaced, or null if the replacement is not applicable.
     */
    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        // Build a new set
        Set<CPropSetElem> killed = new HashSet<>();

        // Loop through the elements
        for (CPropSetElem cprop_elem : state.getDataflowElements()) {
            // If the elem id matches the given id
            if (cprop_elem.getId().equals(id)) {
                // Insert in killed set
                killed.add(cprop_elem);
            }
        }

        return killed;
    }

    /**
     * Replaces an identifier with another in this element.
     * 
     * @note This method is not used in the current implementation of the analysis, but it is required by the interface. It returns an empty set by default.
     * 
     * @param source the identifier to be replaced
     * @param target the identifier to replace with
     * 
     * @return a new element with the identifier replaced, or null if the replacement is not applicable.
     */
    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        // Missing id, return empty set by default
        return Collections.emptySet();
    }

}
