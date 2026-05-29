package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignTaintLattice implements BaseLattice<SignTaintLattice> {

    static final SignTaintLattice TOP    = new SignTaintLattice(ExtendedSignLattice.TOP,    TaintThreeLevelsLattice.TOP);
    static final SignTaintLattice BOTTOM = new SignTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);

    final ExtendedSignLattice      sign;
    final TaintThreeLevelsLattice  taint;

    public SignTaintLattice() {
        this(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP);
    }

    SignTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign  = sign;
        this.taint = taint;
    }

    @Override public SignTaintLattice top()    { return TOP; }
    @Override public SignTaintLattice bottom() { return BOTTOM; }

    @Override
    public SignTaintLattice lubAux(SignTaintLattice other) throws SemanticException {
        return new SignTaintLattice(sign.lub(other.sign), taint.lub(other.taint));
    }

    @Override
    public SignTaintLattice glbAux(SignTaintLattice other) throws SemanticException {
        return new SignTaintLattice(sign.glb(other.sign), taint.glb(other.taint));
    }

    @Override
    public boolean lessOrEqualAux(SignTaintLattice other) throws SemanticException {
        return sign.lessOrEqual(other.sign) && taint.lessOrEqual(other.taint);
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom()) return Lattice.bottomRepresentation();
        if (isTop())    return Lattice.topRepresentation();
        return new StringRepresentation(
            "(" + sign.representation() + ", " + taint.representation() + ")");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SignTaintLattice)) return false;
        SignTaintLattice o = (SignTaintLattice) obj;
        return sign == o.sign && taint.equals(o.taint);
    }

    @Override
    public int hashCode() {
        return 31 * sign.hashCode() + taint.hashCode();
    }
}
