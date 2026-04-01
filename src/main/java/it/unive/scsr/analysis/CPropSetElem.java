package it.unive.scsr.analysis;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.lisa.analysis.ScopeToken;


public class CPropSetElem implements DataflowElement<CPropSetElem>
{
    private final Identifier id;
    private final Integer constant;

    CPropSetElem(Identifier id, Integer constant)
    {
        this.id = id;
        this.constant = constant;
    }

    /**
     * IMPLEMENT THIS CLASS
     * the code below is outside of the scope of the course.
     * You can uncomment it to get your code to compile.
     * Be aware that the code is written expecting that a field named "id"
     * and a field named "constant" exist in this class.
     */
    
    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public String toString() {
        return representation().toString();
    }

    @Override
    public boolean equals(Object a)
    {
        if(this==a)
            return true;

        if(a==null)
            return false; 

        if (getClass() != a.getClass())
            return false;

        CPropSetElem other = (CPropSetElem) a;

        if(other.id == null)
            return false;

        if(this.id.equals(other.id) && Objects.equals(this.constant, other.constant))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : (id.hashCode()));
        result = prime * result + ((constant == null) ? 0 : (constant.hashCode()));
        return result;
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
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Collection<Identifier> result = new HashSet<>();
        result.add(id);

        return result;
    }

    public Identifier getId() {
        return id;
    }

    public Integer getConstant() {
        return constant;
    }
    
}
