//Group with Sebastiano Boato (897542)
package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

    public CProp() {
        super();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        //if the id already exists the kill methods eliminate the set and here we only have to create a new set
        Set<CPropSetElem> result = new HashSet<>();

        if (valueExpression == null)
            return result;

        //Costant
        if(valueExpression instanceof Constant) {
            if(((Constant) valueExpression).getValue() instanceof Integer) {
                result.add(new CPropSetElem(identifier, (Constant) valueExpression));
            }
        //Identifier
        }else if(valueExpression instanceof Identifier) {
            Set<CPropSetElem> currentSet = cPropSetElems.getDataflowElements();
            for(CPropSetElem r : currentSet){
                if(r.getId().equals(valueExpression)){
                    result.add(new CPropSetElem(identifier, r.getConstant()));
                    break;
                }
            }
        //Unary Expression x=-y
        }else if (valueExpression instanceof UnaryExpression unary){
            if (unary.getOperator() instanceof NumericNegation) {
                ValueExpression innerExpr = (ValueExpression) unary.getExpression();
                Integer innerValue = evaluateToInteger(innerExpr, cPropSetElems); // Reuse the logic again!
                if (innerValue != null) {
                    int resultValue = -innerValue;
                    Constant newConstant = new Constant(unary.getStaticType(), resultValue, programPoint.getLocation());
                    result.add(new CPropSetElem(identifier, newConstant));
                }
            }
        //Binary Expression
        }else if (valueExpression instanceof BinaryExpression binary) {
            ValueExpression leftExpr = (ValueExpression) binary.getLeft();
            ValueExpression rightExpr = (ValueExpression) binary.getRight();
            Integer leftValue = evaluateToInteger(leftExpr, cPropSetElems);
            Integer rightValue = evaluateToInteger(rightExpr, cPropSetElems);
            if (leftValue != null && rightValue != null) {
                int resultValue = 0;
                boolean mathSuccessful = true;
                String opSymbol = binary.getOperator().toString();
                if (opSymbol.equals("+")) {
                    resultValue = leftValue + rightValue;
                } else if (opSymbol.equals("-")) {
                    resultValue = leftValue - rightValue;
                } else if (opSymbol.equals("*")) {
                    resultValue = leftValue * rightValue;
                } else if (opSymbol.equals("/")) {
                    if (rightValue != 0) {
                        resultValue = leftValue / rightValue;
                    } else {
                        mathSuccessful = false;
                    }
                } else {
                    mathSuccessful = false;
                }
                if (mathSuccessful) {
                    Constant newConstant = new Constant(binary.getStaticType(), resultValue, programPoint.getLocation());
                    result.add(new CPropSetElem(identifier, newConstant));
                }
            }
        }

        return result;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> result = new HashSet<>();
        Set<CPropSetElem> currentSet = cPropSetElems.getDataflowElements();
        for(CPropSetElem r : currentSet){
            if(r.getId().equals(identifier)){
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

    // Custom helper method to extract an integer value from either a Constant or a known Identifier
    private Integer evaluateToInteger(ValueExpression expr, DefiniteSet<CPropSetElem> CPropSetElems) {
        // Case 1: It's a direct constant (like the "2" in x = y + 2)
        if (expr instanceof Constant c) {
            if (c.getValue() instanceof Integer) {
                return (Integer) c.getValue();
            }
        }
        // Case 2: It's a variable (like the "y" in x = y + 2)
        else if (expr instanceof Identifier identifier) {
            for (CPropSetElem elem : CPropSetElems.getDataflowElements()) {
                if (elem.getId().equals(identifier)) {
                    Object val = elem.getConstant().getValue();
                    if (val instanceof Integer) {
                        return (Integer) val;
                    }
                }
            }
        }
        // If it's not a constant, and not a known variable, we can't evaluate it to an integer.
        return null;
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