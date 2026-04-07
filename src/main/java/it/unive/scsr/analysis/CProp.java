package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

        if (value != null) {
            return Collections.singleton(new CPropSetElem(id, value));
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> set = new HashSet<>();
        for (CPropSetElem elem : state.getDataflowElements()) {
            if (elem.getId().equals(id)) {
                set.add(elem);
            }
        }
        return set;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return  Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    private Integer evaluate(ValueExpression expression, DefiniteSet<CPropSetElem> state) throws SemanticException {

        // First case: expression is a constant
        if  (expression instanceof Constant) {
            Object value = ((Constant) expression).getValue();
            if (value instanceof Integer) {
                return (Integer) value;
            } else {
                return null;
            }
        }

        // Second case: expression is a variable
        if (expression instanceof Identifier id) {
            for (CPropSetElem elem : state.getDataflowElements()) {
                if (elem.getId().equals(id)) {
                    return elem.getConstant();
                }
            }
            return null;
        }

        if (expression instanceof BinaryExpression expr) {
            Integer left = evaluate((ValueExpression) expr.getLeft(), state);
            Integer right = evaluate((ValueExpression) expr.getRight(), state);

            if (left != null && right != null) {
                return applyOperator(expr.getOperator(), left, right);
            } else {
                return null;
            }
        }

        return null;
    }

    private Integer applyOperator(Object operator, int left, int right) {
        if (operator instanceof AdditionOperator) {
            return left + right;
        } else if (operator instanceof SubtractionOperator) {
            return left - right;
        } else if (operator instanceof MultiplicationOperator) {
            return left * right;
        } else if (operator instanceof DivisionOperator) {
            if (right != 0) {
                return left / right;
            } else {
                return null;
            }
        }
        return null;
    }
}