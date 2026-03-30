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
import java.util.List;
import java.util.Objects;

public class CPropSetElem implements DataflowElement<CPropSetElem> {
    /**
     * IMPLEMENT THIS CLASS
     * the code below is outside of the scope of the course.
     * You can uncomment it to get your code to compile.
     * Be aware that the code is written expecting that a field named "id"
     * and a field named "constant" exist in this class.
     */

    private final Identifier id;
    private final Integer constant;

    public CPropSetElem(Identifier id, Integer constant) {
        this.id = id;
        this.constant = constant;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public CPropSetElem pushScope(ScopeToken scope, ProgramPoint programPoint)
            throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scope, ProgramPoint programPoint)
            throws SemanticException {
        return this;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return List.of(id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        if(id.equals(source)){ // single element so source should be the same as id otherwise that would be awkward
            return new CPropSetElem(target, constant);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPropSetElem that = (CPropSetElem) o;
        return Objects.equals(id, that.id) && Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, constant);
    }

    @Override
    public String toString() {
        return representation().toString();
    }

    public Identifier getId(){
        return id;
    }

    public Integer getConstant(){
        return constant;
    }
}
