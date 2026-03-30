package it.unive.scsr.analysis;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class CPropSetElem  implements DataflowElement<CPropSetElem> {
    private final Identifier id;
    private final Integer constant;

    public CPropSetElem(Identifier id, Integer constant) {
        this.id = id;
        this.constant = constant;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(this.id),
                new StringRepresentation(this.constant));
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(this.id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        // Single element, we can just create a new one and be happy.
        return new CPropSetElem(target, this.constant);
    }

    @Override
    public CPropSetElem pushScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public String toString() {
        return this.representation().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        CPropSetElem rhs = (CPropSetElem) o;
        return Objects.equals(this.id, rhs.id) && Objects.equals(this.constant, rhs.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.constant);
    }

    // I need this for the case y = x and x is already saved with a constant
    public Integer getConstant() {
        return this.constant;
    }
}
