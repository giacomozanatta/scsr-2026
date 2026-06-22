package it.unive.scsr.analysis.interval.extended;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class NumericIntervalLattice
    implements BaseLattice<NumericIntervalLattice>, Comparable<NumericIntervalLattice> {

  private static final MathNumber WIDENING_THRESHOLD = new MathNumber(Math.pow(10, -3));

  private final MathNumber low;
  private final MathNumber high;

  public static final NumericIntervalLattice TOP = new NumericIntervalLattice(MathNumber.MINUS_INFINITY,
      MathNumber.PLUS_INFINITY);

  public static final NumericIntervalLattice BOTTOM = new NumericIntervalLattice(null, null);

  public static final NumericIntervalLattice ZERO = new NumericIntervalLattice(MathNumber.ZERO, MathNumber.ZERO);

  public NumericIntervalLattice(MathNumber low, MathNumber high) {
    if (low == null && high == null) {
      this.low = null;
      this.high = null;

    } else {
      if (low == null)
        low = MathNumber.MINUS_INFINITY;

      if (high == null)
        high = MathNumber.PLUS_INFINITY;

      if (low.isNaN() || high.isNaN()) {
        this.low = null;
        this.high = null;

      } else if (low.leq(high)) {
        this.low = low;
        this.high = high;
      } else {
        this.low = high;
        this.high = low;
      }
    }
  }

  public NumericIntervalLattice(int low, int high) {
    this(new MathNumber(low), new MathNumber(high));
  }

  public NumericIntervalLattice(long low, long high) {
    this(new MathNumber(low), new MathNumber(high));
  }

  public NumericIntervalLattice(float low, float high) {
    this(fromFloat(low), fromFloat(high));
  }

  public NumericIntervalLattice(double low, double high) {
    this(fromDouble(low), fromDouble(high));
  }

  // public NumericIntervalLattice(Number low, Number high) {
  // this(fromNumber(low), fromNumber(high));
  // }

  // private static MathNumber fromNumber(Number number) {
  // if (number == null)
  // return null;

  // if (number instanceof Byte || number instanceof Short || number instanceof
  // Integer || number instanceof Long)
  // return new MathNumber(number.longValue());

  // if (number instanceof BigInteger bi)
  // return new MathNumber(new BigDecimal(bi));

  // if (number instanceof BigDecimal bd)
  // return new MathNumber(bd);

  // if (number instanceof Float)
  // return fromFloat(number.floatValue());

  // if (number instanceof Double)
  // return fromDouble(number.doubleValue());

  // return new MathNumber(number.doubleValue());
  // }

  private static MathNumber fromFloat(float value) {
    if (Float.isNaN(value))
      return MathNumber.NaN;

    if (value == Float.POSITIVE_INFINITY)
      return MathNumber.PLUS_INFINITY;

    if (value == Float.NEGATIVE_INFINITY)
      return MathNumber.MINUS_INFINITY;

    return new MathNumber((double) value);
  }

  private static MathNumber fromDouble(double value) {
    if (Double.isNaN(value))
      return MathNumber.NaN;

    if (value == Double.POSITIVE_INFINITY)
      return MathNumber.PLUS_INFINITY;

    if (value == Double.NEGATIVE_INFINITY)
      return MathNumber.MINUS_INFINITY;

    return new MathNumber(value);
  }

  public MathNumber getLow() {
    return low;
  }

  public MathNumber getHigh() {
    return high;
  }

  @Override
  public NumericIntervalLattice top() {
    return TOP;
  }

  @Override
  public NumericIntervalLattice bottom() {
    return BOTTOM;
  }

  @Override
  public boolean isBottom() {
    return low == null && high == null;
  }

  @Override
  public boolean isTop() {
    return !isBottom()
        && getLow().isMinusInfinity()
        && getHigh().isPlusInfinity();
  }

  @Override
  public StructuredRepresentation representation() {
    if (isBottom())
      return Lattice.bottomRepresentation();

    if (isTop())
      return Lattice.topRepresentation();

    return new StringRepresentation("[" + low + "," + high + "]");
  }

  @Override
  public NumericIntervalLattice lubAux(NumericIntervalLattice other) throws SemanticException {
    if (isBottom())
      return other;

    if (other.isBottom())
      return this;

    return new NumericIntervalLattice(
        getLow().min(other.getLow()),
        getHigh().max(other.getHigh()));
  }

  @Override
  public NumericIntervalLattice glbAux(NumericIntervalLattice other) throws SemanticException {
    if (this.isBottom() || other.isBottom())
      return BOTTOM;

    MathNumber newLow = this.low.max(other.low);
    MathNumber newHigh = this.high.min(other.high);

    if (newLow.gt(newHigh))
      return BOTTOM;

    return new NumericIntervalLattice(newLow, newHigh);
  }

  @Override
  public boolean lessOrEqualAux(NumericIntervalLattice other) throws SemanticException {
    if (isBottom())
      return true;

    if (other.isBottom())
      return false;

    return other.getLow().leq(getLow())
        && other.getHigh().geq(getHigh());
  }

  @Override
  public NumericIntervalLattice wideningAux(NumericIntervalLattice other) throws SemanticException {
    if (isBottom())
      return other;

    if (other.isBottom())
      return this;

    if (this.isTop() || other.isTop())
      return TOP;

    MathNumber l1 = getLow();
    MathNumber u1 = getHigh();

    MathNumber l2 = other.getLow();
    MathNumber u2 = other.getHigh();

    MathNumber diffLower = l1.subtract(l2).abs();
    MathNumber diffUpper = u1.subtract(u2).abs();

    MathNumber newLow;
    MathNumber newHigh;

    if (diffLower.geq(WIDENING_THRESHOLD))
      newLow = MathNumber.MINUS_INFINITY;
    else
      newLow = l1.min(l2);

    if (diffUpper.geq(WIDENING_THRESHOLD))
      newHigh = MathNumber.PLUS_INFINITY;
    else
      newHigh = u1.max(u2);

    return new NumericIntervalLattice(newLow, newHigh);
  }

  @Override
  public int hashCode() {
    return Objects.hash(low, high);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (!(obj instanceof NumericIntervalLattice other))
      return false;

    return Objects.equals(low, other.low)
        && Objects.equals(high, other.high);
  }

  @Override
  public int compareTo(NumericIntervalLattice other) {
    if (this.isBottom())
      return other.isBottom() ? 0 : -1;

    if (this.isTop())
      return other.isTop() ? 0 : 1;

    if (other.isBottom())
      return 1;

    if (other.isTop())
      return -1;

    int cmp = this.low.compareTo(other.low);

    if (cmp != 0)
      return cmp;

    return this.high.compareTo(other.high);
  }
}
