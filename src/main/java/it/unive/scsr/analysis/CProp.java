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

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer value = evaluate(expression, state);

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