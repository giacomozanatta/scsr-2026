package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.*;

public class CPropSetElem implements DataflowElement<CPropSetElem> {
    private final Identifier id;          // the variable
    private final Integer constant;       // the value of the  variable (must be an integer)

    public CPropSetElem(Identifier id, Integer constant) {
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
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
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
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        if (this.id.equals(target)) {
            return new CPropSetElem(target, this.constant);
        }
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CPropSetElem)) {
            return false;
        }
        CPropSetElem o = (CPropSetElem) other;
        return this.id.equals(o.id) && this.constant.equals(o.constant);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.constant);
        return hash;
    }

    @Override
    public String toString() {
        return "CPropSetElem{" + "id=" + id + ", constant=" + constant + '}';
    }
}