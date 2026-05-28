package it.unive.scsr.analysis.sign_x_threetaint;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.Objects;

public class ExtendedSignXThreeTaintLattice implements BaseLattice<ExtendedSignXThreeTaintLattice> {
    public final ExtendedSignLattice extSign;
    public final TaintThreeLevelsLattice threeTaint;

    static public ExtendedSignXThreeTaintLattice TOP = new ExtendedSignXThreeTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP);
    static public ExtendedSignXThreeTaintLattice BOTTOM = new ExtendedSignXThreeTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);

    public ExtendedSignXThreeTaintLattice(ExtendedSignLattice ex, TaintThreeLevelsLattice ttl){
        this.extSign = ex;
        this.threeTaint = ttl;
    }

    @Override
    public ExtendedSignXThreeTaintLattice lubAux(ExtendedSignXThreeTaintLattice other) throws SemanticException {
        return new ExtendedSignXThreeTaintLattice(this.extSign.lub(other.extSign), this.threeTaint.lub(other.threeTaint));
    }

    @Override
    public ExtendedSignXThreeTaintLattice glbAux(ExtendedSignXThreeTaintLattice other) throws SemanticException {
        return BaseLattice.super.glbAux(other);
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignXThreeTaintLattice other) throws SemanticException {
        return this.extSign.lessOrEqualAux(other.extSign) && this.threeTaint.equals(other.threeTaint);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        else {
            return this.extSign.equals(((ExtendedSignXThreeTaintLattice) other).extSign) &&
                    this.threeTaint.equals(((ExtendedSignXThreeTaintLattice) other).threeTaint);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.extSign, this.threeTaint);
    }

    @Override
    public String toString() {
        return "[" + this.extSign.toString() + ", " + this.threeTaint.toString() +"]";
    }

    @Override
    public ExtendedSignXThreeTaintLattice top() {
        return new ExtendedSignXThreeTaintLattice(ExtendedSignLattice.TOP,TaintThreeLevelsLattice.TOP);
    }

    @Override
    public ExtendedSignXThreeTaintLattice bottom() {
        return new ExtendedSignXThreeTaintLattice(ExtendedSignLattice.BOTTOM,TaintThreeLevelsLattice.BOTTOM);
    }

    @Override
    public boolean isBottom() {
        return this.extSign == ExtendedSignLattice.BOTTOM || this.threeTaint == TaintThreeLevelsLattice.BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(this.extSign.representation(), this.threeTaint.representation());
    }
}
