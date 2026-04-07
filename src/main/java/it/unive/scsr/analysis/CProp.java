package it.unive.scsr.analysis;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;
import it.unive.lisa.analysis.dataflow.DataflowDomain;
import it.unive.lisa.analysis.dataflow.ReachingDefinitions;
import it.unive.lisa.analysis.dataflow.DefiniteSet;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
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
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class CProp extends DataflowDomain<DefiniteSet<CPropSetElem>, CPropSetElem> {

	public CProp() {super();}

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

	// we obtain the value for a certain id 
    private static Integer getValueOf(Identifier id, DefiniteSet<CPropSetElem> set) {
        for (CPropSetElem cp : set.getDataflowElements()) if (cp.getId().equals(id)) return cp.getConstant();
        return null;
    }

	// we parse the program and evaluate every variable that appears in the code
    private static Integer eval(ValueExpression exp, DefiniteSet<CPropSetElem> set) {
		// null expression, no matter
        if (exp == null) return null;

		// a constant, we find the value and then we return the value
        if (exp instanceof Constant) {
            Object value = ((Constant) exp).getValue(); 
			if (value instanceof Integer) return (Integer) value;
        }

		// an id, we find and return its value 
        if (exp instanceof Identifier) return getValueOf((Identifier) exp, set);

		// an expression, like +-B, we find the sign and return it 
		if (exp instanceof UnaryExpression) {
			UnaryExpression unary = (UnaryExpression) exp;
			UnaryOperator operator = unary.getOperator();
			ValueExpression arg = (ValueExpression) unary.getExpression();
			Integer value = eval(arg, set);
			if (value == null) return null;
			if (operator instanceof NumericNegation) return (-1) * value;
		}
		// an expression like A op B, we fing the operation and the values of the involved id, assigning that to a variable
		if (exp instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) exp;
			BinaryOperator operator = binary.getOperator();
			ValueExpression left = (ValueExpression) binary.getLeft();
			ValueExpression right = (ValueExpression) binary.getRight();
			Integer lvalue = eval(left, set);
			Integer rvalue = eval(right, set);
			if (lvalue == null || rvalue == null) return null;
			if (operator instanceof AdditionOperator) return lvalue + rvalue;
			if (operator instanceof SubtractionOperator) return lvalue - rvalue;
			if (operator instanceof MultiplicationOperator) return lvalue * rvalue;
			if (operator instanceof DivisionOperator && rvalue != 0) return lvalue / rvalue;
		}

		return null;
	}


    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Integer value = eval(expression, state);
        if (value != null) return Collections.singleton(new CPropSetElem(id, value));
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> gen(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        Set<CPropSetElem> result = new HashSet<>();
        for (CPropSetElem cp : state.getDataflowElements()) if (cp.getId().equals(id)) result.add(cp);
        return result;
    }

    @Override
    public Set<CPropSetElem> kill(DefiniteSet<CPropSetElem> state, ValueExpression expression, ProgramPoint pp) throws SemanticException {
        return Collections.emptySet();
    }

}