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

    /**
     * IMPLEMENT THIS CLASS
     * the code below is outside of the scope of the course.
     * You can uncomment it to get your code to compile.
     * Be aware that the code is written expecting that a field named "id"
     * and a field named "constant" exist in this class.
    */

    private final Identifier id;
    private final Integer constant;

    public CPropSetElem(Identifier id, Integer constant){
        this.id = id;
        this.constant = constant;
    }

    public Identifier getId(){
        return this.id;
    }

    public Integer getConstants(){
        return this.constant;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public CPropSetElem pushScope(ScopeToken scope, ProgramPoint pp)
            throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scope, ProgramPoint pp)
            throws SemanticException {
        return this;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(this.id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.constant);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        else if (this == o) return true;
        else{
            CPropSetElem other = (CPropSetElem) o;
            return Objects.equals(this.id,other.id) && Objects.equals(this.constant,other.constant);
        }
    }
}