package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//Work done by Elia Stevanato 895598 & Francesco Pasqualato 897778
public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    private Integer evaluate(ValueExpression expr, DefiniteSet<CPropSetElem> state) {
        if (expr instanceof Constant) {
            Object val = ((Constant) expr).getValue();
            if (val instanceof Integer) return (Integer) val;
        }

        if (expr instanceof Identifier) {
            for (CPropSetElem elem : state) {
                if (elem.getId().equals(expr)) return elem.getConstant();
            }
        }

        if (expr instanceof BinaryExpression binary) {
            Integer left = evaluate((ValueExpression) binary.getLeft(), state);
            Integer right = evaluate((ValueExpression) binary.getRight(), state);

            if (left != null && right != null) {
                if (binary.getOperator() != null) {
                    String str = binary.getOperator().toString();
                    switch (str) {
                        case "+" -> {
                            return left + right;
                        }
                        case "-" -> {
                            return left - right;
                        }
                        case "*" -> {
                            return left * right;
                        }
                        case "/" -> {
                            if (right != 0) return left / right;
                        }
                    }
                }
            }
        }

        if (expr instanceof UnaryExpression unary) {
            Integer exp = evaluate((ValueExpression) unary.getExpression(), state);
            if (exp != null) {
                UnaryOperator op = unary.getOperator();
                if (op instanceof NumericNegation) {
                    return -exp;
                }
            }
        }

        return null;

    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer value = evaluate(expression, state);
        if (value != null) {
            return Collections.singleton(new CPropSetElem(id, value));
        }
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> toKill = new HashSet<>();
        for (CPropSetElem elem : state) {
            if (elem.getId().equals(id)) {
                toKill.add(elem);
            }
        }
        return toKill;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }
   
}