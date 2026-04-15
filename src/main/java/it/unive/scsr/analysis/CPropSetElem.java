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
import java.util.List;
import java.util.Objects;

public class CPropSetElem implements DataflowElement<CPropSetElem> {

    private final Identifier id;
    private final Integer constant;

    public CPropSetElem(Identifier id, Integer constant) {
        this.id = id;
        this.constant = constant;
    }

    public Identifier getId() {
        return id;
    }

    public Integer getConstant() {
        return constant;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        if (id == null)
            return Collections.emptyList();

        return List.of(id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        if (Objects.equals(id, source))
            return new CPropSetElem(target, constant);

        return this;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public CPropSetElem pushScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }
}
