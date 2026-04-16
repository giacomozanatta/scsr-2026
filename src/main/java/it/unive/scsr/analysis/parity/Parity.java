package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Collection;
import java.util.Collections;

public class Parity implements DataflowElement<Parity> {
    public enum State { EVEN, ODD }
    
    private final Identifier id;
    private final State state;

    public Parity() { this(null, null); }
    public Parity(Identifier id, State state) { 
        this.id = id; 
        this.state = state; 
    }

    public State getState() { return state; }
    public Identifier getId() { return id; }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return id == null ? Collections.emptySet() : Collections.singleton(id);
    }

    @Override
    public StructuredRepresentation representation() {
        return (id == null) ? new StringRepresentation("_") : new StringRepresentation("[" + id + "," + state + "]");
    }

    @Override public Parity pushScope(ScopeToken scope, ProgramPoint pp) throws SemanticException { return this; }
    @Override public Parity popScope(ScopeToken scope, ProgramPoint pp) throws SemanticException { return this; }
    @Override public Parity replaceIdentifier(Identifier source, Identifier target) { return this; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Parity)) return false;
        Parity other = (Parity) obj;
        if (id == null) return other.id == null;
        return id.equals(other.id) && state == other.state;
    }

    @Override
    public int hashCode() {
        return (id == null ? 0 : id.hashCode()) + (state == null ? 0 : state.hashCode());
    }
}