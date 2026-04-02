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
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Integer computedValue = evaluate(valueExpression, cPropSetElems);

        if (computedValue != null) {
            return Collections.singleton(new CPropSetElem(identifier, computedValue));
        }

        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> toKillSet = new HashSet<>();
        for (CPropSetElem elem : cPropSetElems.getDataflowElements()) {
            if (elem.getId().equals(identifier)) {
                toKillSet.add(elem);
            }
        }
        return toKillSet;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    private static Integer evaluate(ValueExpression expr, DefiniteSet<CPropSetElem> state) {
        // case 1: hardcoded number
        if (expr instanceof Constant) {
            Object val = ((Constant) expr).getValue();
            if (val instanceof Integer) {
                return (Integer) val;
            }
        }

        // case 2: variable
        else if (expr instanceof Identifier) {
            for (CPropSetElem elem : state.getDataflowElements()) {
                if (elem.getId().equals(expr)) {
                    return elem.getConstant();
                }
            }
        }

        // case recursive 1: unary operation (-x)
        else if (expr instanceof UnaryExpression) {
            UnaryExpression unary = (UnaryExpression) expr;
            UnaryOperator op = unary.getOperator();
            Integer innerVal = evaluate((ValueExpression) unary.getExpression(), state);

            if (innerVal != null) {
                if (op instanceof NumericNegation) {
                    return -innerVal;
                }
            }
        }

        // case recursive 2: binary operation (x + y)
        else if (expr instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expr;
            BinaryOperator operator = binary.getOperator();

            Integer leftVal = evaluate((ValueExpression) binary.getLeft(), state);
            Integer rightVal = evaluate((ValueExpression) binary.getRight(), state);

            if (leftVal != null && rightVal != null) {
                if (operator instanceof AdditionOperator) {
                    return leftVal + rightVal;
                }
                else if (operator instanceof SubtractionOperator) {
                    return leftVal - rightVal;
                }
                else if (operator instanceof MultiplicationOperator) {
                    return leftVal * rightVal;
                }
                else if (operator instanceof DivisionOperator) {
                    if (rightVal == 0) return null;
                    return leftVal / rightVal;
                }
            }
        }

        // Other cases return null
        return null;
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