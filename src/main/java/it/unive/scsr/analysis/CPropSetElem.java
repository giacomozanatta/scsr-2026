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

/**
 * A single element of the Constant Propagation dataflow set.
 *
 * Represents a pair (variable, constant), meaning that at a given
 * program point the variable is guaranteed to hold that integer value.
 *
 * Example: (x, 5) means "x is definitely equal to 5 here".
 */
public class CPropSetElem implements DataflowElement<CPropSetElem> {

    /** The variable being tracked. */
    private final Identifier id;

    /** The constant integer value known to be held by {@code id}. */
    private final Integer value;

    /**
     * Creates a new (variable, constant) pair.
     *
     * @param id    the variable
     * @param value its known constant value
     */
    public CPropSetElem(Identifier id, Integer value) {
        this.id = id;
        this.value = value;
    }

    /** Returns the variable being tracked. */
    public Identifier getId() { return id; }

    /** Returns the constant value associated with the variable. */
    public Integer getValue() { return value; }

    /**
     * Returns the set of identifiers involved in this element.
     * Since each element tracks exactly one variable, this is a singleton.
     */
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    /**
     * Returns a structured representation of this element as [variable, value].
     * Used for analysis output and debugging.
     */
    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(value));
    }

    /**
     * Scope handling — this element does not change when entering a new scope.
     */
    @Override
    public CPropSetElem pushScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }

    /**
     * Scope handling — this element does not change when leaving a scope.
     */
    @Override
    public CPropSetElem popScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {
        return this;
    }

    /**
     * If this element tracks {@code source}, returns a new element tracking
     * {@code target} with the same constant value. Otherwise returns itself.
     *
     * Used when variables are renamed during analysis (e.g. scoping).
     */
    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        if (id.equals(source))
            return new CPropSetElem(target, value);
        return this;
    }

    /**
     * Two elements are equal if they track the same variable with the same value.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPropSetElem that = (CPropSetElem) o;
        return Objects.equals(id, that.id) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return representation().toString();
    }
}