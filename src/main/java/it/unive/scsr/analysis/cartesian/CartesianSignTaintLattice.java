/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class CartesianSignTaintLattice implements BaseLattice<CartesianSignTaintLattice> {

    final TaintThreeLevelsLattice taint;
    final ExtendedSignLattice sign;
    
    static final CartesianSignTaintLattice TOP = new CartesianSignTaintLattice(TaintThreeLevelsLattice.TOP, 
            ExtendedSignLattice.TOP);
    static final CartesianSignTaintLattice BOTTOM = new CartesianSignTaintLattice(TaintThreeLevelsLattice.BOT, 
            ExtendedSignLattice.BOTTOM);
    
    public CartesianSignTaintLattice(TaintThreeLevelsLattice taint, ExtendedSignLattice sign)
    {
        this.taint = taint;
        this.sign = sign;
    }
    
    @Override
    public boolean lessOrEqual(CartesianSignTaintLattice other) throws SemanticException {
        return this.taint.lessOrEqual(other.taint) && this.sign.lessOrEqual(other.sign);
    }

    @Override
    public CartesianSignTaintLattice lubAux(CartesianSignTaintLattice l) throws SemanticException {
        return new CartesianSignTaintLattice(taint.lubAux(l.taint), sign.lubAux(l.sign));
    }

    @Override
    public boolean lessOrEqualAux(CartesianSignTaintLattice l) throws SemanticException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CartesianSignTaintLattice top() {
        return new CartesianSignTaintLattice(taint.top(), sign.top());
    }

    @Override
    public CartesianSignTaintLattice bottom() {
        return new CartesianSignTaintLattice(taint.bottom(), sign.bottom());
    }

    @Override
    public StructuredRepresentation representation() {
        if(isBottom()) return Lattice.bottomRepresentation();
        if(isTop()) return Lattice.topRepresentation();
        
        StringRepresentation signR = (StringRepresentation)this.sign.representation();
        StringRepresentation taintR = (StringRepresentation)this.taint.representation();
        return new StringRepresentation("(" + signR + ", " + taintR + ")");
    }
    
}
