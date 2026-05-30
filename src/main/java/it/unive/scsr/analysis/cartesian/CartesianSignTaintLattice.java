/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import static it.unive.scsr.analysis.sign.extended.ExtendedSignLattice.NEG;
import static it.unive.scsr.analysis.sign.extended.ExtendedSignLattice.NOTNEG;
import static it.unive.scsr.analysis.sign.extended.ExtendedSignLattice.NOTPOS;
import static it.unive.scsr.analysis.sign.extended.ExtendedSignLattice.NOTZERO;
import static it.unive.scsr.analysis.sign.extended.ExtendedSignLattice.POS;
import static it.unive.scsr.analysis.sign.extended.ExtendedSignLattice.ZERO;
import java.util.Objects;

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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CartesianSignTaintLattice other = (CartesianSignTaintLattice) obj;
        return this.taint == other.taint && this.sign == other.sign;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.taint);
        hash = 17 * hash + Objects.hashCode(this.sign);
        return hash;
    }

    public Satisfiability eq(CartesianSignTaintLattice other){
        if(this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if(this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        if(!this.equals(other))
            return Satisfiability.NOT_SATISFIED;
        if(this.sign == ExtendedSignLattice.ZERO)
            return Satisfiability.SATISFIED;

        return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(CartesianSignTaintLattice other){
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;

        if(this.sign == NEG){
            if(other.sign == NEG || other.sign == NOTZERO || other.sign == NOTPOS){
                return Satisfiability.UNKNOWN;
            }
            // Zero, pos, notneg
            return Satisfiability.NOT_SATISFIED;
        }
        if(this.sign == NOTPOS){
            if(other.sign == NEG
            || other.sign == NOTZERO
            || other.sign == ZERO
            || other.sign == NOTNEG
            || other.sign == NOTPOS){
                return Satisfiability.UNKNOWN;
            }
            if(other.sign == POS)
                return Satisfiability.NOT_SATISFIED;
        }

        if(this.sign == POS){
            if(other.sign == POS
            || other.sign == NOTNEG
            || other.sign == NOTZERO){
                return Satisfiability.UNKNOWN;
            }
            // Zero, notpos, neg
            return Satisfiability.SATISFIED;
        }
        if(this.sign == NOTNEG){
            if(other.sign == ZERO || other.sign == NOTZERO || other.sign == POS || other.sign == NOTPOS){
                return Satisfiability.UNKNOWN;
            }
            return Satisfiability.SATISFIED;
        }

        if(this.sign == ZERO){
            if(other.sign == ZERO || other.sign == NOTNEG || other.sign == POS){
                return Satisfiability.NOT_SATISFIED;
            }
            if(other.sign == NOTZERO || other.sign == NOTPOS){
                return Satisfiability.UNKNOWN;
            }

            // Neg
            return Satisfiability.SATISFIED;
        }

        // Notzero
        return Satisfiability.UNKNOWN;
    }
}
