package it.unive.scsr.analysis;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.ReachingDefinitions;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.lisa.analysis.dataflow.DefiniteSet;

public class CPropSetElem implements DataflowElement<CPropSetElem> {

    // variables that mean id has value constant
    private final Identifier id;
    private final Integer constant;

    // building the couple id constant
    CPropSetElem() {this.id = null; this.constant = null;}
    CPropSetElem(Identifier var, Integer val) {this.id = var; this.constant = val;}

    //getters
    public Identifier getId() {return id;}
    public Integer getConstant() {return constant;}

    // for the hash set that permits us to collect the singletons and permit to efficiently check if a couple is already in the set
    @Override
    public int hashCode () {return Objects.hash(id,constant);}

    // to verify that two elements are equal in the set (== is the reference comparison, so it gives true iff the instance is the same)
    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPropSetElem ob = (CPropSetElem) o;
        return Objects.equals(id, ob.id) && Objects.equals(constant, ob.constant);
    }

    // get the indentifiers we parsed 
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {return Collections.singleton(id);}

    // we return the list with all the couples we parsed with their values, to test if it works 
    @Override
    public StructuredRepresentation representation() {return new ListRepresentation(new StringRepresentation(id),new StringRepresentation(constant));}

    // we obtain the value of an id
    private static Integer getIdValue(Identifier id, DefiniteSet<CPropSetElem> set) {
        for (CPropSetElem el : set.getDataflowElements()) if (el.id.equals(id)) return el.constant;
        return null;
    }

    public CPropSetElem pushScope(ScopeToken scope,ProgramPoint pp) throws SemanticException {return this;}

    public CPropSetElem popScope(ScopeToken scope, ProgramPoint pp) throws SemanticException {return this;}

    public CPropSetElem replaceIdentifier(Identifier source, Identifier target) {return null;}

}