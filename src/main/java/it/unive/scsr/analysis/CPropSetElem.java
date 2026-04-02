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

/**
 * @author Alan Dal Col 895879
 * @author Mattia Acquilesi 896827
 */
public class CPropSetElem implements DataflowElement<CPropSetElem> {

    private Identifier id;
    private Integer constant;

    public CPropSetElem(Identifier id, Integer constant) {
        this.id = id;
        this.constant = constant;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return List.of();
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier identifier, Identifier identifier1) {
        return null;
    }

    @Override
    public CPropSetElem pushScope(ScopeToken scopeToken, ProgramPoint programPoint) throws SemanticException {
        return null;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scopeToken, ProgramPoint programPoint) throws SemanticException {
        return null;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(new StringRepresentation(id), new StringRepresentation(constant));
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CPropSetElem that = (CPropSetElem) o;
        return Objects.equals(id, that.id) && Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, constant);
    }
}
