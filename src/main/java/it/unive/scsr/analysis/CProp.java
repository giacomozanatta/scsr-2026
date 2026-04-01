package it.unive.scsr.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    @Override
    public Set<CPropSetElem> gen(
            DefiniteSet<CPropSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {

        Integer value = evalExpression(expression, state);

        if (value == null)
            return Collections.emptySet();

        return Collections.singleton(new CPropSetElem(id, value));
    }

    @Override
    public Set<CPropSetElem> gen(
            DefiniteSet<CPropSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(
            DefiniteSet<CPropSetElem> state,
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {

        Set<CPropSetElem> result = new HashSet<>();

        for (CPropSetElem cp : state.getDataflowElements())
            if (cp.getId().equals(id))
                result.add(cp);

        return result;
    }

    @Override
    public Set<CPropSetElem> kill(
            DefiniteSet<CPropSetElem> state,
            ValueExpression expression,
            ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    private static Integer getValueOf(Identifier id, DefiniteSet<CPropSetElem> domain) {
        for (CPropSetElem cp : domain.getDataflowElements())
            if (id.equals(cp.getId()))
                return cp.getConstant();

        return null;
    }

    private static Integer evalExpression(SymbolicExpression expr, DefiniteSet<CPropSetElem> domain) {
        if (expr == null)
            return null;

        if (expr instanceof Constant constExpr) {
            Object value = constExpr.getValue();
            return value instanceof Integer res ? res : null;
        }

        if (expr instanceof Identifier exprId)
            return getValueOf(exprId, domain);

        if (expr instanceof UnaryExpression unaryExpr) {
            Integer value = evalExpression(unaryExpr.getExpression(), domain);

            if (value == null)
                return null;

            if (unaryExpr.getOperator() instanceof NumericNegation)
                return -value;

            return null;
        }

        if (expr instanceof BinaryExpression binaryExpr) {
            Integer leftValue = evalExpression(binaryExpr.getLeft(), domain);
            Integer rightValue = evalExpression(binaryExpr.getRight(), domain);

            if (leftValue == null || rightValue == null)
                return null;

            BinaryOperator operator = binaryExpr.getOperator();

            if (operator instanceof AdditionOperator)
                return leftValue + rightValue;

            if (operator instanceof SubtractionOperator)
                return leftValue - rightValue;

            if (operator instanceof MultiplicationOperator)
                return leftValue * rightValue;

            if (operator instanceof DivisionOperator)
                return rightValue == 0 ? null : leftValue / rightValue;
        }

        return null;
    }
}