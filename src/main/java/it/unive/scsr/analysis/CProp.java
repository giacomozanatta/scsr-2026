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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

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

    private Integer evaluate(ValueExpression expression, DefiniteSet<CPropSetElem> state) {

        // basic case x = n integer
        if (expression instanceof Constant constant) {
            Object val = constant.getValue();

            if(val instanceof Integer) {
                return (Integer) val;
            }

            return null; //return nothing if not an integer
        }

        // variable reference case (like y = x, look up if x is already equal to a constant)
        if(expression instanceof Identifier) {
            // look up if this variable is already known to be a constant in the state set
            for (CPropSetElem elem : state) {
                if(elem.getId().equals(expression)){
                    return elem.getConstant();
                }
            }
            return null; // variable is not known to be a constant
        }

        // x = -y style expressions
        // we'll only handle the case of unary minus here
        if (expression instanceof UnaryExpression unaryExpr) {
            Operator op = unaryExpr.getOperator();
            ValueExpression expr = (ValueExpression) unaryExpr.getExpression();

            Integer operand = evaluate(expr, state);

            if(operand != null && op instanceof NumericNegation) {
                return -operand;
            }
            return null;
        }

        // expr OP expr case
        if(expression instanceof BinaryExpression binExpr){
            Integer left = evaluate((ValueExpression) binExpr.getLeft(), state);
            Integer right = evaluate((ValueExpression) binExpr.getRight(), state);

            if (left == null || right == null){
                return null;
            }

            BinaryOperator binOp = binExpr.getOperator();

            if (binOp instanceof AdditionOperator){
                return left + right;
            }

            if (binOp instanceof SubtractionOperator){
                return left - right;
            }

            if (binOp instanceof MultiplicationOperator){
                return left * right;
            }

            if (binOp instanceof DivisionOperator){
                return (right == 0) ? null : left/right;
            }
        }

        // any other expression is not a constant
        return null;
    }

    // gen for assignments x := expr => if valueExpression evaluates to a constant in the current state, generates (x,c)
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state,
                                 Identifier id,
                                 ValueExpression valueExpression,
                                 ProgramPoint programPoint)
            throws SemanticException {

        Integer c = evaluate(valueExpression, state);

        if(c != null)
            return Set.of(new CPropSetElem(id, c));

        return Collections.emptySet();
    }

    // gen for non-assignment statements (if, while, etc) => nothing is generated
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    // kill for assignments x := expr => x is overwritten so kill any existing (x,c) fact
    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state,
                                  Identifier id,
                                  ValueExpression valueExpression,
                                  ProgramPoint programPoint)
            throws SemanticException {

        Set<CPropSetElem> killedSet = new HashSet<>();

        for(CPropSetElem elem : state){
            if(elem.getId().equals(id))
                killedSet.add(elem);
        }

        return killedSet;
    }

    // kill for non-assignment statements => nothing
    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

}