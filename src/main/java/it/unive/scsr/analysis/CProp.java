package it.unive.scsr.analysis;

import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    private static class ParserException extends Exception {
        public ParserException() {
        }

        public ParserException(String message) {
            super(message);
        }
    }

    private static final Logger LOG = LogManager.getLogger(CProp.class);

    private static int
    parserInt(DefiniteSet<CPropSetElem> state, ValueExpression expression)
            throws NumberFormatException, ParserException {
        LOG.debug("Processing expression: {} <{}>", expression, expression.getClass().getSimpleName());
        if (expression instanceof Constant x) {
            if (x.getValue() instanceof Integer) {
                return Integer.parseInt(x.getValue().toString());
            } else {
                throw new NumberFormatException();
            }
        } else if (expression instanceof Identifier x /*Variable or GlobalVariable*/) {
            if (state.knowsIdentifier(x)) {
                return state
                        .getDataflowElements()
                        .stream()
                        .filter(cPropSetElem -> cPropSetElem.getId().equals(x))
                        .findFirst()
                        .orElseThrow(() -> new ParserException("ERROR: Failed to retrieve constant"))
                        .getConstant();
            } else {
                throw new ParserException("INFO: Expression is not constant");
            }
        } else if (expression instanceof UnaryExpression x) {
            if (x.getOperator() instanceof NumericNegation) {
                return -(parserInt(state, (ValueExpression) x.getExpression()));
            }
        } else if (expression instanceof BinaryExpression x) {
            ValueExpression l = (ValueExpression) x.getLeft();
            ValueExpression r = (ValueExpression) x.getRight();

            LOG.info("INFO: Expression is type < {} >", x.getStaticType());
            if (x.getOperator() instanceof AdditionOperator) {
                return (parserInt(state, l)) + (parserInt(state, r));
            }
            if (x.getOperator() instanceof SubtractionOperator) {
                return (parserInt(state, l)) - (parserInt(state, r));
            }
            if (x.getOperator() instanceof MultiplicationOperator) {
                return (parserInt(state, l)) * (parserInt(state, r));
            }
            if (x.getOperator() instanceof DivisionOperator) {
                double res = ((double) parserInt(state, l)) / (parserInt(state, r));
                if (res == (double) (int) res) {
                    return (int) res;
                } else {
                    throw new NumberFormatException();
                }
            }
            if (x.getOperator() instanceof ModuloOperator) {
                return (parserInt(state, l)) % (parserInt(state, r));
            }
        }
        throw new ParserException("ERROR: Expression not supported");
    }

    @Override
    public Set<CPropSetElem> gen(
            DefiniteSet<CPropSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) {
        Set<CPropSetElem> result = new HashSet<>();

        if (!(id instanceof Variable || id instanceof GlobalVariable)) {
            return result;
        }

        Integer val = null;
        try {
            val = parserInt(state, expression);
        } catch (NumberFormatException e) {
            LOG.error("ERROR: Expression not an integer\n>>> {}", expression.toString());
        } catch (ParserException u) {
            LOG.warn("{}\n>>> {}", u.getMessage(), expression.toString());
        }

        if (val != null) {
            result.add(new CPropSetElem(id, val));
        }

        return result;
    }

    @Override
    public Set<CPropSetElem> gen(
            DefiniteSet<CPropSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(
            DefiniteSet<CPropSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) {
        return state
                .getDataflowElements()
                .stream()
                .filter(e -> e.getId().equals(id))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Set<CPropSetElem> kill(
            DefiniteSet<CPropSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) {
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