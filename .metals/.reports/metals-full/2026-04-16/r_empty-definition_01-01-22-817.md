error id: file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CProp.java:it/unive/lisa/analysis/dataflow/DefiniteSet#
file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CProp.java
empty definition using pc, found symbol in pc: it/unive/lisa/analysis/dataflow/DefiniteSet#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 214
uri: file:///C:/Users/Ibrahim/Desktop/scsr-2026/src/main/java/it/unive/scsr/analysis/CProp.java
text:
```scala
package it.unive.scsr.analysis;// package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.@@DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id,
                                 ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer constant = eval(expression, state);
        if (constant == null)
            return Collections.emptySet();

        return Collections.singleton(new CPropSetElem(id, constant));
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> result = new HashSet<>();

        for (CPropSetElem element : state.getDataflowElements())
            if (id.equals(element.getId()))
                result.add(element);

        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    private Integer eval(ValueExpression expression, DefiniteSet<CPropSetElem> state) {
        if (expression == null)
            return null;

        if (expression instanceof Constant constant)
            return evalConstant(constant);

        if (expression instanceof Identifier identifier)
            return lookup(identifier, state);

        if (expression instanceof UnaryExpression unary)
            return evalUnary(unary, state);

        if (expression instanceof BinaryExpression binary)
            return evalBinary(binary, state);

        return null;
    }

    private Integer evalConstant(Constant constant) {
        if (constant.getValue() instanceof Integer integer)
            return integer;

        return null;
    }

    private Integer lookup(Identifier identifier, DefiniteSet<CPropSetElem> state) {
        Integer found = null;

        for (CPropSetElem element : state.getDataflowElements()) {
            if (!identifier.equals(element.getId()))
                continue;

            Integer current = element.getConstant();
            if (current == null)
                return null;

            if (found == null)
                found = current;
            else if (!found.equals(current))
                return null;
        }

        return found;
    }

    private Integer evalUnary(UnaryExpression expression, DefiniteSet<CPropSetElem> state) {
        Integer argument = eval((ValueExpression) expression.getExpression(), state);
        if (argument == null)
            return null;

        if (expression.getOperator() == NumericNegation.INSTANCE)
            return -argument;

        return null;
    }

    private Integer evalBinary(BinaryExpression expression, DefiniteSet<CPropSetElem> state) {
        Integer left = eval((ValueExpression) expression.getLeft(), state);
        Integer right = eval((ValueExpression) expression.getRight(), state);
        if (left == null || right == null)
            return null;

        if (expression.getOperator() instanceof AdditionOperator)
            return left + right;

        if (expression.getOperator() instanceof SubtractionOperator)
            return left - right;

        if (expression.getOperator() instanceof MultiplicationOperator)
            return left * right;

        if (expression.getOperator() instanceof DivisionOperator) {
            if (right == 0)
                return null;

            return left / right;
        }

        return null;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: it/unive/lisa/analysis/dataflow/DefiniteSet#