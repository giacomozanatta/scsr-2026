package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.type.NumericType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    private Integer evaluate(ValueExpression expression, DefiniteSet<CPropSetElem> state) {

        if (expression instanceof Constant) {
            Object value = ((Constant) expression).getValue();
            if (value instanceof Integer)
                return (Integer) value;
            return null;
        }

        if (expression instanceof Identifier) {
            Identifier varId = (Identifier) expression;
            for (CPropSetElem elem : state.getDataflowElements())
                if (elem.getId().equals(varId))
                    return elem.getConstant();
            return null;
        }

        if (expression instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) expression;
            Integer left = evaluate((ValueExpression) binary.getLeft(), state);
            Integer right = evaluate((ValueExpression) binary.getRight(), state);

            if (left == null || right == null)
                return null;

            BinaryOperator op = binary.getOperator();

            if (op instanceof Numeric8BitAdd || op instanceof Numeric16BitAdd ||
                op instanceof Numeric32BitAdd || op instanceof Numeric64BitAdd ||
                op instanceof NumericNonOverflowingAdd)
                return left + right;

            if (op instanceof Numeric8BitSub || op instanceof Numeric16BitSub ||
                op instanceof Numeric32BitSub || op instanceof Numeric64BitSub ||
                op instanceof NumericNonOverflowingSub)
                return left - right;

            if (op instanceof Numeric8BitMul || op instanceof Numeric16BitMul ||
                op instanceof Numeric32BitMul || op instanceof Numeric64BitMul ||
                op instanceof NumericNonOverflowingMul)
                return left * right;

            if (op instanceof Numeric8BitDiv || op instanceof Numeric16BitDiv ||
                op instanceof Numeric32BitDiv || op instanceof Numeric64BitDiv ||
                op instanceof NumericNonOverflowingDiv) {
                if (right == 0) return null;
                return left / right;
            }
        }

        if (expression instanceof UnaryExpression) {
            UnaryExpression unary = (UnaryExpression) expression;
            if (unary.getOperator() instanceof NumericNegation) {
                Integer inner = evaluate((ValueExpression) unary.getExpression(), state);
                if (inner != null)
                    return -inner;
            }
        }

        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        // Only track integer-typed variables
        if (!(id.getStaticType() instanceof NumericType) &&
            !(id.getStaticType().isUntyped()))
            return Collections.emptySet();

        Integer value = evaluate(expression, state);
        if (value != null)
            return Collections.singleton(new CPropSetElem(id, value));

        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> toKill = new HashSet<>();
        for (CPropSetElem elem : state.getDataflowElements()) {
            if (elem.getId().equals(id))
                toKill.add(elem);
        }
        return toKill;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }
}
