package it.unive.scsr.analysis.interval.floats;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class FloatIntervalLattice
        implements BaseLattice<FloatIntervalLattice>, Comparable<FloatIntervalLattice> {

    final IntInterval i;

    // Thresholds for widening
    private static final MathNumber[] POS_THRESHOLDS = {
            new MathNumber(1),
            new MathNumber(10),
            new MathNumber(100),
            new MathNumber(1000)
    };
    private static final MathNumber[] NEG_THRESHOLDS = {
            new MathNumber(-1000),
            new MathNumber(-100),
            new MathNumber(-10),
            new MathNumber(-1)
    };

    private static MathNumber nextUpperThreshold(MathNumber value) {
        for (MathNumber t : POS_THRESHOLDS)
            if (t.geq(value)) return t;
        return MathNumber.PLUS_INFINITY;
    }

    private static MathNumber nextLowerThreshold(MathNumber value) {
        for (int i = NEG_THRESHOLDS.length - 1; i >= 0; i--)
            if (NEG_THRESHOLDS[i].leq(value)) return NEG_THRESHOLDS[i];
        return MathNumber.MINUS_INFINITY;
    }

    public static FloatIntervalLattice TOP = new FloatIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static FloatIntervalLattice BOTTOM = new FloatIntervalLattice(null);
    public static FloatIntervalLattice ZERO = new FloatIntervalLattice(0, 0);

    public FloatIntervalLattice(IntInterval i) {
        this.i = i;
    }

    public FloatIntervalLattice(MathNumber l, MathNumber u) {
        this.i = new IntInterval(l, u);
    }

    public FloatIntervalLattice(int l, int u) {
        this.i = new IntInterval(l, u);
    }

    public FloatIntervalLattice() {
        this(IntInterval.INFINITY);
    }


    @Override
    public FloatIntervalLattice top() {
        return TOP;
    }

    @Override
    public FloatIntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM)
            return Lattice.bottomRepresentation();

        MathNumber l = this.i.getLow();
        MathNumber u = this.i.getHigh();

        return new StringRepresentation("[" + l + "," + u + "]");
    }

    @Override
    public FloatIntervalLattice lubAux(FloatIntervalLattice other) throws SemanticException {
        if (this.i == null || other.i == null)
            return BOTTOM;

        MathNumber l1 = this.i.getLow();
        MathNumber l2 = other.i.getLow();

        MathNumber lResult;
        if (l1.leq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = other.i.getHigh();

        MathNumber uResult;

        if (u1.geq(u2))
            uResult = u1;
        else
            uResult = u2;

        return new FloatIntervalLattice(lResult, uResult);
    }


    @Override
    public FloatIntervalLattice glbAux(FloatIntervalLattice other) throws SemanticException {
        if (this.i == null || other.i == null)
            return BOTTOM;

        MathNumber l1 = this.i.getLow();
        MathNumber l2 = other.i.getLow();

        MathNumber lResult;
        if (l1.geq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = other.i.getHigh();

        MathNumber uResult;
        if (u1.leq(u2))
            uResult = u1;
        else
            uResult = u2;

        //the intersection is empty
        if (lResult.gt(uResult)) {
            return BOTTOM;
        }

        return new FloatIntervalLattice(lResult, uResult);
    }

    @Override
    public boolean lessOrEqualAux(FloatIntervalLattice other) throws SemanticException {
        if (this.i == null || other.i == null)
            return false;
        //return this.i.includes(other.i);
        return other.i.includes(this.i);
    }

    @Override
    public FloatIntervalLattice wideningAux(FloatIntervalLattice other) throws SemanticException {
        if (this.i == null || other.i == null)
            return BOTTOM;

        MathNumber upperOld = this.i.getHigh();
        MathNumber upperNew = other.i.getHigh();

        MathNumber upperResult = upperOld;
        if (upperNew.gt(upperOld))
            upperResult = nextUpperThreshold(upperNew);

        MathNumber lowerOld = this.i.getLow();
        MathNumber lowerNew = other.i.getLow();

        MathNumber lowerResult = lowerOld;
        if (lowerNew.lt(lowerOld))
            lowerResult = nextLowerThreshold(lowerNew);

        return new FloatIntervalLattice(lowerResult, upperResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(i);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FloatIntervalLattice other = (FloatIntervalLattice) obj;
        return Objects.equals(i, other.i);
    }

    @Override
    public int compareTo(FloatIntervalLattice o) {
        if (isBottom())
            return o.isBottom() ? 0 : -1;
        if (isTop())
            return o.isTop() ? 0 : 1;

        if (o.isBottom())
            return 1;

        if (isTop())
            return -1;

        return i.compareTo(o.i);
    }
}