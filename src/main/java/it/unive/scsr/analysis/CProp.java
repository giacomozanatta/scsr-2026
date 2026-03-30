package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.analysis.dataflow.PossibleSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp  extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    public Integer Evaluate(ValueExpression exp, DefiniteSet<CPropSetElem> elems){

        Integer res = null;

        if(exp == null) return res;

        if(exp instanceof Constant){
            Constant c = (Constant) exp;
            Object val = c.getValue();
            if(val instanceof Integer)
                res = (Integer)val;
        }

        if(exp instanceof Identifier){
            Identifier i = (Identifier) exp;
            for(CPropSetElem cp : elems.getDataflowElements()){
                if(cp.getId().equals(i)){
                    res = cp.getConst();
                    break;
                }
            }
        }

        if(exp instanceof UnaryExpression){
            UnaryExpression u = (UnaryExpression) exp;
            res = Evaluate((ValueExpression)u.getExpression(), elems);
            if(res != null && u.getOperator() instanceof NumericNegation)
                res = -res;
        }

        if(exp instanceof BinaryExpression){
            BinaryExpression b = (BinaryExpression) exp;
            Integer left = Evaluate((ValueExpression) b.getLeft(), elems);
            Integer right = Evaluate((ValueExpression) b.getRight(), elems);
            if(left != null && right != null){
                BinaryOperator operation = b.getOperator();
                if(operation instanceof AdditionOperator) res = left + right;
                if(operation instanceof SubtractionOperator) res = left - right;
                if(operation instanceof MultiplicationOperator) res = left * right;
                if(operation instanceof DivisionOperator) res = right != 0 ? left/right : null;
            }
        }

        return res;

    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {

        Integer val = Evaluate(valueExpression, cPropSetElems);

        return val == null ? Collections.emptySet() : Collections.singleton(new CPropSetElem(identifier, val));
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> res = new HashSet<>();
        for (CPropSetElem cp : cPropSetElems.getDataflowElements())
            if (cp.getId().equals(identifier))
                res.add(cp);
        return res;
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