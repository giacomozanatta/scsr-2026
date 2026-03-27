package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;

import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AvailableExpressions
        extends
        DataflowDomain<DefiniteSet<AvailableExpressionSetElem>, AvailableExpressionSetElem> {

    @Override
    public DefiniteSet<AvailableExpressionSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    @Override
    public Set<AvailableExpressionSetElem> gen(
            DefiniteSet<AvailableExpressionSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) {
        Set<AvailableExpressionSetElem> result = new HashSet<>();
        AvailableExpressionSetElem ae = new AvailableExpressionSetElem(expression);
        if (!ae.getInvolvedIdentifiers().contains(id) && filter(expression))
            result.add(ae);
        return result;
    }

    @Override
    public Set<AvailableExpressionSetElem> gen(
            DefiniteSet<AvailableExpressionSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) {
        Set<AvailableExpressionSetElem> result = new HashSet<>();
        AvailableExpressionSetElem ae = new AvailableExpressionSetElem(expression);
        if (filter(expression))
            result.add(ae);
        return result;
    }

    private static boolean filter(
            ValueExpression expression) {
        if (expression instanceof Identifier)
            return false;
        if (expression instanceof Constant)
            return false;
        if (expression instanceof Skip)
            return false;
        return !(expression instanceof PushAny);
    }

    @Override
    public Set<AvailableExpressionSetElem> kill(
            DefiniteSet<AvailableExpressionSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) {
        Set<AvailableExpressionSetElem> result = new HashSet<>();

        for (AvailableExpressionSetElem ae : state.getDataflowElements()) {
            Collection<Identifier> ids = ae.getInvolvedIdentifiers();

            if (ids.contains(id))
                result.add(ae);
        }

        return result;
    }

    @Override
    public Set<AvailableExpressionSetElem> kill(
            DefiniteSet<AvailableExpressionSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) {
        return Collections.emptySet();
    }
}
