package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.HashSet;

public class AvailableExpressionSetElem implements DataflowElement<AvailableExpressionSetElem> {
    private final ValueExpression expression;

    AvailableExpressionSetElem(
            ValueExpression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return representation().toString();
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return getVariablesIn(expression);
    }

    private static Collection<Identifier> getVariablesIn(
            ValueExpression expression) {
        Collection<Identifier> result = new HashSet<>();

        if (expression == null)
            return result;

        if (expression instanceof Identifier)
            result.add((Identifier) expression);

        if (expression instanceof UnaryExpression)
            result.addAll(getVariablesIn((ValueExpression) ((UnaryExpression) expression).getExpression()));

        if (expression instanceof BinaryExpression binary) {
            result.addAll(getVariablesIn((ValueExpression) binary.getLeft()));
            result.addAll(getVariablesIn((ValueExpression) binary.getRight()));
        }

        if (expression instanceof TernaryExpression ternary) {
            result.addAll(getVariablesIn((ValueExpression) ternary.getLeft()));
            result.addAll(getVariablesIn((ValueExpression) ternary.getMiddle()));
            result.addAll(getVariablesIn((ValueExpression) ternary.getRight()));
        }

        return result;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        return result;
    }

    @Override
    public boolean equals(
            Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AvailableExpressionSetElem other = (AvailableExpressionSetElem) obj;
        if (expression == null) {
            return other.expression == null;
        } else return expression.equals(other.expression);
    }

    @Override
    public StructuredRepresentation representation() {
        return new StringRepresentation(expression);
    }

    @Override
    public AvailableExpressionSetElem pushScope(
            ScopeToken scope,
            ProgramPoint pp)
            throws SemanticException {
        return this;
    }

    @Override
    public AvailableExpressionSetElem popScope(
            ScopeToken scope,
            ProgramPoint pp)
            throws SemanticException {
        return this;
    }

    @Override
    public AvailableExpressionSetElem replaceIdentifier(
            Identifier source,
            Identifier target) {
        return null;
    }

}