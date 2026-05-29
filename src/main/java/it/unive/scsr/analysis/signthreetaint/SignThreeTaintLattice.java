package it.unive.scsr.analysis.signthreetaint;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.ListRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignThreeTaintLattice implements BaseLattice<SignThreeTaintLattice> {

    // This domain was manually implemented as the cartesian product of ExtendedSignLattice and TaintThreeLevelsLattice, 
    // so it is a non-relational domain that tracks both the sign and the taint of a variable, but it does not track any relation between them.
    public static final SignThreeTaintLattice BOTTOM = new SignThreeTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);
    public static final SignThreeTaintLattice TOP = new SignThreeTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP);

    public final ExtendedSignLattice first;
    public final TaintThreeLevelsLattice second;

    public SignThreeTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
        if (sign == ExtendedSignLattice.BOTTOM || taint == TaintThreeLevelsLattice.BOTTOM) {
            this.first = ExtendedSignLattice.BOTTOM;
            this.second = TaintThreeLevelsLattice.BOTTOM;
        }
        else {this.first = sign; this.second = taint;}
    }

    @Override
    public SignThreeTaintLattice top() { return TOP; }

    @Override
    public SignThreeTaintLattice bottom() { return BOTTOM; }

    @Override
    public SignThreeTaintLattice lubAux(SignThreeTaintLattice other) throws SemanticException {
        return new SignThreeTaintLattice(first.lub(other.first), second.lub(other.second));
    }

    @Override
    public SignThreeTaintLattice glbAux(SignThreeTaintLattice other) throws SemanticException {
        return new SignThreeTaintLattice(first.glb(other.first), second.glb(other.second));
    }

    @Override
    public SignThreeTaintLattice wideningAux(SignThreeTaintLattice other) throws SemanticException {
        return new SignThreeTaintLattice(first.widening(other.first), second.widening(other.second));
    }

    @Override
    public boolean lessOrEqualAux(SignThreeTaintLattice other) throws SemanticException {
        return first.lessOrEqual(other.first) && second.lessOrEqual(other.second);
    }

    @Override
    public StructuredRepresentation representation() {
        return new ListRepresentation(first.representation(), second.representation());
    }

    public boolean isAlwaysTainted() { return second.isAlwaysTainted(); }
    public boolean isPossiblyTainted() { return second.isPossiblyTainted(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SignThreeTaintLattice)) return false;
        SignThreeTaintLattice other = (SignThreeTaintLattice) obj;
        return first.equals(other.first) && second.equals(other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}