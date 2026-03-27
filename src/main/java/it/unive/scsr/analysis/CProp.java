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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> /* extends ... */ {
    public static Integer parser(ValueExpression expression, DefiniteSet<CPropSetElem> elems) {
        if (expression == null)
            return null;

        if (expression instanceof Constant ex) {
            Object tmp = ex.getValue();
            return tmp instanceof Integer ? (Integer) tmp : null;
        }

        if (expression instanceof Identifier ex) {
            for (CPropSetElem elem : elems.getDataflowElements())
                if (elem.id().equals(ex))
                    return elem.constant();
            return null;
        }

        if (expression instanceof UnaryExpression un) {
            Operator op = un.getOperator();
            ValueExpression ex = (ValueExpression) un.getExpression();

            Integer tmpval = parser(ex, elems);

            return tmpval == null ? null : tmpval * (op instanceof NumericNegation ? -1 : 1);
        }

        if (expression instanceof BinaryExpression ex) {
            Integer left = parser((ValueExpression) ex.getLeft(), elems);
            Integer right = parser((ValueExpression) ex.getRight(), elems);
            Operator op = ex.getOperator();

            if (left == null || right == null)
                return null;
            else if (op instanceof AdditionOperator) {
                return left + right;
            } else if (op instanceof SubtractionOperator) {
                return left - right;
            } else if (op instanceof MultiplicationOperator) {
                return left * right;
            } else if (op instanceof DivisionOperator) {
                return left / right;
            }

            return null;
        }

        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Integer tmp = parser(valueExpression, cPropSetElems);
        return tmp != null ? Collections.singleton(new CPropSetElem(identifier, tmp)) : Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> killSet = new HashSet<>();

        for (CPropSetElem elem : cPropSetElems)
            if (elem.id().equals(identifier))
                killSet.add(elem);

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
    // IMPLEMENTATION NOTE:

    // - Implement your solution using the DefiniteDataFlowDomain.
    //   - What would happen if you used a PossibleDataFlowDomain instead? Think about it (or try it), but remember to deliver the Definite version.
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