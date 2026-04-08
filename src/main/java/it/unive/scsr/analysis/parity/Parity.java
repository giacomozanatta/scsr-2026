package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

/**
 * An implementation of a non-relational value domain for parity analysis.
 * 
 * This class defines how to evaluate constants, unary expressions, and binary expressions in terms of their parity (even, odd, or unknown).
 * It also defines how to determine the satisfiability of binary expressions based on the parity information of their operands.
 * 
 * The lattice used in this domain is defined in the {@link ParityLattice} class, which includes the elements: TOP (unknown), EVEN, ODD, and BOTTOM (contradiction).
 * 
 * @author Gianmaria Pizzo 872966
 */
public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

	/**
     * Returns the top element of the lattice.
     * 
     * @return the top element of the lattice
     */
	@Override
    public ParityLattice top() {
        return ParityLattice.TOP;
    }

    /**
     * Returns the bottom element of the lattice.
     * 
     * @return the bottom element of the lattice
     */
    @Override
    public ParityLattice bottom() {
        return ParityLattice.BOTTOM;
    }

    /**`
     * Evaluates a constant and returns its parity lattice element.
     * 
     * @note In this implementation, we only consider integer constants for parity analysis.
     * 
     * @param constant the constant to evaluate
     * @param pp the program point where the constant is evaluated
     * @param oracle the semantic oracle for additional information
     * 
     * @return the parity lattice element corresponding to the constant
     * 
     * @throws SemanticException if an error occurs during evaluation
     */
	@Override
    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        // Only care for integer constants
        if (constant.getValue() instanceof Integer){
            Integer val = ((Integer) constant.getValue());

            if (val == 0 || val % 2 == 0){
                return ParityLattice.EVEN;
            } 

            return ParityLattice.ODD;
        }

        return top();
    }

    /**
     * Evaluates a unary expression and returns its parity lattice element.
     * 
     * @note In this implementation, we only consider numeric negation as a unary operator that affects parity.
     * 
     * @param expression the unary expression to evaluate
     * @param arg the argument lattice element
     * @param pp the program point where the expression is evaluated
     * @param oracle the semantic oracle for additional information
     * 
     * @return the parity lattice element corresponding to the expression
     * 
     * @throws SemanticException if an error occurs during evaluation
     */
    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        // Only care for numeric negation
        if (expression.getOperator() == NumericNegation.INSTANCE){
            // Negation does not change parity
            return arg;
        }

        return top();
    }

	/**
     * Evaluates a binary expression and returns its parity lattice element.
     * 
     * @note In this implementation, we consider addition, subtraction, multiplication, division, modulo and remainder as binary operators that affect parity.
     * 
     * @param expression the binary expression to evaluate
     * @param left the left argument lattice element
     * @param right the right argument lattice element
     * @param pp the program point where the expression is evaluated
     * @param oracle the semantic oracle for additional information
     * 
     * @return the parity lattice element corresponding to the expression
     * 
     * @throws SemanticException if an error occurs during evaluation
     */
    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        // If either is bottom, we have no information
        if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM){
            return bottom();
        }

        // Else gather operator and apply parity rules
        BinaryOperator operator = expression.getOperator();

        // Handle additionn and subtraction because they got same parity rules
        if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator){
            if (left == ParityLattice.EVEN){
                // Even +- X = X
                return right;
            } 
            else if (left == ParityLattice.ODD){
                if (right == ParityLattice.EVEN){
                    // Odd +- Even = Odd
                    return left;
                }
                else if (right == ParityLattice.ODD){
                    // Odd +- Odd = Even
                    return ParityLattice.EVEN;
                }
            }
        } else if (operator instanceof MultiplicationOperator){
            if (left == ParityLattice.EVEN || right == ParityLattice.EVEN){
                // Even by Even is Even
                return ParityLattice.EVEN;
            } 
            else if (left == ParityLattice.ODD){
                // Odd by X = X
                return right;
            }
        }
        else if (operator instanceof ModuloOperator || operator instanceof RemainderOperator){
            // X % Even maintains pairity
            if (right == ParityLattice.EVEN){
                return left;
            }
        }

        // Either we have a division, one of the operands is top or
        // it is an unknown operator
        return top();
    }

    /**
     * Determines the satisfiability of a binary expression given the parity lattice elements of its operands.
     * 
     * @note In this implementation, we only consider equality (==) and inequality (!=) comparisons for satisfiability checks.
     * 
     * @param expression the binary expression to check
     * @param left the left operand's parity lattice element
     * @param right the right operand's parity lattice element
     * @param pp the program point where the expression is evaluated
     * @param oracle the semantic oracle for additional information
     * 
     * @return the satisfiability of the expression given the parity information
     */
	@Override
    public Satisfiability satisfiesBinaryExpression(
            BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle
    ){
        if (left.isBottom() || right.isBottom()){
            return Satisfiability.BOTTOM;
        }

        BinaryOperator operator = expression.getOperator();

        if (operator instanceof ComparisonEq){
            // Use eq() definide in ParityLattice
            return left.eq(right);
        } 
        else if (operator instanceof ComparisonNe){
            // If checking != we invert result 
            return left.eq(right).negate();
        } 
        else {
            // other kinds of comparisons are not decidable
            return Satisfiability.UNKNOWN;
        }
    }

    /**
     * Assumes a binary expression in the given environment.
     * 
     * @note This method only handles assumptions for equality (==) and inequality (!=) comparisons 
     *  where one operand is a variable and the other is an expression that can be evaluated to a parity lattice element (EVEN or ODD).
     * 
     * @param environment the value environment
     * @param expression the binary expression to assume
     * @param src the source program point
     * @param dest the destination program point
     * @param oracle the semantic oracle
     * 
     * @return the updated value environment
     * 
     * @throws SemanticException if an error occurs during semantic analysis
     */
    @Override
    public ValueEnvironment<ParityLattice> assumeBinaryExpression(
            ValueEnvironment<ParityLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, 
            SemanticOracle oracle
    ) throws SemanticException {
        
        BinaryOperator operator = expression.getOperator();
        
        // If we cannot infer anything from the operator, we return the environment unchanged
        if (!(operator instanceof ComparisonEq) && !(operator instanceof ComparisonNe)) {
            return environment;
        }

        // Else gather expressions and boolean flag for equality vs inequality
        ParityLattice inferred;
        Identifier id;
        ValueExpression leftExp = ((ValueExpression) expression.getLeft());
        ValueExpression rightExp = ((ValueExpression) expression.getRight());
        boolean isEq = (operator instanceof ComparisonEq);

        // Left is an identifier (example: x == 2, or x != 2)
        if (leftExp instanceof Identifier) {
            // Evaluate the right expression to get its parity
            ParityLattice rightVal = this.eval(environment, rightExp, src, oracle);
            
            // Proceed only if we have parity info
            if (rightVal == ParityLattice.EVEN || rightVal == ParityLattice.ODD) {
                id = ((Identifier) leftExp);
                
                if (isEq) {
                    // If is Equality Operator we keep the parity as is
                    inferred = rightVal;
                } else {
                    // Else we invert parity
                    if (rightVal == ParityLattice.EVEN) {
                        inferred = ParityLattice.ODD;
                    } else {
                        inferred = ParityLattice.EVEN;
                    }
                }

                environment = environment.putState(id, environment.getState(id).glb(inferred));
            }
        }
        
        // Right is an identifier (example: 2 == x, or 2 != x)
        if (rightExp instanceof Identifier) {
            // Evaluate the left expression to get its parity
            ParityLattice leftVal = this.eval(environment, leftExp, src, oracle);
            
            // Proceed only if we have pairity info
            if (leftVal == ParityLattice.EVEN || leftVal == ParityLattice.ODD) {
                id = ((Identifier) rightExp);
                
                if (isEq) {
                    // If is Equality Operator we keep the parity as is
                    inferred = leftVal;
                } else {
                    // Else we invert parity
                    if (leftVal == ParityLattice.EVEN) {
                        inferred = ParityLattice.ODD;
                    } else {
                        inferred = ParityLattice.EVEN;
                    }
                }
                
                environment = environment.putState(id, environment.getState(id).glb(inferred));
            }
        }
        
        return environment;
    }
}