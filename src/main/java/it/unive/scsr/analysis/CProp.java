package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.analysis.dataflow.PossibleSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alan Dal Col 895879
 * @author Mattia Acquilesi 896827
 */
public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    private Integer eval(ValueExpression expr, DefiniteSet<CPropSetElem> state) {

        if (expr instanceof Constant c) {
            Object v = c.getValue();
            if (v instanceof Integer)
                return (Integer) v;
            return null;
        }

        if (expr instanceof Identifier id) {
            for (CPropSetElem e : state.getDataflowElements()) {
                if (e.getId().equals(id))
                    return e.getConstant();
            }
            return null;
        }

        if (expr instanceof BinaryExpression bin) {
            Integer left = eval((ValueExpression) bin.getLeft(), state);
            Integer right = eval((ValueExpression) bin.getRight(), state);

            if (left == null || right == null)
                return null;

            String op = bin.getOperator().toString();

            switch (op) {
                case "+":
                    return left + right;
                case "-":
                    return left - right;
                case "*":
                    return left * right;
                case "/":
                    if (right == 0)
                        return null;
                    return left / right;
                default:
                    return null;
            }
        }

        if (expr instanceof UnaryExpression un) {
            Integer val = eval((ValueExpression) un.getExpression(), state);

            if (val == null)
                return null;

            String op = un.getOperator().toString();

            if (op.equals("-"))
                return -val;
        }

        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> result = new HashSet<>();

        Integer value = eval(expression, state);

        if (value != null)
            result.add(new CPropSetElem(id, value));

        return result;

    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {

        Set<CPropSetElem> result = new HashSet<>();

        for (CPropSetElem e : state.getDataflowElements()) {
            if (e.getId().equals(id))
                result.add(e);
        }

        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
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