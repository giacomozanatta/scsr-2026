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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CPropSolution extends
        DataflowDomain<DefiniteSet<CPropSetElemSolution>, CPropSetElemSolution> {


    private static Integer getValueOf(
            Identifier id,
            DefiniteSet<CPropSetElemSolution> state) {
        for (CPropSetElemSolution cp : state.getDataflowElements())
            if (cp.getId().equals(id))
                return cp.getConstant();
        return null;
    }

    private static Integer eval(
            ValueExpression expression,
            DefiniteSet<CPropSetElemSolution> state) {
        if (expression == null)
            return null;

        if (expression instanceof Constant) {
            Object value = ((Constant) expression).getValue();
            if (value instanceof Integer)
                return (Integer) value;
        }

        if (expression instanceof Identifier)
            return getValueOf((Identifier) expression, state);

        if (expression instanceof UnaryExpression) {
            UnaryExpression unary = (UnaryExpression) expression;
            UnaryOperator operator = unary.getOperator();
            ValueExpression arg = (ValueExpression) unary.getExpression();

            Integer value = eval(arg, state);
            if (value == null)
                return null;
            if (operator instanceof NumericNegation)
                return -value;
        }

        if (expression instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expression;
            BinaryOperator operator = binary.getOperator();
            ValueExpression left = (ValueExpression) binary.getLeft();
            ValueExpression right = (ValueExpression) binary.getRight();

            Integer lvalue = eval(left, state);
            Integer rvalue = eval(right, state);
            if (lvalue == null || rvalue == null)
                return null;
            if (operator instanceof AdditionOperator)
                return lvalue + rvalue;
            if (operator instanceof SubtractionOperator)
                return lvalue - rvalue;
            if (operator instanceof MultiplicationOperator)
                return lvalue * rvalue;
            if (operator instanceof DivisionOperator)
                return lvalue / rvalue;
        }

        return null;
    }
    @Override
    public Set<CPropSetElemSolution> gen(DefiniteSet<CPropSetElemSolution> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer value = eval(expression, state);
        if (value != null)
            return Collections.singleton(new CPropSetElemSolution(id, value));
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElemSolution> gen(DefiniteSet<CPropSetElemSolution> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElemSolution> kill(DefiniteSet<CPropSetElemSolution> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElemSolution> result = new HashSet<>();

        for (CPropSetElemSolution cp : state.getDataflowElements())
            if (cp.getId().equals(id))
                result.add(cp);

        return result;
    }

    @Override
    public Set<CPropSetElemSolution> kill(DefiniteSet<CPropSetElemSolution> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElemSolution> makeLattice() {
        return new DefiniteSet<>();
    }
}
