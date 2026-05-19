package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.Objects;

public class ExtendedSignTaintLattice implements BaseLattice<ExtendedSignTaintLattice>{
    private final ExtendedSignLattice sign;
    private final TaintThreeLevelsLattice taint;

    public ExtendedSignTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign = sign;
        this.taint = taint;
    }

    public ExtendedSignLattice getSign(){
        return this.sign;
    }

    public TaintThreeLevelsLattice getTaint(){
        return this.taint;
    }

    @Override
    public ExtendedSignTaintLattice top() {
        return new ExtendedSignTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP);
    }

    @Override
    public boolean isTop() {
        return this.sign.isTop() && this.taint.isTop();
    }

    @Override
    public ExtendedSignTaintLattice bottom() {
        return new ExtendedSignTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);
    }

    @Override
    public boolean isBottom() {
        return this.sign.isBottom() || this.taint.isBottom();
    }

    @Override
    public StructuredRepresentation representation() {
        if(this.isBottom())
            return Lattice.bottomRepresentation();
        else if(this.isTop())
            return Lattice.topRepresentation();
        else
            return new StringRepresentation("<" + sign.representation().toString() + ", " +  taint.representation().toString() + ">");
    }


    @Override
    public ExtendedSignTaintLattice lubAux(ExtendedSignTaintLattice other) throws SemanticException {
        return new ExtendedSignTaintLattice(this.sign.lub(other.sign),  this.taint.lub(other.taint));
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignTaintLattice other) throws SemanticException {
        return this.sign.lessOrEqual(other.sign) && this.taint.lessOrEqual(other.taint);
    }

    @Override
    public ExtendedSignTaintLattice glbAux(ExtendedSignTaintLattice other) throws SemanticException {
        return new ExtendedSignTaintLattice(this.sign.glb(other.sign), this.taint.glb(other.taint));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendedSignTaintLattice that = (ExtendedSignTaintLattice) o;
        return Objects.equals(sign, that.sign) && Objects.equals(taint, that.taint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, taint);
    }
}
