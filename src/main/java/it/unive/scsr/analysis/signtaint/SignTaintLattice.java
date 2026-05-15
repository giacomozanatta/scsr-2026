package it.unive.scsr.analysis.signtaint;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignTaintLattice implements BaseLattice<SignTaintLattice> {
    private final ExtendedSignLattice sign;
    private final TaintThreeLevelsLattice taint;

    private SignTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign = sign;
        this.taint = taint;
    }

    @Override
    public SignTaintLattice top() {
        return new SignTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP);
    }

    @Override
    public SignTaintLattice bottom() {
        return new SignTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);
    }

    @Override
    public SignTaintLattice lubAux(SignTaintLattice other) throws SemanticException {
        return new SignTaintLattice(this.sign.lub(other.sign), this.taint.lub(other.taint));
    }

    @Override
    public boolean lessOrEqualAux(SignTaintLattice other) throws SemanticException {
        return this.sign.lessOrEqual(other.sign) && this.taint.lessOrEqual(other.taint);
    }

    @Override
    public StructuredRepresentation representation() {
        return new StringRepresentation("(" + sign.representation() + ", " + taint.representation() + ")");
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, taint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SignTaintLattice other = (SignTaintLattice) obj;
        return this.sign.equals(other.sign)
                && this.taint.equals(other.taint);
    }

}
