package it.unive.scsr.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem>{

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
    
    private Integer eval_idtf_value(Identifier id, DefiniteSet<CPropSetElem> l)
    {
        for(CPropSetElem el : l.getDataflowElements())
        {
            if(el.getId().equals(id))
            {
                return el.getConstant();
            }
        }
        return null;
    }
    
    private Integer eval_expr_value(SymbolicExpression ve, DefiniteSet<CPropSetElem> l)
    {
        //Empty expression
        if(ve == null)
            return null;
        
        //Expression is constant
        if(ve instanceof Constant constant)
        {
            Object value = constant.getValue();
            if(value instanceof Integer integer)
                return integer;            
        }
        
        //Expression is identifier
        if(ve instanceof Identifier identifier)
        {
            return eval_idtf_value(identifier, l);
        }
        
        //Expression is unary
        if(ve instanceof UnaryExpression un_expr)
        {
            //Extract operator and argument
            UnaryOperator un_op = un_expr.getOperator();
            ValueExpression arg = (ValueExpression) un_expr.getExpression();
            //Evaluate argument's value
            Integer value = eval_expr_value(arg, l);
            //If value is null, return null
            if(value == null)
                return null;
            //Expression is negation operator
            if(un_op instanceof NumericNegation)
            {
                return -value;
            }
        }
        
        //Expression is binary
        if(ve instanceof BinaryExpression bi_expr)
        {
            //Extract operator
            BinaryOperator bi_op = bi_expr.getOperator();
            //Evaluate left and right operands 
            Integer left = eval_expr_value(bi_expr.getLeft(), l);
            Integer right = eval_expr_value(bi_expr.getRight(), l);
            //If either is null, return null
            if(left == null || right == null)
                return null;
            //Evaluate various operators
            if(bi_op instanceof AdditionOperator)
                return left + right;
            
            if(bi_op instanceof SubtractionOperator)
                return left - right;
            
            if(bi_op instanceof MultiplicationOperator)
                return left * right;
            
            //Here we evaluate division and modulo operators
            //If right operand is 0 for either, return null
            if(0 == right)
                    return null;
            if(bi_op instanceof DivisionOperator)
            {
                return (int) left / right;
            }
            if(bi_op instanceof ModuloOperator)
            {
                return left % right;
            }
        }
        return null;
    }
    
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> l, Identifier idntfr, ValueExpression ve, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> generated = new HashSet<>();
        
        Integer value = eval_expr_value(ve, l);
        
        if(value != null)
        {
            generated.add(new CPropSetElem(idntfr, value));
        }
        
        return generated;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> l, ValueExpression ve, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> l, Identifier idntfr, ValueExpression ve, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> dead = new HashSet<>();
        for(CPropSetElem el : l.getDataflowElements())
        {
            if(el.getId().equals(idntfr))
                dead.add(el);
        }
        return dead;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> l, ValueExpression ve, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }


}