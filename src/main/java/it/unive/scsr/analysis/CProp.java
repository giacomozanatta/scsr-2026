package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.HashSet;
import java.util.Set;

public class CProp
    extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() { return new DefiniteSet<>(); }

    private Integer evaluate (ValueExpression expression, DefiniteSet<CPropSetElem> state) throws SemanticException {
        // Constant
        if (expression instanceof Constant) {
            Object value = ((Constant) expression).getValue();
            if (value instanceof Integer i) {
                return i;
            }
        }

        // Expression
        if (expression instanceof BinaryExpression) {
            SymbolicExpression l = ((BinaryExpression) expression).getLeft();
            SymbolicExpression r = ((BinaryExpression) expression).getRight();
            Object value_l = new Object();
            Object value_r = new Object();
            if (l instanceof ValueExpression) {
                value_l = evaluate(((ValueExpression) l), state);
            }
            if (r instanceof ValueExpression) {
                value_r = evaluate(((ValueExpression) r), state);
            }

            BinaryOperator op = ((BinaryExpression) expression).getOperator();
            if (op instanceof AdditionOperator) {
                if (value_l instanceof Integer && value_r instanceof Integer) {
                    return (Integer) value_l + (Integer) value_r;
                }
                throw new SemanticException();
            } else if (op instanceof MultiplicationOperator) {
                if ((value_l instanceof Integer) && (value_r instanceof Integer)) {
                    return (Integer) value_l * (Integer) value_r;
                }
                throw new SemanticException();
            } else if (op instanceof DivisionOperator) {
                if (value_l instanceof Integer && value_r instanceof Integer) {
                    if ((Integer) value_r == 0) {
                        throw new ArithmeticException();
                    }
                    return (Integer) value_l / (Integer) value_r;
                }
                throw new SemanticException();
            } else if (op instanceof ModuloOperator) {
                if (value_l instanceof Integer && value_r instanceof Integer) {
                    if ((Integer) value_r == 0) {
                        throw new ArithmeticException();
                    }
                    return (Integer) value_l % (Integer) value_r;
                }
                throw new SemanticException();
            } else if (op instanceof SubtractionOperator) {
                if (value_l instanceof Integer && value_r instanceof Integer) {
                    return (Integer) value_l - (Integer) value_r;
                }
                throw new SemanticException();
            }
        }

        // Identifier
        if (expression instanceof Identifier id) {
            if (state.knowsIdentifier(id)) {
                for (CPropSetElem c : state.getDataflowElements()) {
                    if (c.getId().equals(id)) {
                        return c.getValue();
                    }
                }
            }
        }

        // Unary
        if (expression instanceof UnaryExpression ue) {
            if (ue.getOperator() instanceof NumericNegation){
                if (ue.getExpression() instanceof ValueExpression ve){
                    return -evaluate(ve, state);
                }
                throw new SemanticException();
            }
            throw new SemanticException();
        }

        // Null case
        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> output = new HashSet<>();
        Integer evaluation = evaluate(expression, state);
        if (evaluation != null) {
            CPropSetElem e = new CPropSetElem(id, evaluation);
            output.add(e);
        }
        return output;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) {
        if (state.knowsIdentifier(id)) {
            for (CPropSetElem c : state.getDataflowElements()) {
                if (c.getId().equals(id)) {
                    return Set.of(c);
                }
            }
        }
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) {
        return Set.of();
    }

    // IMPLEMENTATION NOTE:

    // - Implement your solution using the DefiniteSet.
    //   - What would happen if you used a PossibleSet instead? Think about it (or try it), but remember to deliver the Definite version.
    // - Keep it simple: track only integer values. Any noninteger values should be ignored.
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