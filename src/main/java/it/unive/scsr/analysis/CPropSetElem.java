package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    public CPropSetElem() {this(null, null);}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPropSetElem that = (CPropSetElem) o;
        return Objects.equals(id, that.id) && Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, constant);
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(
                new StringRepresentation(id),
                new StringRepresentation(constant));
    }

   /* @Override
    public CPropSetElem pushScope(ScopeToken scope)
            throws SemanticException {
        return this;
    }

    @Override
    public CPropSetElem popScope(ScopeToken scope)
            throws SemanticException {
        return this;
    }*/

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return Collections.singleton(id);
    }

    @Override
    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {
        if(this.id != null && this.id.equals(source)) {
            return new CPropSetElem(target, this.constant);
        }
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

    private static Integer getValue(Identifier id, Iterable<CPropSetElem> el) {
        if(el == null)
            return null;
        for(CPropSetElem e : el) {
            if(e.id != null && e.id.equals(id)){
                return e.constant;
            }
        }
        return null;
    }

    public static Integer eval(ValueExpression exp, Iterable<CPropSetElem> el){
        if(exp == null)
            return null;
        if(exp instanceof Constant){
            Object val = ((Constant) exp).getValue();
            if(val instanceof Integer) return ((Integer) val);
        }
        if(exp instanceof Identifier){
            Identifier id = (Identifier) exp;
            return getValue(id, el);
        }
        if(exp instanceof BinaryExpression){
            BinaryExpression b = (BinaryExpression) exp;
            BinaryOperator op = b.getOperator();
            ValueExpression left = (ValueExpression) b.getLeft();
            ValueExpression right = (ValueExpression) b.getRight();
            Integer rval = eval(right, el);
            Integer lval = eval(left, el);

            if(rval == null || lval == null)
                return null;
            if(op instanceof AdditionOperator){
                return rval + lval;
            }
            if(op instanceof MultiplicationOperator){
                return rval * lval;
            }
            if(op instanceof DivisionOperator){
                if(rval == 0) return null;
                return lval / rval;
            }
            if(op instanceof SubtractionOperator){
                return lval - rval;
            }
            return null;
        }

        if(exp instanceof UnaryExpression){
            UnaryExpression u = (UnaryExpression) exp;
            UnaryOperator op = u.getOperator();
            Integer value = eval((ValueExpression) u.getExpression(), el);

            if(value == null)
                return null;
            if(op instanceof NumericNegation){
                return -value;
            }
        }
        return null;
    }

    public Identifier getId() {
        return id;
    }

    public Integer getConstant() {
        return constant;
    }
}