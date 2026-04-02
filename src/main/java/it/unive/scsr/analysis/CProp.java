package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    // IMPLEMENTATION NOTE:

    // - Implement your solution using the DefiniteSet.
    //   - What would happen if you used a PossibleSet instead? Think about it (or try it), but remember to deliver the Definite version.
    // - Keep it simple: track only integer values. Any non-integer values should be ignored.
    // - To test your implementation, you can use the inputs/cprop.imp file or define your own test cases.
    // - Refer to the Java test methods discussed in class and adjust them accordingly to work with your domain.
    // - Constant is subclass of ValueExpression
    // - How should integer constant values be propagated?
    //   - Consider the following code snippet:
    //       1. x = 1
    //       2. y = x + 2
    //     The expected output should be:
    //       1. [x,1]
    //       2. [x,1] [y,3]
    //   - How can you retrieve the constant value of `x` to use at program point 2?
    //   - When working with an object of type `Constant`, you can obtain its value by calling the `getValue()` method.


    /*
    * Q:What would happen if you used a PossibleSet instead? Think about it (or try it)
    * A:Since we didn't implement PossibleSet, we don't see branched values in result step (At least I guess so based on my observations).
    * Definite set shows only values that are definitely having that value
    * */
    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer value = evaluateExpression(state,expression);
        if (value == null) {return Collections.emptySet();}
        return Collections.singleton(new CPropSetElem(id,value));
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> currentSet = state.getDataflowElements();
        Set<CPropSetElem> result = new HashSet<>();
        for (CPropSetElem r : currentSet) {
            if (r.getId().equals(id)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    private Integer evaluateExpression(DefiniteSet<CPropSetElem> state, ValueExpression expression) {
        Integer value = null;

        if (expression instanceof Constant constant) {
            value = evaluateConstant(constant);
        }else if(expression instanceof Identifier id) {
            value = getConstantByIdentifier(state.getDataflowElements(), id);
        }else if (expression instanceof BinaryExpression binaryExpression) {
            value = BinaryExpressionEvaluation(state, binaryExpression);

        }else if (expression instanceof UnaryExpression unaryExpression) {
            UnaryOperator operator = unaryExpression.getOperator();
            if(operator instanceof NumericNegation){
                ValueExpression v = (ValueExpression)unaryExpression.getExpression();
                if(v instanceof Constant constant){
                    value = evaluateConstant(constant);
                }else if(v instanceof Identifier id){
                    value = getConstantByIdentifier(state.getDataflowElements(), id);
                }
                value *= -1;
            }
        }
        return value;

    }

    private Integer evaluateConstant(Constant constant) {
        Integer value = null;

        Object c  = constant.getValue();
        if(c instanceof Integer) {
            value = (Integer) c;
        }

        return value;
    }

    private Integer getConstantByIdentifier(Set<CPropSetElem> elements, Identifier id) {
        Integer value = null;
        for(CPropSetElem elem : elements) {
            if(elem.getId().equals(id)){
                value = elem.getConstant();
            }
        }
        return value;
    }

    private Integer BinaryExpressionEvaluation(DefiniteSet<CPropSetElem> state, BinaryExpression expression) {
        Integer result = null;

        SymbolicExpression l = expression.getLeft();
        Integer leftValue = null;
        if(l instanceof Constant constant) {
            leftValue = evaluateConstant(constant);
        }else if(l instanceof Identifier id) {
            leftValue = getConstantByIdentifier(state.getDataflowElements(), id);
        }

        SymbolicExpression r = expression.getRight();
        Integer rightValue = null;
        if(r instanceof Constant constant) {
            rightValue = evaluateConstant(constant);
        }else if(r instanceof Identifier id) {
            rightValue = getConstantByIdentifier(state.getDataflowElements(), id);
        }

        if(leftValue == null || rightValue == null) {
            return null;
        }

        BinaryOperator operator = expression.getOperator();

        if(operator instanceof AdditionOperator) {
            result = leftValue + rightValue;

        } else if (operator instanceof SubtractionOperator) {
            result = leftValue - rightValue;
        } else if (operator instanceof MultiplicationOperator) {
            result = leftValue * rightValue;
        }else if (operator instanceof DivisionOperator) {//check div by 0
            if(rightValue == 0){
                return result;
            }
            result = leftValue / rightValue;
        }else{
            return result;
        }
        return result;
    }
}