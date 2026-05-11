package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StructuredRepresentation;

public class ExtendedSignLattice implements BaseLattice<ExtendedSignLattice> {

    public enum ExtendedSignValues {
        TOP,
        ZeroNegative,
        NonZero,
        ZeroPositive,
        NonZeroNegative,
        Zero,
        NonZeroPositive,
        BOTTOM
    }

    private final ExtendedSignValues element;

    public static ExtendedSignLattice TOP = new ExtendedSignLattice(ExtendedSignValues.TOP);
    public static ExtendedSignLattice ZERONEG = new ExtendedSignLattice(ExtendedSignValues.ZeroNegative);
    public static ExtendedSignLattice NONZERO = new ExtendedSignLattice(ExtendedSignValues.NonZero);
    public static ExtendedSignLattice ZEROPOS = new ExtendedSignLattice(ExtendedSignValues.ZeroPositive);
    public static ExtendedSignLattice NONZERONEG = new ExtendedSignLattice(ExtendedSignValues.NonZeroNegative);
    public static ExtendedSignLattice ZERO = new ExtendedSignLattice(ExtendedSignValues.Zero);
    public static ExtendedSignLattice NONZEROPOS = new ExtendedSignLattice(ExtendedSignValues.NonZeroPositive);
    public static ExtendedSignLattice BOTTOM = new ExtendedSignLattice(ExtendedSignValues.BOTTOM);

    public ExtendedSignLattice(ExtendedSignValues ev){
        element = ev;
    }

    @Override
    public ExtendedSignLattice lubAux(ExtendedSignLattice other) throws SemanticException {
        ExtendedSignValues ev_l = this.element;
        ExtendedSignValues ev_r = other.element;
        if (ev_l == ExtendedSignValues.BOTTOM) {
            return other;
        } else if (ev_r == ExtendedSignValues.BOTTOM) {
            return this;
        } else if (ev_l == ev_r) {
            return this;
        }

        if (ev_l == ExtendedSignValues.NonZeroNegative){
            if (ev_r == ExtendedSignValues.ZeroNegative){
                return ExtendedSignLattice.ZERONEG;
            } else if (ev_r == ExtendedSignValues.NonZero) {
                return ExtendedSignLattice.NONZERO;
            } else if (ev_r == ExtendedSignValues.ZeroPositive) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.Zero) {
                return ExtendedSignLattice.ZERONEG;
            } else if (ev_r == ExtendedSignValues.NonZeroPositive) {
                return ExtendedSignLattice.NONZERO;
            }
        } else if (ev_l == ExtendedSignValues.Zero) {
            if (ev_r == ExtendedSignValues.NonZeroNegative) {
                return ExtendedSignLattice.ZERONEG;
            } else if (ev_r == ExtendedSignValues.ZeroNegative){
                return ExtendedSignLattice.ZERONEG;
            } else if (ev_r == ExtendedSignValues.NonZero) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.ZeroPositive) {
                return ExtendedSignLattice.ZEROPOS;
            } else if (ev_r == ExtendedSignValues.NonZeroPositive) {
                return ExtendedSignLattice.ZEROPOS;
            }
        } else if (ev_l == ExtendedSignValues.NonZeroPositive){
            if (ev_r == ExtendedSignValues.NonZeroNegative) {
                return ExtendedSignLattice.NONZERO;
            } else if (ev_r == ExtendedSignValues.ZeroNegative){
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.NonZero) {
                return ExtendedSignLattice.NONZERO;
            } else if (ev_r == ExtendedSignValues.ZeroPositive) {
                return ExtendedSignLattice.ZEROPOS;
            } else if (ev_r == ExtendedSignValues.Zero) {
                return ExtendedSignLattice.ZEROPOS;
            }
        } else if (ev_l == ExtendedSignValues.ZeroNegative){
            if (ev_r == ExtendedSignValues.NonZeroNegative){
                return ExtendedSignLattice.ZERONEG;
            } else if (ev_r == ExtendedSignValues.NonZero) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.ZeroPositive) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.Zero) {
                return ExtendedSignLattice.ZERONEG;
            } else if (ev_r == ExtendedSignValues.NonZeroPositive) {
                return ExtendedSignLattice.TOP;
            }
        } else if (ev_l == ExtendedSignValues.NonZero) {
            if (ev_r == ExtendedSignValues.NonZeroNegative) {
                return ExtendedSignLattice.NONZERO;
            } else if (ev_r == ExtendedSignValues.ZeroNegative){
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.Zero) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.ZeroPositive) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.NonZeroPositive) {
                return ExtendedSignLattice.NONZERO;
            }
        } else if (ev_l == ExtendedSignValues.ZeroPositive){
            if (ev_r == ExtendedSignValues.NonZeroNegative) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.ZeroNegative){
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.NonZero) {
                return ExtendedSignLattice.TOP;
            } else if (ev_r == ExtendedSignValues.NonZeroPositive) {
                return ExtendedSignLattice.ZEROPOS;
            } else if (ev_r == ExtendedSignValues.Zero) {
                return ExtendedSignLattice.ZEROPOS;
            }
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignLattice extendedSignLattice) throws SemanticException {
        return false;
    }

    @Override
    public ExtendedSignLattice top() {
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return ExtendedSignLattice.BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        return null;
    }
}
