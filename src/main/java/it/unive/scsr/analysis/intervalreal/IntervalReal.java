package it.unive.scsr.analysis.intervalreal;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StructuredRepresentation;


/**
 * IntervalReal Class
 * 
 * An implementation of {@link BaseNonRelationalValueDomain} that represents sets of real numbers as intervals [l, u]. Note that:
 *  - The interval is closed, meaning it includes its endpoints (for ease of implementation we do not consider open intervals);
 *  - The class supports basic arithmetic operations and handles special cases like division by zero by returning TOP;
 *  - Unlike intervals, rounding is not necessary for real numbers, as they can be represented with arbitrary precision; 
 *  - We still need to consider the possibility of infinite bounds.
 * 
 * @author Gianmaria Pizzo 872966
 */
public class IntervalReal implements BaseNonRelationalValueDomain<IntervalRealLattice> {


}
