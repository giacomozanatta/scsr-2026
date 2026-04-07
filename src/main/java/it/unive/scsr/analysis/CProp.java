package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>,CPropSetElem>{
    // IMPLEMENTATION NOTE:

    // - Implement your solution using the DefiniteSet.
    // - What would happen if you used a PossibleSet  instead? Think about it (or try it), but remember to deliver the Definite version.
    // - Keep it simple: track only integer values. Any non-integer values should be ignored.
    // - To test your implementation, you can use the inputs/cprop.imp file or define your own test cases.
    // - Refer to the Java test methods discussed in class and adjust them accordingly to work with your domain.
    // - Constant is subclass of ValueExpression
    // - How should integer constant values be propagated?
    // - Consider the following code snippet:
    //       1. x = 1
    //       2. y = x + 2
    //     The expected output should be:
    //       1. [x,1]
    //       2. [x,1] [y,3]
    // - How can you retrieve the constant value of `x` to use at program point 2?
    // - When working with an object of type `Constant`, you can obtain its value by calling the `getValue()` method.

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer constValue = evaluateValueExpression(state,expression);

        if (constValue == null) return Collections.emptySet();
        else return Collections.singleton(new CPropSetElem(id,constValue));
    }

    private Integer evaluateValueExpression(DefiniteSet<CPropSetElem> state,ValueExpression expression){
        Integer constValue = null;

        if(expression instanceof Constant constant){
            constValue=evaluateIntegerConstant(constant,state);
        } else if (expression instanceof Identifier identifier){
            constValue=this.getValueIfPresentInState(identifier,state);
        } else if (expression instanceof UnaryExpression unaryExpression) {
            constValue=this.evaluateUnaryExpression(unaryExpression,state);
        } else if (expression instanceof BinaryExpression binaryExpression){
            constValue = evaluateBinaryExpression(binaryExpression,state);
        }

        return constValue;
    }

    private Integer evaluateIntegerConstant(Constant constant, DefiniteSet<CPropSetElem> state){
        Integer res = null;
        if(constant.getValue() instanceof Integer value){
            res = value;
        }

        return res;
    }

    private Integer evaluateValueSymbolicExpression(SymbolicExpression symbolicExpression, DefiniteSet<CPropSetElem> state){
        Integer res = null;

        if(symbolicExpression instanceof ValueExpression valueExpression){
            res = evaluateValueExpression(state,valueExpression);
        }

        return res;
    }

    private Integer evaluateUnaryExpression(UnaryExpression unaryExpression, DefiniteSet<CPropSetElem> state){
        Integer res = null;
        UnaryOperator operator = unaryExpression.getOperator();

        if(operator instanceof NumericNegation){
            res = this.evaluateValueSymbolicExpression(unaryExpression.getExpression(),state);
            if ( res != null) res = -res;
        }

        return res;
    }

    private Integer evaluateBinaryExpression(BinaryExpression binaryExpression,DefiniteSet<CPropSetElem> state){
        Integer res = null;

        SymbolicExpression leftSe = binaryExpression.getLeft();
        SymbolicExpression rightSe = binaryExpression.getRight();
        BinaryOperator operator = binaryExpression.getOperator();

        Integer left = this.evaluateValueSymbolicExpression(leftSe,state);
        Integer right = this.evaluateValueSymbolicExpression(rightSe,state);;

        if(left !=null && right!=null){
            if (operator instanceof AdditionOperator) {
                res = left + right;
            } else if (operator instanceof SubtractionOperator) {
                res = left - right;
            } else if (operator instanceof MultiplicationOperator) {
                res = left * right;
            } else if (operator instanceof DivisionOperator) {
                res = left / right;
            }
        }
        return res;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> killed = new java.util.HashSet<CPropSetElem>();
        this.getIfPresentInState(id, state).findFirst().ifPresent(killed::add);

        return killed;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    private Stream<CPropSetElem> getIfPresentInState(Identifier id, DefiniteSet<CPropSetElem> state){
        return state.getDataflowElements().stream().filter(element -> element.getId().equals(id));
    }

    private Integer getValueIfPresentInState(Identifier id, DefiniteSet<CPropSetElem> state) {
        return this.getIfPresentInState(id,state).map(CPropSetElem::getConstants).findFirst().orElse(null);
    }

}