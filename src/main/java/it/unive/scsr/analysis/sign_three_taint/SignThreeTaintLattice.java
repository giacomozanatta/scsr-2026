package it.unive.scsr.analysis.sign_three_taint;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

/**
 * Cartesian product of SignLattice x TaintThreeLevelsLattice.
 *
 * Lattice order is pointwise: (s1,t1) ⊑ (s2,t2)  iff  s1 ⊑ s2  ∧  t1 ⊑ t2
 *
 * All lattice operations (lub, glb, widening) are computed component-wise.
 */
public class SignThreeTaintLattice implements BaseLattice<SignThreeTaintLattice> {
    private final SignLattice sign;
    private final TaintThreeLevelsLattice taint;

    public static final SignThreeTaintLattice TOP = new SignThreeTaintLattice(SignLattice.TOP, TaintThreeLevelsLattice.TOP);
    public static final SignThreeTaintLattice BOTTOM = new SignThreeTaintLattice(SignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);

    public SignThreeTaintLattice(SignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign = sign;
        this.taint = taint;
    }

    public SignLattice getSign() { 
        return sign;
    }

    public TaintThreeLevelsLattice getTaint() { 
        return taint; 
    }

    @Override
    public SignThreeTaintLattice top() { 
        return TOP;    
    }

    @Override
    public SignThreeTaintLattice bottom() { 
        return BOTTOM; 
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, taint);
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom()) {
            return Lattice.bottomRepresentation();
        }

        if (isTop()) {
            return Lattice.topRepresentation();
        }

        return new StringRepresentation(
            "(" + getSign().representation() + ", " + getTaint().representation() + ")"
        );
    }

    @Override
    public String toString() {
        return representation().toString();
    }

    @Override
    public SignThreeTaintLattice lubAux(SignThreeTaintLattice other) throws SemanticException {
        return new SignThreeTaintLattice(
            getSign().lub(other.getSign()), getTaint().lub(other.getTaint())
        );
    }

    @Override
    public SignThreeTaintLattice glbAux(SignThreeTaintLattice other) throws SemanticException {
        return new SignThreeTaintLattice(
            getSign().glb(other.getSign()), getTaint().glb(other.getTaint())
        );
    }

    @Override
    public boolean lessOrEqualAux(SignThreeTaintLattice other) throws SemanticException {
        return (
            getSign().lessOrEqual(other.getSign()) && 
            getTaint().lessOrEqual(other.getTaint())
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        SignThreeTaintLattice other = (SignThreeTaintLattice) obj;

        return (
            Objects.equals(getSign(), other.getSign()) && 
            Objects.equals(getTaint(), other.getTaint())
        );
    }

}
