package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

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

    private Integer solve(
            ValueExpression expr,
            Set<CPropSetElem> set
    ) {
        if (expr instanceof Constant c) {
            Object value = c.getValue();
            if (value instanceof Integer) {
                return (Integer) value;
            }
        } else if (expr instanceof Identifier i) {
            for (CPropSetElem elem : set) {
                if (elem.id.equals(i)) {
                    return elem.constant;
                }
            }
        } else if (
                expr instanceof UnaryExpression u &&
                u.getExpression() instanceof ValueExpression source &&
                u.getOperator() instanceof NumericNegation
        ) {
            Integer value = solve(source, set);
            return value != null ? -value : null;
        } else if (
                expr instanceof BinaryExpression b &&
                b.getLeft() instanceof ValueExpression l &&
                b.getRight() instanceof ValueExpression r
        ) {
            Integer left = solve(l, set);
            Integer right = solve(r, set);
            BinaryOperator op = b.getOperator();

            if (left == null || right == null) {
                return null;
            } else if (op instanceof AdditionOperator) {
                return left + right;
            } else if (op instanceof SubtractionOperator) {
                return left - right;
            } else if (op instanceof MultiplicationOperator) {
                return left * right;
            } else if (op instanceof DivisionOperator) {
                return left / right;
            }
        }

        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Integer value = solve(valueExpression, cPropSetElems.elements());

        if (value == null) {
            return Set.of();
        } else {
            return Set.of(new CPropSetElem(identifier, value));
        }
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        for (CPropSetElem elem : cPropSetElems.getDataflowElements()) {
            if (elem.id.equals(identifier)) {
                return Set.of(elem);
            }
        }

        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Set.of();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

}