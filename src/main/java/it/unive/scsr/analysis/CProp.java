package it.unive.scsr.analysis;

import java.util.Collections;
import java.util.Set;

import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.ArithmDiv;
import it.unive.lisa.symbolic.value.operator.binary.ArithmMul;
import it.unive.lisa.symbolic.value.operator.binary.ArithmSub;
import it.unive.lisa.symbolic.value.operator.binary.ArithmSum;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

/**
 * Constant Propagation implementation.
 * It tracks which variables have a fixed integer value at each program point.
 */
public class CProp implements DataflowElement<CProp, ValueExpression> {

    private final Identifier id;
    private final Integer value;

    // Default constructor for the bottom state
    public CProp() {
        this(null, null);
    }

    // Constructor to store a variable and its constant value
    private CProp(Identifier id, Integer value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public Set<CProp> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DataflowDomain<CProp> domain) {
        // Try to evaluate the expression to a constant
        Integer val = eval(expression, domain);
        if (val != null) {
            // If it's a constant, generate a new pair [id, value]
            return Collections.singleton(new CProp(id, val));
        }
        return Collections.emptySet();
    }

    @Override
    public Set<CProp> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DataflowDomain<CProp> domain) {
        // When a variable is reassigned, we kill its previous constant value
        return Collections.singleton(new CProp(id, null));
    }

    /**
     * Helper method to evaluate an expression.
     * It handles constants, variables, and basic arithmetic.
     */
    private Integer eval(ValueExpression expression, DataflowDomain<CProp> domain) {
        // 1. Literal constants (e.g., x = 5)
        if (expression instanceof Constant && ((Constant) expression).getValue() instanceof Integer) {
            return (Integer) ((Constant) expression).getValue();
        }

        // 2. Variables (e.g., y = x)
        if (expression instanceof Identifier) {
            for (CProp element : domain.getDataflowElements()) {
                if (element.id.equals(expression)) {
                    return element.value;
                }
            }
        }

        // 3. Unary operations (e.g., y = -x)
        if (expression instanceof UnaryExpression) {
            UnaryExpression un = (UnaryExpression) expression;
            Integer subValue = eval(un.getExpression(), domain);
            if (subValue != null && un.getOperator() instanceof NumericNegation) {
                return -subValue;
            }
        }

        // 4. Binary operations (e.g., z = x + y)
        if (expression instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) expression;
            Integer left = eval(bin.getLeft(), domain);
            Integer right = eval(bin.getRight(), domain);

            if (left != null && right != null) {
                var op = bin.getOperator();
                if (op instanceof ArithmSum) return left + right;
                if (op instanceof ArithmSub) return left - right;
                if (op instanceof ArithmMul) return left * right;
                if (op instanceof ArithmDiv && right != 0) return left / right;
            }
        }

        return null; // Not a constant or calculation failed
    }

    @Override
    public DomainRepresentation representation() {
        return (id == null) ? new StringRepresentation("_") : new StringRepresentation(id + ":" + value);
    }

    // Standard equals and hashCode for the analysis to work correctly
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CProp other = (CProp) obj;
        if (id == null) return other.id == null;
        if (!id.equals(other.id)) return false;
        if (value == null) return other.value == null;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }
}