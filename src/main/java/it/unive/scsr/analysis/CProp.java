package it.unive.scsr.analysis;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;

import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp extends
        DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {


    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) 
    {
        Set<CPropSetElem> result = new HashSet<>();
        Integer left = null;
        Integer right = null;
        
        if (expression instanceof Constant) {   // x = 5
            Constant c = (Constant) expression;

            if( c.getValue() instanceof Integer)
            {
                CPropSetElem ae = new CPropSetElem(id, (Integer) c.getValue());  //  [X, 5]
                result.add(ae);
            }
        }

        else if(expression instanceof Identifier)  // x = y
        {
            for(CPropSetElem elem : state.getDataflowElements())
            {                    
                if (elem.getId().equals(expression))
                {
                    CPropSetElem ae = new CPropSetElem(id, (Integer) elem.getConstant());
                    result.add(ae);
                }
            }
        }

        else if(expression instanceof BinaryExpression) // x = y + 2
        {
            BinaryExpression byexpr = (BinaryExpression) expression;

            if(byexpr.getLeft() instanceof Constant)
            {
                Constant c1 = (Constant) byexpr.getLeft();
                Object a = c1.getValue();

                if(a instanceof Integer)
                    left = (Integer) a; 
            }

            else if (byexpr.getLeft() instanceof Identifier)
            {
                for(CPropSetElem elem : state.getDataflowElements())
                    if(elem.getId().equals(byexpr.getLeft()))
                        left = elem.getConstant();
            }


            if(byexpr.getRight() instanceof Constant)
            {
                Constant c1 = (Constant) byexpr.getRight();
                Object a = c1.getValue();

                if(a instanceof Integer)
                    right = (Integer) a; 
            }

            else if (byexpr.getRight() instanceof Identifier)
            {
                for(CPropSetElem elem : state.getDataflowElements())
                    if(elem.getId().equals(byexpr.getRight()))
                        right = elem.getConstant();
            }

            
            if(left != null && right != null)
            {
                if(byexpr.getOperator().toString().contains("+"))
                    result.add(new CPropSetElem(id, left + right));

                if(byexpr.getOperator().toString().contains("-"))
                    result.add(new CPropSetElem(id, left - right));

                if(byexpr.getOperator().toString().contains("*"))
                    result.add(new CPropSetElem(id, left*right));

                if(byexpr.getOperator().toString().contains("/") || byexpr.getOperator().toString().contains(":"))
                    if(right != 0)
                        result.add(new CPropSetElem(id, left/right));
            }
            
        }

        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) 
    {
        Set<CPropSetElem> result = new HashSet<>();

        for (CPropSetElem elem : state.getDataflowElements())
        {
            if (elem.getId().equals(id))
                result.add(elem);
        }
            
        return result;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) {
        return Collections.emptySet();
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