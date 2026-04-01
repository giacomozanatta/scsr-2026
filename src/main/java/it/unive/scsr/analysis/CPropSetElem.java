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

    CPropSetElem(Identifier id, Integer constant) {
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
        return Collections.singleton(id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier identifier, Identifier identifier1) {
        return this;
    }

    @Override
    public CPropSetElem pushScope(ScopeToken scopeToken, ProgramPoint programPoint) throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scopeToken, ProgramPoint programPoint) throws SemanticException {
        return this;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, constant);
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
        CPropSetElem cProp = (CPropSetElem) obj;
        return Objects.equals(this.getId(), cProp.id) && Objects.equals(this.getConstant(), cProp.constant);
    }


    /**
     * IMPLEMENT THIS CLASS
     * the code below is outside of the scope of the course.
     * You can uncomment it to get your code to compile.
     * Be aware that the code is written expecting that a field named "id"
     * and a field named "constant" exist in this class.
     */


}
