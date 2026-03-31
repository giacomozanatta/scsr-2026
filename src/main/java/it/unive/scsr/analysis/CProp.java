
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

import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {


    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    private Integer evaluate(ValueExpression expression, DefiniteSet<CPropSetElem> state) {

        // case: integer literal → e.g. x = 5
        if (expression instanceof Constant constant) {
            Object value = constant.getValue();
            if (value instanceof Integer i) {
                return i;
            }
            return null;
        }

        // case: variable → look up its value in the current state, e.g. y = x (if x is known)
        if (expression instanceof Identifier identifier) {
            for (CPropSetElem elem : state.getDataflowElements()) {
                if (elem.getId().equals(identifier)) {
                    return elem.getConstant();
                }
            }
            return null;
        }

        if (expression instanceof UnaryExpression unaryExpression) {
            if (unaryExpression.getOperator() instanceof NumericNegation) {
                Integer inner = evaluate((ValueExpression) unaryExpression.getExpression(), state);
                if (inner != null) {
                    return -inner;
                }
            }
            return null;
        }

        if (expression instanceof BinaryExpression binaryExpression) {
            Integer left = evaluate((ValueExpression) binaryExpression.getLeft(), state);
            Integer right = evaluate((ValueExpression) binaryExpression.getRight(), state);

            // if either side is not a constant, the result is not a constant
            if (left == null ||  right == null) {
                return null;
            }

            if (binaryExpression.getOperator() instanceof AdditionOperator) {
                return left + right;
            }

            if (binaryExpression.getOperator() instanceof SubtractionOperator) {
                return left - right;
            }

            if (binaryExpression.getOperator() instanceof MultiplicationOperator) {
                return left * right;
            }

            if (binaryExpression.getOperator() instanceof DivisionOperator) {
                if (right == 0) {
                    return null;
                }
                return left / right;
            }
        }

        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> generated = new HashSet<>();

        Integer value = evaluate(expression, state);

        if (value != null) {
            generated.add(new CPropSetElem(id, value));
        }

        return generated;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> killed = new HashSet<>();
        for (CPropSetElem elem : state.getDataflowElements()) {
            if (elem.getId().equals(id)) {
                killed.add(elem);
            }
        }
        return killed;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }


}


