package it.unive.scsr.analysis.sign.extended;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    private final boolean includesNeg;
    private final boolean includesZero;
    private final boolean includesPos;

    public static final ExtendedSignLattice BOTTOM = new ExtendedSignLattice(false, false, false);
    public static final ExtendedSignLattice LT_ZERO = new ExtendedSignLattice(true, false, false);
    public static final ExtendedSignLattice EQ_ZERO = new ExtendedSignLattice(false, true, false);
    public static final ExtendedSignLattice GT_ZERO = new ExtendedSignLattice(false, false, true);
    public static final ExtendedSignLattice LE_ZERO = new ExtendedSignLattice(true, true, false);
    public static final ExtendedSignLattice NE_ZERO = new ExtendedSignLattice(true, false, true);
    public static final ExtendedSignLattice GE_ZERO = new ExtendedSignLattice(false, true, true);
    public static final ExtendedSignLattice TOP = new ExtendedSignLattice(true, true, true);

    private ExtendedSignLattice(boolean includesNeg, boolean includesZero, boolean includesPos) {
        this.includesNeg = includesNeg;
        this.includesZero = includesZero;
        this.includesPos = includesPos;
    }

    @Override
    public ExtendedSignLattice top() {
        return TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return BOTTOM;
    }

    ExtendedSignLattice negate() {
        return fromFlags(includesPos, includesZero, includesNeg);
    }

    ExtendedSignLattice add(ExtendedSignLattice other) {
        boolean resNeg = this.includesNeg || other.includesNeg;
        boolean resPos = this.includesPos || other.includesPos;
        boolean resZero = (this.includesZero && other.includesZero)
                || (this.includesPos && other.includesNeg)
                || (this.includesNeg && other.includesPos);

        return fromFlags(resNeg, resZero, resPos);
    }

    ExtendedSignLattice mul(ExtendedSignLattice other) {
        boolean resNeg = (this.includesNeg && other.includesPos)
                || (this.includesPos && other.includesNeg);
        boolean resZero = this.includesZero || other.includesZero;
        boolean resPos = (this.includesPos && other.includesPos)
                || (this.includesNeg && other.includesNeg);

        return fromFlags(resNeg, resZero, resPos);
    }

    ExtendedSignLattice div(ExtendedSignLattice other) {
        if (!other.includesPos && !other.includesNeg) return BOTTOM;

        boolean resNeg = (this.includesNeg && other.includesPos)
                || (this.includesPos && other.includesNeg);
        boolean resZero = this.includesZero && (other.includesPos || other.includesNeg);
        boolean resPos = (this.includesPos && other.includesPos)
                || (this.includesNeg && other.includesNeg);

        return fromFlags(resNeg, resZero, resPos);
    }

    ExtendedSignLattice mod(ExtendedSignLattice other) {
        if (!other.includesPos && !other.includesNeg) return BOTTOM;

        boolean resNeg = other.includesNeg;
        boolean resZero = true;
        boolean resPos = other.includesPos;

        return fromFlags(resNeg, resZero, resPos);
    }

    ExtendedSignLattice rem(ExtendedSignLattice other) {
        if (!other.includesPos && !other.includesNeg) return BOTTOM;

        boolean resNeg = this.includesNeg;
        boolean resZero = true;
        boolean resPos = this.includesPos;

        return fromFlags(resNeg, resZero, resPos);
    }

    private static ExtendedSignLattice fromFlags(boolean neg, boolean zero, boolean pos) {
        if (!neg && !zero && !pos) return BOTTOM;
        if (neg && !zero && !pos) return LT_ZERO;
        if (!neg && zero && !pos) return EQ_ZERO;
        if (!neg && !zero && pos) return GT_ZERO;
        if (neg && zero && !pos) return LE_ZERO;
        if (neg && !zero && pos) return NE_ZERO;
        if (!neg && zero && pos) return GE_ZERO;
        return TOP;
    }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        return fromFlags(
                this.includesNeg || other.includesNeg,
                this.includesZero || other.includesZero,
                this.includesPos || other.includesPos
        );
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this.includesNeg && !other.includesNeg) return false;
        if (this.includesZero && !other.includesZero) return false;
        if (this.includesPos && !other.includesPos) return false;
        return true;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM) return Lattice.bottomRepresentation();
        if (this == TOP) return Lattice.topRepresentation();
        if (this == LT_ZERO) return new StringRepresentation("<0");
        if (this == EQ_ZERO) return new StringRepresentation("=0");
        if (this == GT_ZERO) return new StringRepresentation(">0");
        if (this == LE_ZERO) return new StringRepresentation("<=0");
        if (this == NE_ZERO) return new StringRepresentation("!=0");
        return new StringRepresentation(">=0");
    }

    @Override
    public int hashCode() {
        return Objects.hash(includesNeg, includesZero, includesPos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ExtendedSignLattice other = (ExtendedSignLattice) obj;
        return this.includesNeg == other.includesNeg
                && this.includesZero == other.includesZero
                && this.includesPos == other.includesPos;
    }

}
