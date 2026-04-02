package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Alan Dal Col 895879
 * @author Mattia Acquilesi 896827
 */
public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier id, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {

        Set<CPropSetElem> result = new HashSet<>();

        Integer value = getEvaluation(valueExpression, cPropSetElems);

        if (value != null)
            result.add(new CPropSetElem(id, value));

        return result;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier id, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> currentSet = cPropSetElems.getDataflowElements();
        Set<CPropSetElem> result = new HashSet<>();
        for (CPropSetElem r : currentSet)
        {
            if (r.getId().equals(id))
            {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Set.of();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    private Integer getEvaluation(ValueExpression valueExpression, DefiniteSet<CPropSetElem> cPropSetElems) {

        if (valueExpression instanceof Constant c)
        {
            Object v = c.getValue();
            if (v instanceof Integer)
                return (Integer) v;
            return null;
        }

        if (valueExpression instanceof Identifier id)
        {
            for (CPropSetElem e : cPropSetElems.getDataflowElements())
            {
                if (e.getId().equals(id))
                    return e.getConstant();
            }
            return null;
        }

        if (valueExpression instanceof BinaryExpression bin)
        {
            Integer left = getEvaluation((ValueExpression) bin.getLeft(), cPropSetElems);
            Integer right = getEvaluation((ValueExpression) bin.getRight(), cPropSetElems);

            if (left == null || right == null)
                return null;

            String operation = bin.getOperator().toString();
            return switch (operation)
            {
                case "+" -> left + right;
                case "-" -> left - right;
                case "*" -> left * right;
                case "/" ->
                {
                    if (right == 0)
                        yield null;
                    yield left / right;
                }
                default -> null;
            };
        }

        if (valueExpression instanceof UnaryExpression un)
        {
            Integer value = getEvaluation((ValueExpression) un.getExpression(), cPropSetElems);

            if (value == null)
                return null;

            String operation = un.getOperator().toString();
            if (operation.equals("-"))
                return -value;
        }

        return null;
    }
}