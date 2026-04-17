package it.unive.scsr.analysis.interval;

import com.sun.source.doctree.SystemPropertyTree;
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

import java.util.ArrayList;
import java.util.List;

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


			MathNumber ladd = l1.add(l2);
			MathNumber uadd = u1.add(u2);

			if (ladd.isNaN() || uadd.isNaN())
				return IntervalLattice.TOP;

			return new IntervalLattice(ladd, uadd);
			
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			// [ l1, u1 ]   [ l2, u2 ]
			// TODO: fare check funzionamento per BOTTOM, TOP, infiniti vari, intervalli che contengono 0 (soprattuto per divisione)

			// System.out.println("-------------- Multiplication -------------- ");
			System.out.print(left.representation() + " * " + right.representation() + " = ");

			if (left.isBottom() || right.isBottom()) {
				// System.out.println("Bottom");
				return IntervalLattice.BOTTOM;
			}

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			MathNumber p1 = (l1.isInfinite() && l2.isZero()) || (l1.isZero() && l2.isInfinite()) ? MathNumber.ZERO : l1.multiply(l2);
			MathNumber p2 = (l1.isInfinite() && u2.isZero()) || (l1.isZero() && u2.isInfinite()) ? MathNumber.ZERO : l1.multiply(u2);
			MathNumber p3 = (u1.isInfinite() && l2.isZero()) || (u1.isZero() && l2.isInfinite()) ? MathNumber.ZERO : u1.multiply(l2);
			MathNumber p4 = (u1.isInfinite() && u2.isZero()) || (u1.isZero() && u2.isInfinite()) ? MathNumber.ZERO : u1.multiply(u2);

			IntervalLattice res = new IntervalLattice(p1.min(p2).min(p3).min(p4), p1.max(p2).max(p3).max(p4));
			// System.out.println(res.representation());
			return res;
			// TODO: homework
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			// System.out.println("-------------- Subtraction -------------- ");
			System.out.print(left.representation() + " - " + right.representation() + " = ");

			if (left.isBottom() || right.isBottom()){
				// System.out.println("Bottom");
				return IntervalLattice.BOTTOM;
			}

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			MathNumber lsub = l1.subtract(u2);
			MathNumber usub = u1.subtract(l2);

			if (lsub.isNaN() || usub.isNaN())
				return IntervalLattice.TOP;

			IntervalLattice res = new IntervalLattice(lsub, usub);
			// System.out.println(res.representation());
			return res;
			// TODO: homework
		} else if (expression.getOperator() instanceof DivisionOperator) {
			// System.out.println("-------------- Division -------------- ");
			System.out.print(left.representation() + " / " + right.representation() + " = ");

			if (left.isBottom() || right.isBottom()) {
				// System.out.println("Bottom");
				return IntervalLattice.BOTTOM;
			}

			MathNumber u1 = left.i.getHigh();
			MathNumber u2 = right.i.getHigh();
			MathNumber l1 = left.i.getLow();
			MathNumber l2 = right.i.getLow();

			IntervalLattice res;

			if (l2.isZero() && u2.isZero()) { // [l1,u1] / [0,0]
				// System.out.println("Bottom");
				return IntervalLattice.BOTTOM;
			}

			if (l2.isZero()) { // [l1,u1] / [0,?]
				if (u2.isInfinite())
					res = IntervalLattice.TOP;
				else if (l1.compareTo(MathNumber.ZERO) >= 0)
					res = new IntervalLattice(u1.divide(u2), MathNumber.PLUS_INFINITY); // [+,+] / [0,+] = [u1/u2, +inf]
				else if (u1.isNegative())
					res = new IntervalLattice(MathNumber.MINUS_INFINITY, l1.divide(u2)); // [-,-] / [0,+] = [-inf, l1/u2]
				else
					res = IntervalLattice.TOP; // [-,+] / [0,+] = [-inf, +inf]
				// System.out.println(res.representation());
				return res;
			} else if (u2.isZero()) { // [l1,u1] / [?,0]
				if (l2.isInfinite())
					res = IntervalLattice.TOP;
				else if (l1.compareTo(MathNumber.ZERO) >= 0)
					res = new IntervalLattice(MathNumber.MINUS_INFINITY, u1.divide(l2)); // [+,+] / [-,0] = [-inf, u1/l2]
				else if (u1.isNegative())
					res = new IntervalLattice(l1.divide(l2), MathNumber.PLUS_INFINITY); // [-,-] / [-,0] = [l1/l2, +inf]
				else
					res = IntervalLattice.TOP; // [-,+] / [-,0]
				// System.out.println(res.representation());
				return res;
			}

			MathNumber minV = MathNumber.PLUS_INFINITY;
			MathNumber maxV = MathNumber.MINUS_INFINITY;

			List<MathNumber> vals = new ArrayList<>(List.of(
				l1.divide(l2),
				l1.divide(u2),
				u1.divide(l2),
				u1.divide(u2)
			));

			for (MathNumber val : vals) {
				if (!val.isNaN()) {
					minV = minV.min(val);
					maxV = maxV.max(val);
				}
			}

			res = new IntervalLattice(minV, maxV);
			// System.out.println(res.representation());
			return res;


			// TODO: homework
		}
		
		return IntervalLattice.TOP;
	}

	

	
	
}
