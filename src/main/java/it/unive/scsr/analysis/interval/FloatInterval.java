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


public class FloatInterval implements BaseNonRelationalValueDomain<FloatIntervalLattice> {

	@Override
	public FloatIntervalLattice top() {
		return FloatIntervalLattice.TOP;
	}

	@Override
	public FloatIntervalLattice bottom() {
		return FloatIntervalLattice.BOTTOM;
	}

	@Override
    public FloatIntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
        throws SemanticException {

    if (constant.getValue() instanceof MathNumber) {
        MathNumber n = (MathNumber) constant.getValue();
        return round(new FloatIntervalLattice(n, n));
    }
    
    if (constant.getValue() instanceof Float) {
        float f = (Float) constant.getValue();
        MathNumber n = new MathNumber(new java.math.BigDecimal(String.valueOf(f)));
        return round(new FloatIntervalLattice(n, n));
    }
    
    if (constant.getValue() instanceof Double) {
        double d = (Double) constant.getValue();
        MathNumber n = new MathNumber(new java.math.BigDecimal(String.valueOf(d)));
        return round(new FloatIntervalLattice(n, n));
    }
    
    if (constant.getValue() instanceof Integer) {
        MathNumber n = new MathNumber((long)(Integer) constant.getValue());
        return round(new FloatIntervalLattice(n, n));
    }

    return FloatIntervalLattice.TOP;
}

	@Override
	public FloatIntervalLattice evalUnaryExpression(UnaryExpression expression, FloatIntervalLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {

		if (arg == null)
			return FloatIntervalLattice.BOTTOM;

		if (expression.getOperator() == NumericNegation.INSTANCE) {
			MathNumber u = arg.getHigh();
			MathNumber l = arg.getLow();

			return round(new FloatIntervalLattice(u.multiply(MathNumber.MINUS_ONE), l.multiply(MathNumber.MINUS_ONE)));
		}

		return FloatIntervalLattice.TOP;
	}

	@Override
public FloatIntervalLattice evalBinaryExpression(BinaryExpression expression, FloatIntervalLattice left,
        FloatIntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
    
    if (left == null || right == null)
        return FloatIntervalLattice.BOTTOM;

    if (left.equals(FloatIntervalLattice.TOP) || right.equals(FloatIntervalLattice.TOP))
        return FloatIntervalLattice.TOP;
    
    MathNumber l1 = left.getLow();
    MathNumber l2 = right.getLow();
    MathNumber u1 = left.getHigh();
    MathNumber u2 = right.getHigh();
    
    if (expression.getOperator() instanceof AdditionOperator) {
        return round(new FloatIntervalLattice(l1.add(l2), u1.add(u2)));

    } else if (expression.getOperator() instanceof MultiplicationOperator) {
        return round(mul(left, right));

    } else if (expression.getOperator() instanceof SubtractionOperator) {
        return round(new FloatIntervalLattice(l1.subtract(u2), u1.subtract(l2)));

    } else if (expression.getOperator() instanceof DivisionOperator) {
        if (left.equals(FloatIntervalLattice.ZERO))
            return FloatIntervalLattice.ZERO;
        if (right.equals(FloatIntervalLattice.ZERO))
            return FloatIntervalLattice.TOP;

        if (!includes(right, FloatIntervalLattice.ZERO)) {
            MathNumber recipLow = MathNumber.ONE.divide(u2);
            MathNumber recipHigh = MathNumber.ONE.divide(l2);

            if (!recipLow.isMinusInfinity() && !recipLow.isPlusInfinity()) {
                try {
                    recipLow = new MathNumber(new java.math.BigDecimal(recipLow.toString())
                        .setScale(5, java.math.RoundingMode.HALF_UP));
                } catch (NumberFormatException e) {}
            }
            if (!recipHigh.isMinusInfinity() && !recipHigh.isPlusInfinity()) {
                try {
                    recipHigh = new MathNumber(new java.math.BigDecimal(recipHigh.toString())
                        .setScale(5, java.math.RoundingMode.HALF_UP));
                } catch (NumberFormatException e) {}
            }

            return round(mul(left, new FloatIntervalLattice(recipLow, recipHigh)));

        } else if (u2.isZero()) {
            return round(mul(left, new FloatIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2))));

        } else if (l2.isZero()) {
            return round(mul(left, new FloatIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY)));

        } else {
            FloatIntervalLattice lower = round(mul(left, new FloatIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(l2))));
            FloatIntervalLattice higher = round(mul(left, new FloatIntervalLattice(MathNumber.ONE.divide(u2), MathNumber.PLUS_INFINITY)));

