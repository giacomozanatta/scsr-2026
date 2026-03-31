
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

    public CPropSetElem(Identifier id, Integer value) {
        this.id = id;
        this.constant = value;
    }

    public Identifier getId() {
        return id;
    }

    public Integer getConstant() {
        return constant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, constant);
    }

    @Override
    public String toString() {
        return representation().toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        CPropSetElem that = (CPropSetElem) other;
        return Objects.equals(id, that.id) && Objects.equals(constant, that.constant);
    }


    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        if (this.id != null && this.id.equals(source)) {
            return new CPropSetElem(target, this.constant);
        }
        return this;
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
    public StructuredRepresentation representation() {
        return new ListRepresentation(new StringRepresentation(id), new StringRepresentation(constant));
    }
}
