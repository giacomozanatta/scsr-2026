package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Collection;
import java.util.Collections;

public class CProp implements DataflowElement<CProp> {
    private final Identifier id;
    private final Integer value;

    public CProp() { this(null, null); }
    public CProp(Identifier id, Integer value) { 
        this.id = id; 
        this.value = value; 
    }

    public Integer getValue() { return value; }
    public Identifier getIdentifier() { return id; }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return id == null ? Collections.emptySet() : Collections.singleton(id);
    }

    @Override
    public StructuredRepresentation representation() {
        return (id == null) ? new StringRepresentation("_") : new StringRepresentation("[" + id + "," + value + "]");
    }

    @Override
    public CProp pushScope(ScopeToken scope, ProgramPoint pp) throws SemanticException { return this; }

    @Override
    public CProp popScope(ScopeToken scope, ProgramPoint pp) throws SemanticException { return this; }

    @Override
    public CProp replaceIdentifier(Identifier source, Identifier target) { return this; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CProp)) return false;
        CProp other = (CProp) obj;
        if (id == null) return other.id == null;
        return id.equals(other.id) && ((value == null) ? other.value == null : value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return (id == null ? 0 : id.hashCode()) + (value == null ? 0 : value.hashCode());
    }
}