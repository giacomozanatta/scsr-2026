package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class ReachingDefinitionSet implements DataflowElement<ReachingDefinitionSet> {

    private Identifier id;

    private CodeLocation location;

    public Identifier getId() {
        return id;
    }

    public CodeLocation getLocation() {
        return location;
    }
    public ReachingDefinitionSet(Identifier id, CodeLocation location) {
        this.id = id;
        this.location = location;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    @Override
    public ReachingDefinitionSet replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }

    @Override
    public ReachingDefinitionSet pushScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public ReachingDefinitionSet popScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(new StringRepresentation(id), new StringRepresentation(location));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReachingDefinitionSet that = (ReachingDefinitionSet) o;
        return Objects.equals(id, that.id) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location);
    }
}
