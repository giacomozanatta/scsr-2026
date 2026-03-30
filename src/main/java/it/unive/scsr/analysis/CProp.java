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

    private Integer parseConstant(Constant expr) {
        Integer parsed = (Integer) expr.getValue();
        if (parsed != null) {
            return (Integer) parsed;
        }
        return null;
    }

    private Integer parseIdentifier(Identifier expr, DefiniteSet<CPropSetElem> set) {
        for (CPropSetElem c : set.getDataflowElements()) {
            // I mean a collection of a single element compared to a single element...
            // It is either is equal or not.
            if (c.getInvolvedIdentifiers().contains(expr)) {
                return c.getConstant();
            }
        }
        return null;
    }

    private Integer parseUnaryExpression(UnaryExpression expr, DefiniteSet<CPropSetElem> set) {
        UnaryOperator op = expr.getOperator();
        ValueExpression e = (ValueExpression) expr.getExpression();
        // I don't want to do recursion but saves LoC and makes sense...
        // One recursion call will not fill the memory... right?
        Integer parsed = parse(e, set);
        if (parsed != null) {
            return (op instanceof NumericNegation) ? -parsed : parsed;
        }
        return null;
    }

    private Integer parseBinaryExpression(BinaryExpression expr, DefiniteSet<CPropSetElem> set) {
        BinaryOperator op = expr.getOperator();
        ValueExpression lhs = (ValueExpression) expr.getLeft();
        ValueExpression rhs = (ValueExpression) expr.getRight();
        // I could avoid recursion though by trying both constant and identifier
        // meh, also doing parseConstant always is not smart
        // Integer left = parseConstant(lhs) != null ? parseConstant(lhs) : parseIdentifier(lhs);
        Integer left = parse(lhs, set);
        Integer right = parse(rhs, set);
        if (left != null && right != null) {
            if (op instanceof AdditionOperator) {
                return left + right;
            }
            if (op instanceof SubtractionOperator) {
                return left - right;
            }
            if (op instanceof MultiplicationOperator) {
                return left * right;
            }
            if (op instanceof DivisionOperator && right != 0) {
                return left / right;
            }
        }
        return null;
    }

    private Integer parse(ValueExpression expr, DefiniteSet<CPropSetElem> elems) {
        // Damn, can't do switch (expr) case Integer and so on because using JDK 21 does not compile :(

        // x = 1
        if (expr instanceof Constant constant) {
            return parseConstant(constant);
        }

        // previously x = some_value
        // y = x
        // I go and search the x value
        if (expr instanceof Identifier identifier) {
            return parseIdentifier(identifier, elems);
        }

        // One operand -> -
        if (expr instanceof UnaryExpression unaryExpr) {
            return parseUnaryExpression(unaryExpr, elems);
        }

        // Two operand -> +-*/
        if (expr instanceof BinaryExpression binaryExpr) {
            return parseBinaryExpression(binaryExpr, elems);
        }

        // Something unexpected
        return null;
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer value = this.parse(expression, state);
        if (value != null) {
            return Set.of(new CPropSetElem(id, value));
        }
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> res = new HashSet<>();
        for (CPropSetElem c : state) {
            if (c.getInvolvedIdentifiers().contains(id)) res.add(c);
        }
        return res;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Set.of();
    }

    @Override
    public DefiniteSet<CPropSetElem> makeLattice() {
        return new DefiniteSet<>();
    }
}