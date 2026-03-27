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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    private Integer eval_expression(ValueExpression valueExpression, DefiniteSet<CPropSetElem> cPropSetElems) {
            if(valueExpression == null) return null;

            if(valueExpression instanceof Constant const_exp){
                Object val = const_exp.getValue();
                if(val instanceof Integer){
                    return (Integer) val;
                }
                //Non-integer vals get ignored, i.e we return null (at the end of the func)
            }

            if(valueExpression instanceof Identifier identifier){
                for(CPropSetElem elem : cPropSetElems.getDataflowElements()){
                    if(elem.getInvolvedIdentifiers().contains(identifier)){
                        return elem.getConstant();
                    }
                }
                return null;
            }

            //https://javadoc.io/doc/io.github.lisa-analyzer/lisa-sdk/latest/it/unive/lisa/symbolic/value/operator/unary/package-summary.html
            if(valueExpression instanceof UnaryExpression unary_expression){
                ValueExpression unary_val = (ValueExpression) unary_expression.getExpression();

                Integer val = eval_expression(unary_val, cPropSetElems);
                if(val == null)
                    return null;
                if(unary_expression.getOperator() instanceof NumericNegation)
                    return -val;
            }

            //https://javadoc.io/doc/io.github.lisa-analyzer/lisa-sdk/latest/it/unive/lisa/symbolic/value/operator/package-summary.html
            // <lexp> <op> <rexp>
            if(valueExpression instanceof BinaryExpression binary_expression){
                Integer lval = eval_expression((ValueExpression) binary_expression.getLeft(), cPropSetElems);
                Integer rval = eval_expression((ValueExpression) binary_expression.getRight(), cPropSetElems);
                BinaryOperator op = binary_expression.getOperator();

                if(lval == null || rval == null)
                    return null;
                // Maybe this could be a for loop over a mapping between Operator interfaces and a function defining the operation
                // E.g to support bitwise shift and modulo op without copy-pasting another line here => better scalability, perhaps.
                if(op instanceof AdditionOperator) return lval + rval;
                if(op instanceof SubtractionOperator) return lval - rval;
                if(op instanceof MultiplicationOperator) return lval * rval;
                if(op instanceof DivisionOperator) return rval != 0 ? lval/rval : null;
            }

            return null;
    }
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Integer val = eval_expression(valueExpression, cPropSetElems);

        if(val == null) return Collections.emptySet();
        return Collections.singleton(new CPropSetElem(identifier, val));
    }


    // This gen is for expressions that are not assigned to an identifier (e.g if(i == 0), if(x >= 2 && y <= 1) )
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        HashSet<CPropSetElem> killed = new HashSet<>();
        for(CPropSetElem elem : cPropSetElems.getDataflowElements()){
            if(elem.getInvolvedIdentifiers().contains(identifier)){
                killed.add(elem);
            }
        }
        return killed;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }


    // IMPLEMENTATION NOTE:

    // - Implement your solution using the DefiniteDataFlowDomain.
    //   - What would happen if you used a PossibleDataFlowDomain instead? Think about it (or try it), but remember to deliver the Definite version.
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