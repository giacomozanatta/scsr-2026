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

public class CPropSetElem implements DataflowElement<CPropSetElem> {

    private final Identifier id;
    private final Integer constant;

    public CPropSetElem(Identifier id, Integer constant) {
        this.id = id;
        this.constant = constant;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CPropSetElem other)) {
            return false;
        }
        return Objects.equals(id, other.id) && Objects.equals(constant, other.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, constant);
    }

    @Override
    public String toString() {
        return id + "=" + constant;
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        if (this.id.equals(source)) {
            return new CPropSetElem(target, this.constant);
        }
        return this;
    }

    @Override
    public CPropSetElem pushScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }

    public Identifier getId() {
        return id;
    }

    public Integer getConstant() {
        return constant;
    }
}
