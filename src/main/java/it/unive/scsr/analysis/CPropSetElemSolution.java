package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class CPropSetElemSolution implements DataflowElement<CPropSetElemSolution> {

    private final Identifier id;

    private final Integer constant;

    public CPropSetElemSolution(Identifier id, Integer constant) {
        this.id = id;
        this.constant = constant;
    }

    public Identifier getId() {
        return this.id;
    }

    public Integer getConstant() {
        return this.constant;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }


    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    @Override
    public CPropSetElemSolution replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }

    @Override
    public CPropSetElemSolution pushScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public CPropSetElemSolution popScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant, id);
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
        CPropSetElemSolution other = (CPropSetElemSolution) obj;
        return Objects.equals(constant, other.getConstant()) && Objects.equals(id, other.getId());
    }
}
