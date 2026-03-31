package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.HashSet;
import java.util.Set;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {
    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Integer value = CPropSetElem.eval(valueExpression, cPropSetElems.getDataflowElements());
        if (value != null) {
            CPropSetElem elem = new CPropSetElem(identifier, value);
            return Set.of(elem);
        }
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, Identifier identifier, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        Set<CPropSetElem> killSet = new HashSet<>();
        for (CPropSetElem elem : cPropSetElems.getDataflowElements()) {
            if(elem.getId() != null && elem.getId().equals(identifier)) {
                killSet.add(elem);
            }
        }
        return killSet;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> cPropSetElems, ValueExpression valueExpression, ProgramPoint programPoint) throws SemanticException {
        return Set.of();
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
