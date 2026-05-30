package it.unive.scsr.analysis.signxthreetaint;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.Objects;

public class SignXThreeTaintLattice implements BaseLattice<SignXThreeTaintLattice> {

    private final ExtendedSignLattice sign;
    private final TaintThreeLevelsLattice taint;

    public static final SignXThreeTaintLattice TOP =
            new SignXThreeTaintLattice(
                    ExtendedSignLattice.TOP,
                    TaintThreeLevelsLattice.TOP
            );

    public static final SignXThreeTaintLattice BOTTOM =
            new SignXThreeTaintLattice(
                    ExtendedSignLattice.BOTTOM,
                    TaintThreeLevelsLattice.BOTTOM
            );

    public SignXThreeTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign = sign;
        this.taint = taint;
    }

    public ExtendedSignLattice getSign() {
        return sign;
    }

    public TaintThreeLevelsLattice getTaint() {
        return taint;
    }

    @Override
    public SignXThreeTaintLattice top() {
        return TOP;
    }

    @Override
    public SignXThreeTaintLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == TOP)
            return Lattice.topRepresentation();
        if (this == BOTTOM)
            return Lattice.bottomRepresentation();
        return new StringRepresentation("(" + sign.representation() + ", " + taint.representation() + ")");
    }

    @Override
    public SignXThreeTaintLattice lubAux(SignXThreeTaintLattice other) throws SemanticException {
        return new SignXThreeTaintLattice(
                this.sign.lub(other.sign),
                this.taint.lub(other.taint)
        );
    }

    @Override
    public SignXThreeTaintLattice glbAux(SignXThreeTaintLattice other) throws SemanticException {
        return new SignXThreeTaintLattice(
                this.sign.glb(other.sign),
                this.taint.glb(other.taint)
        );
    }

    @Override
    public boolean lessOrEqualAux(SignXThreeTaintLattice other) throws SemanticException{
        return this.sign.lessOrEqual(other.sign)
                && this.taint.lessOrEqual(other.taint);
    }

    @Override
    public SignXThreeTaintLattice wideningAux(
            SignXThreeTaintLattice other)
            throws SemanticException {

        return new SignXThreeTaintLattice(
                this.sign.widening(other.sign),
                this.taint.widening(other.taint)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, taint);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        SignXThreeTaintLattice other =
                (SignXThreeTaintLattice) obj;

        return Objects.equals(sign, other.sign)
                && Objects.equals(taint, other.taint);
    }


    @Override
    public String toString() {
        return representation().toString();
    }
}