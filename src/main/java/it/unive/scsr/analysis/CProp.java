package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
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
import java.util.Objects;
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

    public static Integer handler(DefiniteSet<CPropSetElem> state, ValueExpression expression) {

        if(expression instanceof Constant constant) {
            Object value = ((Constant) constant).getValue();

            return value instanceof Integer ? (Integer) value : null;
        }
        else if(expression instanceof Identifier identifier) {
            for(CPropSetElem elem : state.getDataflowElements()) {
                if(elem.getId().equals(identifier)) {
                    return elem.getConstant();
                }
            }
            return null;
        }
        else if(expression instanceof UnaryExpression exp) {
            UnaryOperator op = exp.getOperator();
            ValueExpression value = (ValueExpression) exp.getExpression();

            Integer num = handler(state, value);

            if(op instanceof NumericNegation) {
                return num != null ? -num : null;
            }
            return null;
        }
        else if(expression instanceof BinaryExpression bin) {
            BinaryOperator op = bin.getOperator();
            ValueExpression left = (ValueExpression) bin.getLeft();
            ValueExpression right = (ValueExpression) bin.getRight();

            Integer leftValue = handler(state, left);
            Integer rightValue = handler(state, right);

            if(leftValue != null && rightValue != null) {
                if(op instanceof MultiplicationOperator) {
                    return leftValue * rightValue;
                }
                else if(op instanceof AdditionOperator) {
                    return leftValue + rightValue;
                }
                else if(op instanceof SubtractionOperator) {
                    return leftValue - rightValue;
                }
                else if(op instanceof DivisionOperator && rightValue != 0) {
                    return leftValue / rightValue;
                }
            }
            return null;

        }

        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Integer value = handler(cPropSetElems, valueExpression);

        return value != null ? Collections.singleton(new CPropSetElem(identifier, value)) : Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> killSet = new HashSet<>();

        for(CPropSetElem elem : cPropSetElems.getDataflowElements()) {
            if(elem.getId().equals(identifier)) {
                killSet.add(elem);
            }
        }

        return killSet;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

}