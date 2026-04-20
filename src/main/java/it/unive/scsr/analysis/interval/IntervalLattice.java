package it.unive.scsr.analysis.interval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class IntervalLattice implements BaseLattice<IntervalLattice>, Comparable<IntervalLattice> {

    IntInterval i;

    public static IntervalLattice TOP = new IntervalLattice(MathNumber.MINUS_INFINITY,MathNumber.PLUS_INFINITY);
    public static IntervalLattice BOTTOM = new IntervalLattice(null);
    public static IntervalLattice ZERO = new IntervalLattice(0,0);

    public IntervalLattice(IntInterval i){
        this.i = i;
    }

    public IntervalLattice(MathNumber l, MathNumber u){
        this.i = new IntInterval(l,u);
    }

    public IntervalLattice(int l, int u){
        this.i = new IntInterval(l,u);
    }

    public IntervalLattice(){
        this(IntInterval.INFINITY);
    }

    @Override
    public IntervalLattice top() {
        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice bottom() {
        return IntervalLattice.BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if(this.isBottom())
            return Lattice.bottomRepresentation();
        MathNumber l = this.i.getLow();
        MathNumber u = this.i.getHigh();

        return new StringRepresentation("["+l+","+u+"]");
    }

    @Override
    public IntervalLattice lubAux(IntervalLattice other) throws SemanticException {
        if( this.i == null || other.i == null)
            return IntervalLattice.BOTTOM;

        MathNumber l1 = this.i.getLow();
        MathNumber l2 = other.i.getLow();
        MathNumber lResult;

        if(l1.leq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = other.i.getHigh();
        MathNumber uResult;

        if(u1.geq(u2))
            uResult  = u1;
        else
            uResult = u2;

        return new IntervalLattice(lResult,uResult);
    }

    @Override
    public IntervalLattice glbAux(IntervalLattice other) throws SemanticException{
        if( this.i == null || other.i == null)
            return IntervalLattice.BOTTOM;

        MathNumber l1 = this.i.getLow();
        MathNumber l2 = other.i.getLow();
        MathNumber lResult;

        if(l1.geq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = other.i.getHigh();
        MathNumber uResult;

        if(u1.leq(u2))
            uResult  = u1;
        else
            uResult = u2;

        return new IntervalLattice(lResult,uResult);
    }

    @Override
    public boolean lessOrEqualAux(IntervalLattice other) throws SemanticException {
        if(this.i == null || other.i == null)
            return false;
        return this.i.includes(other.i);
    }

    @Override
    public IntervalLattice wideningAux(IntervalLattice other) throws SemanticException {
        if( this.i == null || other.i == null)
            return IntervalLattice.BOTTOM;

        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = other.i.getHigh();
        MathNumber uResult = u1;

        if(u2.geq(u1))
            uResult = MathNumber.PLUS_INFINITY;

        MathNumber l1 = this.i.getLow();
        MathNumber l2 = other.i.getLow();
        MathNumber lResult = l1;

        if (l2.leq(l1))
            lResult = MathNumber.MINUS_INFINITY;

        return new IntervalLattice(lResult,uResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash((this.i));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj == null || (this.getClass()!=obj.getClass()))
            return false;
        IntervalLattice other = (IntervalLattice) obj;
        return Objects.equals(this.i,other.i);
    }

    @Override
    public boolean isBottom(){
        return this.equals(IntervalLattice.BOTTOM);
    }

    @Override
    public boolean isTop(){
        return this.equals(IntervalLattice.TOP);
    }

    public boolean isZero(){
        return this.equals(IntervalLattice.ZERO);
    }

    @Override
    public int compareTo(IntervalLattice o) {
        if (this.isBottom())
            return o.isBottom() ? 0 : -1;
        if (this.isTop())
            return o.isTop() ? 0 : 1;

        if (o.isBottom())
            return 1;

        if (o.isTop())
            return -1;

        return i.compareTo(o.i);
    }
}
