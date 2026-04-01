package it.unive.scsr.analysis;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.binary.NumericNonOverflowingAdd;
import it.unive.lisa.symbolic.value.operator.binary.NumericNonOverflowingDiv;
import it.unive.lisa.symbolic.value.operator.binary.NumericNonOverflowingMul;
import it.unive.lisa.symbolic.value.operator.binary.NumericNonOverflowingSub;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CProp  extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

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

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state,
                                 Identifier id,
                                 ValueExpression expression,
                                 ProgramPoint pp) throws SemanticException {
        // assignment: id = expression
        Constant c = evalToConstant(state, expression);
        if (c == null)
            return Collections.emptySet(); // if non-constant -> nothing to generate
        return Collections.singleton(new CPropSetElem(id, c));
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state,
                                  Identifier id,
                                  ValueExpression expression,
                                  ProgramPoint pp) throws SemanticException {
        // kill all information on this variable
        Set<CPropSetElem> result = new HashSet<>();
        for (CPropSetElem e : state.getDataflowElements()) {
            if (e.getId().equals(id))
                result.add(e);
        }
        return result;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state,
                                 ValueExpression expression,
                                 ProgramPoint pp) throws SemanticException {
        // no assignment -> no new information
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state,
                                  ValueExpression expression,
                                  ProgramPoint pp) throws SemanticException {
        // no assignment -> nothing to kill
        return Collections.emptySet();
    }

    /**
     * Try to evaluate a constant integer expression
     * Return null if not possible
     */
    private Constant evalToConstant(DefiniteSet<CPropSetElem> state,
                                    SymbolicExpression expr) {

        // Direct constant
        if (expr instanceof Constant c) {
            if (c.getValue() instanceof Integer)
                return c;
            return null;
        }

        // Identifier
        if (expr instanceof Identifier id) {
            for (CPropSetElem e : state.getDataflowElements())
                if (e.getId().equals(id))
                    return e.getConstant();
            return null;
        }

        // Unary : -x
        if (expr instanceof UnaryExpression u) {
            Constant inner = evalToConstant(state, u.getExpression());
            if (inner == null || !(inner.getValue() instanceof Integer))
                return null;

            int v = (Integer) inner.getValue();

            if (u.getOperator() instanceof NumericNegation)
                return new Constant(inner.getStaticType(), -v, expr.getCodeLocation());

            return null;
        }

        // Binary : x+y, x-y, x*y, x/y
        if (expr instanceof BinaryExpression b) {
            Constant lc = evalToConstant(state, b.getLeft());
            Constant rc = evalToConstant(state, b.getRight());
            if (lc == null || rc == null)
                return null;

            if (!(lc.getValue() instanceof Integer) || !(rc.getValue() instanceof Integer))
                return null;

            int l = (Integer) lc.getValue();
            int r = (Integer) rc.getValue();
            Integer res = null;

            if (b.getOperator() instanceof NumericNonOverflowingAdd)
                res = l + r;
            else if (b.getOperator() instanceof NumericNonOverflowingSub)
                res = l - r;
            else if (b.getOperator() instanceof NumericNonOverflowingMul)
                res = l * r;
            else if (b.getOperator() instanceof NumericNonOverflowingDiv) {
                if (r == 0)
                    return null;
                res = l / r;
            }

            if (res == null)
                return null;

            return new Constant(lc.getStaticType(), res, expr.getCodeLocation());
        }
        return null;
    }
}