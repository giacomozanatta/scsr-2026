/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unive.scsr.analysis;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/**
 *
 * @author brauny
 */
public class IntervalLattice implements BaseLattice<IntervalLattice> {
    public static IntervalLattice TOP = new IntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY);
    public static IntervalLattice BOTTOM = new IntervalLattice(null);
    
    public static IntervalLattice ZERO = new IntervalLattice(0, 0);
    
    
    IntInterval i;
    
    public IntervalLattice(IntInterval i)
    {
        this.i = i;
    }
    
    public IntervalLattice(int l, int u)
    {
        this.i = new IntInterval(l, u);
    }
    
    public IntervalLattice(MathNumber l, MathNumber u)
    {
        this.i = new IntInterval(l, u);
    }
    
    @Override
    public IntervalLattice lubAux(IntervalLattice o) {
        if(this.i == null || o.i == null)
            return BOTTOM;
        
        MathNumber l1 = this.i.getLow();
        MathNumber l2 = o.i.getLow();
        
        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = o.i.getHigh();
        
        MathNumber lResult;
        if(l1.leq(l2))
        {
            lResult = l1;
        }else
        {
            lResult = l2;
        }
        
        MathNumber uResult;
        if(u1.leq(u2))
        {
            uResult = u2;
        }else
        {
            uResult = u1;
        }
        return new IntervalLattice(lResult, uResult);
    }

    @Override
    public IntervalLattice glbAux(IntervalLattice o)
    {
        MathNumber l1 = this.i.getLow();
        MathNumber l2 = o.i.getLow();
        
        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = o.i.getHigh();
        
        MathNumber lResult;
        if(l1.geq(l2))
            lResult = l1;
        else
            lResult = l2;
        
        MathNumber uResult;
        if(u1.geq(u2))
            uResult = u2;
        else
            uResult = u1;
        return new IntervalLattice(lResult, uResult);
    }
    
    @Override
    public IntervalLattice wideningAux(IntervalLattice o)
    {
        MathNumber u1 = this.i.getHigh();
        MathNumber u2 = o.i.getHigh();
        
        MathNumber uResult = u1;
        if(u1.geq(u2))
            uResult = MathNumber.PLUS_INFINITY;
        
        MathNumber l1 = this.i.getLow();
        MathNumber l2 = o.i.getLow();
        
        MathNumber lResult = l1;
        if(l1.leq(l2))
            lResult = MathNumber.MINUS_INFINITY;
        
        return new IntervalLattice(lResult, uResult);
    }
    
    @Override
    public boolean lessOrEqualAux(IntervalLattice o){
        return this.i.includes(o.i);
    }

    @Override
    public IntervalLattice top() {
        return TOP;
    }

    @Override
    public IntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if(this == BOTTOM)
            return Lattice.bottomRepresentation();
        var l = this.i.getLow();
        var u = this.i.getHigh();
        
        return new StringRepresentation("[+"+l+","+u+"]");
    }
}
