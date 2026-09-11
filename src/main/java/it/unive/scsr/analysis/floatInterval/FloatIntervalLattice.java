package it.unive.scsr.analysis.floatInterval;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.interval.IntervalLattice;

public class FloatIntervalLattice implements BaseLattice<FloatIntervalLattice>, Comparable<FloatIntervalLattice>{


    private final MathNumber low;
    private final MathNumber high;


    public static FloatIntervalLattice TOP = new FloatIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static FloatIntervalLattice BOTTOM = new FloatIntervalLattice((MathNumber)null, (MathNumber)null);
    public static FloatIntervalLattice ZERO = new FloatIntervalLattice(MathNumber.ZERO,MathNumber.ZERO);

    public FloatIntervalLattice(){
        this.low=MathNumber.MINUS_INFINITY;
        this.high=MathNumber.PLUS_INFINITY;
    }

    //adapted by IntInterval.java line 128
    public FloatIntervalLattice(MathNumber low, MathNumber high) {
        if (low == null && high == null) {
            this.low = null;
            this.high = null;
        } else {
            Objects.requireNonNull(low, "Low bound must not be null");
            Objects.requireNonNull(high, "High bound must not be null");
            if (low.isNaN() || high.isNaN()) {
                this.low = MathNumber.NaN;
                this.high = MathNumber.NaN;
            } else if (low.compareTo(high) <= 0) {
                this.low = low;
                this.high = high;
            } else {
                this.low = high;
                this.high = low;
            }
        }
    }
    public FloatIntervalLattice(double low, double high){
        this.low= new MathNumber(low);
        this.high=new MathNumber(high);
    }

    public MathNumber getHigh() {return high;}
    public MathNumber getLow() {return low;}


    @Override
    public FloatIntervalLattice top(){return TOP;}
    @Override
    public FloatIntervalLattice bottom(){return BOTTOM;}


    @Override
    public StructuredRepresentation representation() {
        if(this == BOTTOM)
            return Lattice.bottomRepresentation();
        return new StringRepresentation("["+low+","+high+"]");
    }

    @Override
    public FloatIntervalLattice lubAux(FloatIntervalLattice other){
        MathNumber newLow= this.low.min(other.low);
        MathNumber newHigh= this.high.max(other.high);
        return new FloatIntervalLattice(newLow,newHigh);
    }

    @Override
    public FloatIntervalLattice glbAux(FloatIntervalLattice other){
        MathNumber newLow= this.low.max(other.low);
        MathNumber newHigh= this.high.min(other.high);
        if(!newLow.leq(newHigh))    return BOTTOM;
        return new FloatIntervalLattice(newLow,newHigh);
    }


    @Override
    public boolean lessOrEqualAux(FloatIntervalLattice other) throws SemanticException{
        return other.low.leq(this.low) && this.high.leq(other.high);
    }

    private static final List<MathNumber> THRESHOLDS = Arrays.asList(new MathNumber(-10L), new MathNumber(-1L), new MathNumber(-0.5), new MathNumber(0L),new MathNumber(0.5), new MathNumber(1L), new MathNumber(10L));
    public FloatIntervalLattice wideningAux(FloatIntervalLattice other) {
        if (this.isBottom() || other.isBottom())
            return BOTTOM;

        MathNumber u1 = this.high;
        MathNumber u2 = other.high;

        MathNumber uResult = u1;
        if (u2.gt(u1)) {
            uResult = MathNumber.PLUS_INFINITY;

            for (MathNumber t : THRESHOLDS) {
                if (!t.lt(u2)) {
                    uResult = t;
                    break;
                }
            }
        }

        MathNumber l1 = this.low;
        MathNumber l2 = other.low;

        MathNumber lResult = l1;
        if (l2.lt(l1)) {
            lResult = MathNumber.MINUS_INFINITY;

            for (int j = THRESHOLDS.size() - 1; j >= 0; j--) {
                MathNumber t = THRESHOLDS.get(j);
                if (!t.gt(l2)) {
                    lResult = t;
                    break;
                }
            }
        }

        return new FloatIntervalLattice(lResult, uResult);
    }



    @Override
    public int hashCode(){return Objects.hash(low,high);}

    @Override
    public boolean equals(Object obj){
        if(this==obj) return true;
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass())   return false;
        FloatIntervalLattice other= (FloatIntervalLattice) obj;
        return this.low.equals(other.low) && this.high.equals(other.high);
    }

    @Override
    public int compareTo(FloatIntervalLattice o) {
        if(this.isBottom()) return o.isBottom() ? 0 : -1;
        if(this.isTop()) return o.isTop() ? 0 : 1;
        if(o.isBottom()) return 1;
        if(o.isTop()) return -1;
        int cmp=this.low.compareTo(o.low);
        if (cmp != 0) return cmp;
        return this.high.compareTo(o.high);
    }

}
