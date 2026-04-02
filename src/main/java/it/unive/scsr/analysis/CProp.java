package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    private Integer eval(ValueExpression expr, DefiniteSet<CPropSetElem> state) {
        if (expr instanceof Constant) {
            Object val = ((Constant) expr).getValue();
            if (val instanceof Integer) return (Integer) val;
        }

        if (expr instanceof Identifier) {
            for (CPropSetElem elem : state) {
                if (elem.getId().equals(expr)) return elem.getCostant();
            }
        }

        if (expr instanceof BinaryExpression binary) {
            Integer left = eval((ValueExpression) binary.getLeft(), state);
            Integer right = eval((ValueExpression) binary.getRight(), state);

            if (left != null && right != null) {
                if (binary.getOperator() != null) {
                    String str = binary.getOperator().toString();
                    switch (str) {
                        case "+" -> {
                            return left + right;
                        }
                        case "-" -> {
                            return left - right;
                        }
                        case "*" -> {
                            return left * right;
                        }
                        case "/" -> {
                            if(right != 0) return left / right;
                        }
                    }
                }
            }
        }

        if (expr instanceof UnaryExpression unary) {
            Integer exp = eval((ValueExpression) unary.getExpression(), state);
            if (exp != null) {
                UnaryOperator op = unary.getOperator();
                if (op instanceof NumericNegation) {
                    return -exp;
                }
            }
        }

        return null;

    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer value = eval(expression, state);
        if (value != null) {
            return Collections.singleton(new CPropSetElem(id, value));
        }
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> toKill = new HashSet<>();
        for (CPropSetElem elem : state) {
            if (elem.getId().equals(id)) {
                toKill.add(elem);
            }
        }
        return toKill;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

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