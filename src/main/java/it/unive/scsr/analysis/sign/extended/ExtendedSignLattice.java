package it.unive.scsr.analysis.sign.extended;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    private final boolean includesNeg;
    private final boolean includesZero;
    private final boolean includesPos;

    public static final ExtendedSignLattice BOTTOM  = new ExtendedSignLattice(false, false, false);
    public static final ExtendedSignLattice LT_ZERO = new ExtendedSignLattice(true,  false, false);
    public static final ExtendedSignLattice EQ_ZERO = new ExtendedSignLattice(false, true,  false);
    public static final ExtendedSignLattice GT_ZERO = new ExtendedSignLattice(false, false, true);
    public static final ExtendedSignLattice LE_ZERO = new ExtendedSignLattice(true,  true,  false);
    public static final ExtendedSignLattice NE_ZERO = new ExtendedSignLattice(true,  false, true);
    public static final ExtendedSignLattice GE_ZERO = new ExtendedSignLattice(false, true,  true);
    public static final ExtendedSignLattice TOP     = new ExtendedSignLattice(true,  true,  true);

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

    private static ExtendedSignLattice fromFlags(boolean neg, boolean zero, boolean pos) {
        if (!neg && !zero && !pos) return BOTTOM;
        if ( neg && !zero && !pos) return LT_ZERO;
        if (!neg &&  zero && !pos) return EQ_ZERO;
        if (!neg && !zero &&  pos) return GT_ZERO;
        if ( neg &&  zero && !pos) return LE_ZERO;
        if ( neg && !zero &&  pos) return NE_ZERO;
        if (!neg &&  zero &&  pos) return GE_ZERO;
        return TOP;
    }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        return fromFlags(
            this.includesNeg  || other.includesNeg,
            this.includesZero || other.includesZero,
            this.includesPos  || other.includesPos
        );
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        // TODO
        return false;
    }

    @Override
    public StructuredRepresentation representation() {
        // TODO
        return null;
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
