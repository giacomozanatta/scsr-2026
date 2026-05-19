package it.unive.scsr.analysis.interval.floatinterval;

import it.unive.lisa.util.numeric.MathNumber;

import java.util.Objects;

public class NumberInterval implements Comparable<NumberInterval> {

	public static final NumberInterval INFINITY = new NumberInterval();
	public static final NumberInterval MINUS_ONE = new NumberInterval(-1.0,-1.0);
	public static final NumberInterval ZERO = new NumberInterval(0.0, 0.0);
	public static final NumberInterval ONE= new NumberInterval(1.0, 1.0);
	public static final NumberInterval NaN = new NumberInterval(MathNumber.NaN, MathNumber.NaN);

	private final MathNumber low;
	private final MathNumber high;

	/* CONSTRUCTORS */
	public NumberInterval(){ this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY); }
	public NumberInterval(double low, double high){ this(Double.valueOf(low), Double.valueOf(high));}
	public NumberInterval(long low, long high){ this(Long.valueOf(low), Long.valueOf(high));}
	public NumberInterval(Number low, Number high){
		this(toMathNumber(low, MathNumber.MINUS_INFINITY),toMathNumber(high, MathNumber.PLUS_INFINITY));
	}

	private static MathNumber toMathNumber(Number num, MathNumber default_value){
		if(num == null) return default_value;
		if(num instanceof Double || num instanceof Float){
			return new MathNumber(num.doubleValue());
		}
		return new MathNumber(num.longValue());
	}

	public NumberInterval(MathNumber low, MathNumber high) {
		if(low == null && high == null){
			this.low = null;
			this.high = null;
		}
		else {
			Objects.requireNonNull(low, "Low bound must not be null");
			Objects.requireNonNull(high, "High bound must not be null");
			if(low.isNaN() || high.isNaN()){
				this.low = MathNumber.NaN;
				this.high = MathNumber.NaN;
			}
			else if(low.compareTo(high) <= 0){
				this.low = low;
				this.high = high;
			}
			else{
				this.low = high;
				this.high = low;
			}
		}
	}

	public MathNumber getLow(){ return low; }
	public MathNumber getHigh(){ return high; }

	/* FLAGS */
	public boolean isLowMinusInfinity(){ return low.isMinusInfinity(); }
	public boolean isHighPlusInfinity(){ return high.isPlusInfinity(); }
	public boolean isInfinity(){ return isLowMinusInfinity() && isHighPlusInfinity(); }
	public boolean isInfinite(){ return isLowMinusInfinity() || isHighPlusInfinity(); }
	public boolean isFinite() {  return !isInfinite(); }
	public boolean isSingleton(){ return isFinite() && low.equals(high); }
	public boolean is(Number x){ return isSingleton() && low.equals(toMathNumber(x, null)); }

	/* UTILITIES */
	public static MathNumber min(MathNumber... nums){
		if(nums.length == 0) throw new IllegalArgumentException("No numbers provided");
		MathNumber min = nums[0];
		for(int i = 1; i < nums.length; i++){
			min = min.min(nums[i]);
		}
		return min;
	}
	public static MathNumber max(MathNumber... nums){
		if(nums.length == 0) throw new IllegalArgumentException("No numbers provided");
		MathNumber max = nums[0];
		for(int i = 1; i < nums.length; i++){
			max = max.min(nums[i]);
		}
		return max;
	}

	private static NumberInterval cacheAndRound(NumberInterval i){
		if(i.is(-1)) return MINUS_ONE;
		if(i.is(0)) return ZERO;
		if(i.is(1)) return ONE;
		if(i.isInfinity()) return INFINITY;
		return new NumberInterval(i.low, i.high);
	}

	public boolean includes(NumberInterval other){
		return low.compareTo(other.low) <= 0 && high.compareTo(other.high) >= 0;
	}

	public boolean intersects(NumberInterval other){
		boolean this_before_other = other.low.compareTo(high) <= 0;
		boolean other_before_this = low.compareTo(other.high) <= 0;
		return includes(other) || other.includes(this) || this_before_other || other_before_this;
	}

	/* ARITHMETIC OPERATORS */
	public NumberInterval plus(NumberInterval other){
		if(isInfinity() || other.isInfinity()) return INFINITY; // [-inf, +inf] + [1,2] = [-inf, +inf]

		return cacheAndRound(new NumberInterval(low.add(other.low), high.add(other.high)));
	}
	public NumberInterval diff(NumberInterval other){
		if(isInfinity() || other.isInfinity()) return INFINITY; // [-inf, +inf] + [1,2] = [-inf, +inf]

		// [2,3] - [4,5] -> smallest number = 2-5, biggest number = 3-4
		return cacheAndRound(new NumberInterval(low.subtract(other.high), high.subtract(other.low)));
	}

	public NumberInterval mul(NumberInterval other){
		if(is(0) || other.is(0)) return ZERO;
		if(isInfinity() || other.isInfinity()) return INFINITY;

		// Both intervals non-negative
		if(low.compareTo(MathNumber.ZERO) >= 0 && other.low.compareTo(MathNumber.ZERO) >= 0){
			return cacheAndRound(new NumberInterval(low.multiply(other.low), high.multiply(other.high)));
		}

		// Default case: at least one interval has negative numbers in it
		MathNumber lol = low.multiply(other.low);
		MathNumber loh = low.multiply(other.high);
		MathNumber hoh = high.multiply(other.high);
		MathNumber hol = high.multiply(other.low);
		return cacheAndRound(new NumberInterval(min(lol, loh, hoh, hol), max(lol, loh, hoh, hol)));
	}

	public NumberInterval div(NumberInterval other, boolean ignoreZero, boolean errorOnZero){
		if(errorOnZero && (other.is(0) || other.includes(ZERO))) throw new ArithmeticException("DoubleInterval division by zero");

		if(is(0)) return ZERO;

		if(!other.includes(ZERO)){
			// if zero is not present, then
			// (1/high,1/low) because 1/high is smaller than 1/low regardless of sign
			// [1,1]/[2,4] = [1/4, 1/2],   [1,1]/[-2,-0.6] = [-10/6 > 1, -0.5]
			return mul(new NumberInterval(MathNumber.ONE.divide(other.high),MathNumber.ONE.divide(other.low)));
		}
		else if(other.high.isZero()){
			return mul(new NumberInterval(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(other.low)));
		}
		else if(other.low.isZero()){
			return mul(new NumberInterval(MathNumber.ONE.divide(other.high), MathNumber.PLUS_INFINITY));
		}
		else if(ignoreZero){
			return mul(new NumberInterval(MathNumber.ONE.divide(other.low), MathNumber.ONE.divide(other.high)));
		}
		else{
			NumberInterval lower = mul(new NumberInterval(MathNumber.MINUS_INFINITY, MathNumber.ONE.divide(other.low)));
			NumberInterval higher = mul(new NumberInterval(MathNumber.ONE.divide(other.high), MathNumber.PLUS_INFINITY));

			if (lower.includes(higher))
				return lower;
			else if (higher.includes(lower))
				return higher;
			else{
				MathNumber new_low = lower.low.compareTo(higher.low) < 0 ? lower.low : higher.low;
				MathNumber new_high = lower.high.compareTo(higher.high) > 0 ? lower.high : higher.high;
				return cacheAndRound( new NumberInterval(new_low, new_high) );
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((high == null) ? 0 : high.hashCode());
		result = prime * result + ((low == null) ? 0 : low.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null || getClass() != obj.getClass())
			return false;

		NumberInterval other = (NumberInterval) obj;
		return java.util.Objects.equals(low, other.low) && java.util.Objects.equals(high, other.high);
	}

	@Override
	public String toString() {
		return "[" + low + ", " + high + "]";
	}

	@Override
	public int compareTo(NumberInterval o) {
		int cmp = low.compareTo(o.low);
		return cmp != 0 ? cmp : high.compareTo(o.high);
	}
}
