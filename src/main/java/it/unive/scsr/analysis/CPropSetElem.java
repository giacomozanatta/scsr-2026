package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.List;

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

    public Integer getValue () {
        return constant;
    }

    public Identifier getId () {
        return id;
    }

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
    public Collection<Identifier> getInvolvedIdentifiers() {
        return List.of(id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        return null;
    }

    @Override
    public CPropSetElem pushScope(ScopeToken token, ProgramPoint pp) {
        return null;
    }

    @Override
    public CPropSetElem popScope(ScopeToken token, ProgramPoint pp) {
        return null;
    }
}
