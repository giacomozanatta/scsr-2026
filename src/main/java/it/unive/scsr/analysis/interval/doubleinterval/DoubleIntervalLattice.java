package it.unive.scsr.analysis.interval.doubleinterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public record DoubleIntervalLattice(MathNumber low, MathNumber high)
        implements BaseLattice<DoubleIntervalLattice>, Comparable<DoubleIntervalLattice> {

    public static DoubleIntervalLattice TOP = new DoubleIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static DoubleIntervalLattice BOTTOM = new DoubleIntervalLattice(MathNumber.PLUS_INFINITY, MathNumber.MINUS_INFINITY);
    public static DoubleIntervalLattice ZERO = new DoubleIntervalLattice(0.0, 0.0);

    public DoubleIntervalLattice() {
        this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    }

    public DoubleIntervalLattice(double l, double u) {
        this(new MathNumber(l), new MathNumber(u));
    }

    @Override
    public DoubleIntervalLattice top() {
        return TOP;
    }

    @Override
    public DoubleIntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM)
            return Lattice.bottomRepresentation();

        return new StringRepresentation("[" + this.low + "," + this.high + "]");
    }

    @Override
    public DoubleIntervalLattice lubAux(DoubleIntervalLattice other) throws SemanticException {

        if ((this.low == null || this.high == null) || (other.low == null || other.high == null))
            return BOTTOM;

        MathNumber l1 = this.low;
        MathNumber l2 = other.low;

        MathNumber lResult;
        if (l1.leq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.high;
        MathNumber u2 = other.high;

        MathNumber uResult;

        if (u1.geq(u2))
            uResult = u1;
        else
            uResult = u2;

        return new DoubleIntervalLattice(lResult, uResult);
    }


    @Override
    public DoubleIntervalLattice glbAux(DoubleIntervalLattice other) throws SemanticException {


        if ((this.low == null || this.high == null) || (other.low == null || other.high == null))
            return BOTTOM;

        MathNumber l1 = this.low;
        MathNumber l2 = other.low;

        MathNumber lResult;
        if (l1.geq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.high;
        MathNumber u2 = other.high;

        MathNumber uResult;
        if (u1.leq(u2))
            uResult = u1;
        else
            uResult = u2;

        return new DoubleIntervalLattice(lResult, uResult);
    }

    private boolean includes(
            DoubleIntervalLattice other) {
        if (isBottom() || other.isBottom())
            return false;
        return low.compareTo(other.low) <= 0 && high.compareTo(other.high) >= 0;
    }

    @Override
    public boolean lessOrEqualAux(DoubleIntervalLattice other) throws SemanticException {
        if ((this.low == null || this.high == null) || (other.low == null || other.high == null))
            return false;
        return other.includes(this);
    }

    @Override
    public DoubleIntervalLattice wideningAux(DoubleIntervalLattice other) throws SemanticException {
        if ((this.low == null || this.high == null) || (other.low == null || other.high == null))
            return BOTTOM;
        MathNumber u1 = this.high();
        MathNumber u2 = other.high();

        MathNumber l1 = this.low();
        MathNumber l2 = other.low();

        // IDEA: set a threshold, if trespassed over-approximate to +- infinity
        // Inspired by: slides 20-21-... lesson 8
        // https://matthewbdwyer.github.io/6620/slides/5-widening-and-narrowing.pdf
        // |5-3| = |3-5|
        // TODO: a valid threshold?
        // How small can we go?
        MathNumber threshold = new MathNumber(
                Math.pow(10, -10)
        );
        MathNumber diffLower = l1.subtract(l2).abs();
        MathNumber lResult = null;
        MathNumber diffUpper = u1.subtract(u2).abs();
        MathNumber uResult = null;
        if (diffLower.geq(threshold)) {
            lResult = MathNumber.MINUS_INFINITY;
        } else {
            lResult = l1.min(l2);
        }

        if (diffUpper.geq(threshold)) {
            uResult = MathNumber.PLUS_INFINITY;
        } else {
            uResult = u1.max(u2);
        }

        return new DoubleIntervalLattice(lResult, uResult);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DoubleIntervalLattice other = (DoubleIntervalLattice) obj;
        return this.low.equals(other.low) && this.high.equals(other.high);
    }

    @Override
    public int compareTo(DoubleIntervalLattice o) {
        if (isBottom())
            return o.isBottom() ? 0 : -1;
        if (isTop())
            return o.isTop() ? 0 : 1;

        if (o.isBottom())
            return 1;

        if (isTop())
            return -1;

        int lowCompare = this.low().compareTo(o.low());
        if (lowCompare != 0)
            return this.high().compareTo(o.high());

        return lowCompare;
    }
}
