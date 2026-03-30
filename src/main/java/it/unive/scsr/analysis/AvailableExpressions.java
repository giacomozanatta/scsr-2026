package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;import java.util.Set;

public class AvailableExpressions extends DataflowDomain<DefiniteSet<AvailableExpressionSetElem>,AvailableExpressionSetElem> {

    @Override
    public Set<AvailableExpressionSetElem> gen(DefiniteSet<AvailableExpressionSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<AvailableExpressionSetElem> result = new HashSet<>();
        AvailableExpressionSetElem ae = new AvailableExpressionSetElem(expression);
        if (!ae.getInvolvedIdentifiers().contains(id) && AvailableExpressions.filter(expression)) {
            result.add(ae);
        }
        return result;
    }

    @Override
    public Set<AvailableExpressionSetElem> gen(DefiniteSet<AvailableExpressionSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<AvailableExpressionSetElem> result = new HashSet<>();
        AvailableExpressionSetElem ae = new AvailableExpressionSetElem(expression);
        if(AvailableExpressions.filter(expression)) {
            result.add(ae);
        }
        return result;
    }

    private static boolean filter(ValueExpression expression) {
        if (expression instanceof Identifier)
            return false;

        if (expression instanceof Constant)
            return false;

        if (expression instanceof Skip)
            return false;

        if (expression instanceof PushAny)
            return false;

        return true;
    }

    @Override
    public Set<AvailableExpressionSetElem> kill(DefiniteSet<AvailableExpressionSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<AvailableExpressionSetElem> result = new HashSet<>();

        for (AvailableExpressionSetElem ae : state.getDataflowElements()) {
            Collection<Identifier> ids = ae.getInvolvedIdentifiers();

            if (ids.contains(id)){
                result.add(ae);
            }
        }

        return result;
    }

    @Override
    public Set<AvailableExpressionSetElem> kill(DefiniteSet<AvailableExpressionSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<AvailableExpressionSetElem> makeLattice() {
        return new DefiniteSet<>();

    }}
