package it.unive.scsr.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.Constant;
import java.util.HashSet;

public class CPropSetElem implements DataflowElement<CPropSetElem>{
    /**
     * IMPLEMENT THIS CLASS
     * the code below is outside of the scope of the course.
     * You can uncomment it to get your code to compile.
     * Be aware that the code is written expecting that a field named "id"
     * and a field named "constant" exist in this class.
     */
    
    private Identifier id;
    private Integer constant;
    
    public CPropSetElem(Identifier id, Integer constant)
    {
        this.id = id;
        this.constant = constant;
    }
    
    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public Integer getConstant() {
        return constant;
    }

    public void setConstant(Integer constant) {
        this.constant = constant;
    }
    
    @Override
    public String toString() {
        return representation().toString();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null) return false;
        if(this.getClass() != o.getClass()) return false;
        CPropSetElem eo = (CPropSetElem) o;
        return Objects.equals(this.id, eo.id) && Objects.equals(this.constant, eo.constant);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.constant);
    }
      
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Collection<Identifier> result = new HashSet<>();
        result.add(id);

        return result;
    }
    
    @Override
    public CPropSetElem pushScope(ScopeToken st, ProgramPoint pp) throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken st, ProgramPoint pp) throws SemanticException {
        return this;
    }
    
    //region OutsideOfScope
    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }
    /*
    @Override
    public CPropSetElem pushScope(ScopeToken scope, ProgramPoint pp)
            throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scope)
            throws SemanticException {
        return this;
    }
    */
    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }
    //endregion
    
}
