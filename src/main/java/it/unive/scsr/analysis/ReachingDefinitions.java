package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;

import it.unive.lisa.analysis.dataflow.PossibleSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ReachingDefinitions extends DataflowDomain<PossibleSet<ReachingDefinitionSet>, ReachingDefinitionSet> {

    @Override
    public Set<ReachingDefinitionSet> gen(PossibleSet<ReachingDefinitionSet> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.singleton(new ReachingDefinitionSet(id, pp.getLocation()));
    }

    @Override
    public Set<ReachingDefinitionSet> gen(PossibleSet<ReachingDefinitionSet> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<ReachingDefinitionSet> kill(PossibleSet<ReachingDefinitionSet> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<ReachingDefinitionSet> currentSet = state.getDataflowElements();
        Set<ReachingDefinitionSet> result = new HashSet<>();
        for (ReachingDefinitionSet r : currentSet) {
            if (r.getId().equals(id)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public Set<ReachingDefinitionSet> kill(PossibleSet<ReachingDefinitionSet> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public PossibleSet<ReachingDefinitionSet> makeLattice() {
        return new PossibleSet<>();
    }}