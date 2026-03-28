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

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier id, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {

        Set<CPropSetElem> result = new HashSet<>();

        Integer value = getEvaluation(valueExpression, cPropSetElems);

        if (value != null)
            result.add(new CPropSetElem(id, value));

        return result;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier id, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> currentSet = cPropSetElems.getDataflowElements();
        Set<CPropSetElem> result = new HashSet<>();
        for (CPropSetElem r : currentSet)
        {
            if (r.getId().equals(id))
            {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
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

    private Integer getEvaluation(ValueExpression valueExpression, DefiniteSet<CPropSetElem> cPropSetElems) {

        // costante
        if (valueExpression instanceof Constant c)
        {
            Object v = c.getValue();
            if (v instanceof Integer)
                return (Integer) v;
            return null;
        }

        // variabile
        if (valueExpression instanceof Identifier id)
        {
            for (CPropSetElem e : cPropSetElems.getDataflowElements())
            {
                if (e.getId().equals(id))
                    return e.getConstant();
            }
            return null;
        }

        // binaria
        if (valueExpression instanceof BinaryExpression bin)
        {
            Integer left = getEvaluation((ValueExpression) bin.getLeft(), cPropSetElems);
            Integer right = getEvaluation((ValueExpression) bin.getRight(), cPropSetElems);

            if (left == null || right == null)
                return null;

            String operation = bin.getOperator().toString();
            return switch (operation)
            {
                case "+" -> left + right;
                case "-" -> left - right;
                case "*" -> left * right;
                case "/" ->
                {
                    if (right == 0)
                        yield null;
                    yield left / right;
                }
                default -> null;
            };
        }

        // unaria (-x)
        if (valueExpression instanceof UnaryExpression un)
        {
            Integer value = getEvaluation((ValueExpression) un.getExpression(), cPropSetElems);

            if (value == null)
                return null;

            String operation = un.getOperator().toString();
            if (operation.equals("-"))
                return -value;
        }

        return null;
    }
}