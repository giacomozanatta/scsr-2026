package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CPropSetElem implements DataflowElement<CPropSetElem> {

    private Identifier id;

    private Integer costant;

    public Identifier getId() {
        return id;
    }

    public Integer getCostant() {
        return costant;
    }

    public CPropSetElem(Identifier id, Integer costant) {
        this.id = id;
        this.costant = costant;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CPropSetElem that = (CPropSetElem) o;
        return Objects.equals(id, that.id) && Objects.equals(costant, that.costant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, costant);
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return List.of();
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }

    @Override
    public CPropSetElem pushScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public CPropSetElem popScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return null;
    }

    @Override
    public StructuredRepresentation representation() {
        return null;
    }
    /**
     * IMPLEMENT THIS CLASS
     * the code below is outside of the scope of the course.
     * You can uncomment it to get your code to compile.
     * Be aware that the code is written expecting that a field named "id"
     * and a field named "constant" exist in this class.
     */


}
