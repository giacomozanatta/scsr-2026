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

/**
 * @param id IMPLEMENT THIS CLASS
 *           the code below is outside of the scope of the course.
 *           You can uncomment it to get your code to compile.
 *           Be aware that the code is written expecting that a field named "id"
 *           and a field named "constant" exist in this class.
 */
public record CPropSetElem(Identifier id,
                           Integer constant) implements DataflowElement<CPropSetElem> /* implements ... */ {

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
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
}
