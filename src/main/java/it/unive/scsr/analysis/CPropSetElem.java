package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;

import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Constant;

import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;


/**
 * Class representing an element of the set of the constant propagation analysis.
 *
 * @note Docstrings are mostly auto-generated and may need to be revised for clarity and accuracy. This is becaus I had some difficullties to make sense of the surrounding code.
 * 
 * @author Gianmaria Pizzo 872966
 */
public class CPropSetElem implements DataflowElement<CPropSetElem>{
    // ================================================================
    // Fields

    private final Identifier id;        /// Identifier
    private final Integer constant;     /// Integer Constant

    // ================================================================
    // Constructors

    /**
     * Constructs a new Constant Propagation element with null values.
     * 
     */
    public CPropSetElem() {
        this.id = null;
        this.constant = null;
    }

    /**
     * Constructs a new Constant Propagation element.
     *
     * @param id the identifier
     * @param constant the constant integer value assigned to the identifier
     *
     */
    public CPropSetElem(Identifier id, Integer constant) {
        super();
        this.id = id;
        this.constant = constant;
    }

    // ================================================================
    // Given Methods (Untouched)

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

    // NOTE: I noticed the task template for pushScope does not match the current ScopedObject implementation.
    //  Therefore, I just changed the method signature.

    @Override
    public CPropSetElem pushScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return this;
    }

    // NOTE: I noticed the task template for popScope does not match the current ScopedObject implementation.
    //  Therefore, I just changed the method signature.

    @Override
    public CPropSetElem popScope(ScopeToken token, ProgramPoint pp) throws SemanticException {
        return this;
    }

    // ================================================================
    // Methods implemented for Constant Propagation Task

    /**
     * Replaces an identifier with another in this element.
     * 
     * @param source the identifier to be replaced
     * @param target the identifier to replace with
     * 
     * @return a new element with the identifier replaced, or null if the replacement is not applicable.
     */
    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        return null;    // As for AvailableExpressionSetElem and ReachingDefinitionSet.
    }

    /**
     * Returns a collection of identifiers involved in this element.
     * 
     * @return a collection of identifiers involved in this element
     */
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    /**
     * Getter for the identifier of this element.
     * 
     * @return the identifier of this element
     */
    public Identifier getId(){
        return id;
    }

    /**
     * Getter for the constant value of this element.
     * 
     * @return the constant value of this element
     */
    public Integer getConstant(){
        return constant;
    }

    /**
     * Returns a string representation of this element.
     * 
     * @return a string representation of this element
     */
    @Override
    public String toString() {
        return representation().toString();
    }

    /**
     * Checks if this element is equal to another object.
     * 
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        if (o == null || getClass() != o.getClass()){
            return false;
        }

        CPropSetElem that = (CPropSetElem) o;

        return Objects.equals(id, that.id) && Objects.equals(constant, that.constant);
    }

    /**
     * Returns the hash code of this element.
     * 
     * @return the hash code of this element
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, constant);
    }

}