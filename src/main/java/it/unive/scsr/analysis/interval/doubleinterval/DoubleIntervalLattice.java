package it.unive.scsr.analysis.interval.doubleinterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class DoubleIntervalLattice
        implements BaseLattice<DoubleIntervalLattice>, Comparable<DoubleIntervalLattice> {

    private final MathNumber low;
    private final MathNumber high;
    public static DoubleIntervalLattice TOP = new DoubleIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static DoubleIntervalLattice BOTTOM = new DoubleIntervalLattice(MathNumber.PLUS_INFINITY, MathNumber.MINUS_INFINITY);
    public static DoubleIntervalLattice ZERO = new DoubleIntervalLattice(0.0, 0.0);

    public DoubleIntervalLattice(MathNumber l, MathNumber u) {
        this.low = l;
        this.high = u;
    }

    public DoubleIntervalLattice() {
        this(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    }

    public DoubleIntervalLattice(double l, double u) {
        this.low = new MathNumber(l);
        this.high = new MathNumber(u);
    }

    public MathNumber getLow() {
        return this.low;
    }

    public MathNumber getHigh() {
        return this.high;
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
        // TODO: rethink as task says
        if ((this.low == null || this.high == null) || (other.low == null || other.high == null))
            return BOTTOM;

        MathNumber u1 = this.getHigh();
        MathNumber u2 = other.getHigh();

        MathNumber uResult = u1;
        if (u2.gt(u1))
            uResult = MathNumber.PLUS_INFINITY;

        MathNumber l1 = this.getLow();
        MathNumber l2 = other.getLow();

        MathNumber lResult = l1;
        if (l2.lt(l1)) {
            lResult = MathNumber.MINUS_INFINITY;
        }

        return new DoubleIntervalLattice(lResult, uResult);

    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
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
        int lowCompare = this.getLow().compareTo(o.getLow());
        if (lowCompare != 0)
            return this.getHigh().compareTo(o.getHigh());

        return lowCompare;
    }


}