            if (includes(lower, higher))
                return lower;
            else if (includes(higher, lower))
                return higher;
            else {
                MathNumber l = lower.getLow().compareTo(higher.getLow()) > 0 ? higher.getLow() : lower.getLow();
                MathNumber u = lower.getHigh().compareTo(higher.getHigh()) < 0 ? higher.getHigh() : lower.getHigh();
                return round(new FloatIntervalLattice(l, u));
            }
        }
    }
    
    return FloatIntervalLattice.TOP;
}

	private FloatIntervalLattice round(FloatIntervalLattice intervalLattice) {
        // this was better implemented to avoid square root irrational results to be very precise.

        if (intervalLattice.isBottom() || intervalLattice.isTop()) 
        return intervalLattice;


        MathNumber l = intervalLattice.getLow();
        MathNumber u = intervalLattice.getHigh();

        if (l.isMinusInfinity() || u.isPlusInfinity())
            return intervalLattice;

        try {
            l = new MathNumber(new java.math.BigDecimal(l.toString()).setScale(5, java.math.RoundingMode.HALF_UP));
        } catch (NumberFormatException e) {}

        try {
            u = new MathNumber(new java.math.BigDecimal(u.toString()).setScale(5, java.math.RoundingMode.HALF_UP));
        } catch (NumberFormatException e) {}

        return new FloatIntervalLattice(l, u);
	}

	private FloatIntervalLattice mul(FloatIntervalLattice left, FloatIntervalLattice right) {
		MathNumber l1 = left.getLow();
		MathNumber l2 = right.getLow();
		
		MathNumber u1 = left.getHigh();
		MathNumber u2 = right.getHigh();
		
		if (left.equals(FloatIntervalLattice.ZERO) || right.equals(FloatIntervalLattice.ZERO))
			return FloatIntervalLattice.ZERO;
		else {
			if (l1.compareTo(MathNumber.ZERO) >= 0 && l2.compareTo(MathNumber.ZERO) >= 0)
            return new FloatIntervalLattice(l1.multiply(l2), u1.multiply(u2));
			
			MathNumber ll = l1.multiply(l2);
			MathNumber lh = l1.multiply(u2);
			MathNumber hl = u1.multiply(l2);
			MathNumber hh = u1.multiply(u2);
			
			return new FloatIntervalLattice(min(ll, lh, hl, hh), max(ll, lh, hl, hh));
			}	
		}

	private static MathNumber min(MathNumber... nums) {
		if (nums.length == 0)
			throw new IllegalArgumentException("No numbers provided");

		MathNumber min = nums[0];
		for (int i = 1; i < nums.length; i++)
			min = min.min(nums[i]);

		return min;
	}

	private static MathNumber max(MathNumber... nums) {
		if (nums.length == 0)
			throw new IllegalArgumentException("No numbers provided");

		MathNumber max = nums[0];
		for (int i = 1; i < nums.length; i++)
			max = max.max(nums[i]);

		return max;
	}
	
	public boolean includes(FloatIntervalLattice a,
			FloatIntervalLattice b) {
		if (a.isBottom() || b.isBottom())
			return false;
		return a.getLow().compareTo(b.getLow()) <= 0 && a.getHigh().compareTo(b.getHigh()) >= 0;
	}
}