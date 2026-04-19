package it.unive.scsr.analysis.interval;

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
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;

public class Interval implements BaseNonRelationalValueDomain<IntervalLattice>{

	@Override
	public IntervalLattice top() {
		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice bottom() {
		return IntervalLattice.BOTTOM;
	}
	
	@Override
	public IntervalLattice 
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		
		if(constant.getValue() instanceof Integer) {
			//I need to check the integer value to 
			// assign the right approx value
			Integer n = (Integer) constant.getValue();

			return new IntervalLattice(n, n);
		}
			
		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice evalUnaryExpression(UnaryExpression expression, IntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if(arg.i == null)
			return IntervalLattice.BOTTOM;
		
		if(expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.i.getHigh();
			MathNumber l = arg.i.getLow();
			
			return new IntervalLattice(u.multiply(MathNumber.MINUS_ONE),l.multiply(MathNumber.MINUS_ONE));
		}
	
		return IntervalLattice.TOP;
	}

	@Override
	public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left,
			IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		
		if(left.i == null || right == null)
			return IntervalLattice.BOTTOM;
		
		if(expression.getOperator() instanceof AdditionOperator) {
			
			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();
			
			return new IntervalLattice(l1.add(l2), u1.add(u2));
			
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			// [l1, h1] * [l2, h2] = [min(prodotti), max(prodotti)]
			// prodotti = { l1*l2, l1*h2, h1*l2, h1*h2 }.
			// Vanno calcolati questi quattro prodotti e di questi preso il min-max,
			// in quanto posso avere dei cambi di segno, ad esempio NEG * POS oppure NEG * NEG
			// es: [-4, 5], [-3, -1] -> [-15, 12]

			// min-max del primo intervallo
			MathNumber l1 = left.i.getLow();
			MathNumber h1 = left.i.getHigh();

			// min-max del secondo intervallo
			MathNumber l2 = right.i.getLow();
			MathNumber h2 = right.i.getHigh();

			// calcolo il prodotto di tutte le possibili tra le due coppie
			MathNumber p1 = l1.multiply(l2);
			MathNumber p2 = l1.multiply(h2);
			MathNumber p3 = h1.multiply(l2);
			MathNumber p4 = h1.multiply(h2);

			// min-max dei prodotti calcolati sopra
			MathNumber low  = p1.min(p2).min(p3).min(p4);
			MathNumber high = p1.max(p2).max(p3).max(p4);

			// restituisco l'intervallo
			return new IntervalLattice(low, high);

		} else if (expression.getOperator() instanceof SubtractionOperator) {
			// [l1, h1] - [l2, h2] = [l1-h2, h1-l2]

			// min-max del primo intervallo
			MathNumber l1 = left.i.getLow();
			MathNumber h1 = left.i.getHigh();

			// min-max del secondo intervallo
			MathNumber l2 = right.i.getLow();
			MathNumber h2 = right.i.getHigh();

			return new IntervalLattice(l1.subtract(h2), h1.subtract(l2));

		} else if (expression.getOperator() instanceof DivisionOperator) {
			// [l1, h1] / [l2, h2] — stesso ragionamento della moltiplicazione, ma facendo attenzione alla divisione per 0

			// min-max del primo intervallo
			MathNumber l2 = right.i.getLow();
			MathNumber h2 = right.i.getHigh();

			// min-max del secondo intervallo
			MathNumber l1 = left.i.getLow();
			MathNumber h1 = left.i.getHigh();

			// se il secondo intervallo (denominatore) è [0, 0], la divisione non è possibile
			if (l2.equals(MathNumber.ZERO) && h2.equals(MathNumber.ZERO)) {
				return IntervalLattice.BOTTOM;
			}

			// se il denominatore nel suo intervallo include lo zero, significa che potrei avere una divisione per zero, devo restituire TOP
			if (l2.leq(MathNumber.ZERO) && h2.geq(MathNumber.ZERO)) {
				return IntervalLattice.TOP;
			}

			MathNumber p1 = l1.divide(l2);
			MathNumber p2 = l1.divide(h2);
			MathNumber p3 = h1.divide(l2);
			MathNumber p4 = h1.divide(h2);

			MathNumber low  = p1.min(p2).min(p3).min(p4);
			MathNumber high = p1.max(p2).max(p3).max(p4);

			return new IntervalLattice(low, high);
		}
		
		return IntervalLattice.TOP;
	}
}
