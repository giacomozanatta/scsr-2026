package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.analysis.dataflow.PossibleSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/*
public class CProp extends ... {

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


}
*/
public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {
    public Integer evaluate(DefiniteSet<CPropSetElem> state, ValueExpression expression) {

        // case x = >5<
        if (expression instanceof Constant c) {

            Object val = c.getValue();
            if (val instanceof Integer) {
                return (Integer) val;

            }
            return null;
        }
        // case x = >y<
        if (expression instanceof Identifier id) {
            for (CPropSetElem elem : state.getDataflowElements()) {
                if (elem.getId().equals(id)) {
                    return elem.getConstant();
                }
            }
            return null;
        }
        // case x >+< y

        if (expression instanceof BinaryExpression b) {
            Integer left = evaluate(state, (ValueExpression) b.getLeft());
            Integer right = evaluate(state, (ValueExpression) b.getRight());
            if (left == null || right == null) {
                return null;
            }
            if (b.getOperator() instanceof AdditionOperator) {
                return left + right;
            }
            if (b.getOperator() instanceof SubtractionOperator) {
                return left - right;
            }
            if (b.getOperator() instanceof MultiplicationOperator) {
                return left * right;
            }
            
            if (b.getOperator() instanceof DivisionOperator) {
                //for divisions we have to check for divide by 0 
                if (right == 0) {

                    return null;
                }

                return left / right;
            }
            return null;
        }

        // >-x<
        if (expression instanceof UnaryExpression u) {
            if (u.getOperator() instanceof NumericNegation) {
                Integer inner = evaluate(state, (ValueExpression) u.getExpression());
                if (inner != null) {
                    return -inner;
                }
            }
            return null;
        }
        return null;

    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression,
            ProgramPoint pp) throws SemanticException {
        Integer val = evaluate(state, expression);
        if (val != null) {
            return Collections.singleton(new CPropSetElem(id, val));

        }
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp)
            throws SemanticException {

        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression,
            ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> result = new HashSet<>();
        for (CPropSetElem elem : state.getDataflowElements()) {
            if (elem.getId().equals(id)) {
                result.add(elem);
            }
        }

        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp)
            throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }
}