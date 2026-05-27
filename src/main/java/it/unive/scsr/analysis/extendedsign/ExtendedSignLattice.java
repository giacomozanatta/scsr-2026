package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
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
    public boolean lessOrEqualAux(ExtendedSignLattice other) throws SemanticException {
        if (this == ExtendedSignLattice.ZERO){
            return other == ExtendedSignLattice.ZERO ||
                    other == ExtendedSignLattice.ZEROPOS ||
                    other == ExtendedSignLattice.ZERONEG;
        }
        else if (this == ExtendedSignLattice.NONZEROPOS) {
            return other == ExtendedSignLattice.NONZERO ||
                    other == ExtendedSignLattice.NONZEROPOS ||
                    other == ExtendedSignLattice.ZEROPOS;
        }
        else if (this == ExtendedSignLattice.NONZERONEG) {
            return other == ExtendedSignLattice.NONZERONEG ||
                    other == ExtendedSignLattice.ZERONEG ||
                    other == ExtendedSignLattice.NONZERO;
        }
        else if (this == ExtendedSignLattice.ZEROPOS) {
            return this == other;
        }
        else if (this == ExtendedSignLattice.ZERONEG) {
            return this == other;
        }
        return false;
    }

    public ExtendedSignLattice appendZero() {
        if (this == ExtendedSignLattice.TOP ||
            this == ExtendedSignLattice.BOTTOM ||
            this == ExtendedSignLattice.ZERONEG ||
            this == ExtendedSignLattice.ZEROPOS ||
            this == ExtendedSignLattice.ZERO) {
            return this;
        }
        else if (this == ExtendedSignLattice.NONZERO) {
            return ExtendedSignLattice.TOP;
        }
        else if (this == ExtendedSignLattice.NONZEROPOS) {
            return ExtendedSignLattice.ZEROPOS;
        }
        else if (this == ExtendedSignLattice.NONZERONEG) {
            return ExtendedSignLattice.ZERONEG;
        }
        return ExtendedSignLattice.TOP;
    }

    public ExtendedSignLattice negate() {
        if (this == ExtendedSignLattice.TOP ||
            this == ExtendedSignLattice.BOTTOM ||
            this == ExtendedSignLattice.NONZERO ||
            this == ExtendedSignLattice.ZERO) {
            return this;
        }
        else if (this == ExtendedSignLattice.ZEROPOS) {
            return ExtendedSignLattice.ZERONEG;
        }
        else if (this == ExtendedSignLattice.NONZEROPOS) {
            return ExtendedSignLattice.NONZERONEG;
        }
        else if (this == ExtendedSignLattice.ZERONEG) {
            return ExtendedSignLattice.ZEROPOS;
        }
        else if (this == ExtendedSignLattice.NONZERONEG) {
            return ExtendedSignLattice.NONZEROPOS;
        }
        return ExtendedSignLattice.TOP;
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
        if (this == ExtendedSignLattice.BOTTOM)
            return Lattice.bottomRepresentation();
        else if (this == ExtendedSignLattice.TOP)
            return Lattice.topRepresentation();
        else if (this == ExtendedSignLattice.ZERO)
            return new StringRepresentation("0");
        else if (this == ExtendedSignLattice.NONZERO)
            return new StringRepresentation("!=0");
        else if (this == ExtendedSignLattice.NONZEROPOS)
            return new StringRepresentation(">0");
        else if (this == ExtendedSignLattice.NONZERONEG)
            return new StringRepresentation("<0");
        else if (this == ExtendedSignLattice.ZEROPOS)
            return new StringRepresentation(">=0");
        return new StringRepresentation("<=0");
    }

    public Satisfiability eq(ExtendedSignLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        else if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        else if (!this.equals(other))
            return Satisfiability.NOT_SATISFIED;
        else if (this == ExtendedSignLattice.ZERO &&
                 other == ExtendedSignLattice.ZERO) {
            return Satisfiability.SATISFIED;
        }
        else
            return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(ExtendedSignLattice other){
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        else if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        else if (this == ExtendedSignLattice.ZERO) {
            if (other == ExtendedSignLattice.NONZEROPOS) {
                return Satisfiability.NOT_SATISFIED;
            }
            else if (other == ExtendedSignLattice.NONZERONEG) {
                return Satisfiability.SATISFIED;
            }
            else {
                return Satisfiability.UNKNOWN;
            }
        }
        else if (this == ExtendedSignLattice.NONZEROPOS) {
            if (other == ExtendedSignLattice.ZERO ||
                other == ExtendedSignLattice.ZERONEG ||
                other == ExtendedSignLattice.NONZERONEG) {
                return Satisfiability.SATISFIED;
            }
            return Satisfiability.UNKNOWN;
        }
        else if (this == ExtendedSignLattice.NONZERONEG) {
            if (other == ExtendedSignLattice.ZERO ||
                other == ExtendedSignLattice.ZEROPOS ||
                other == ExtendedSignLattice.NONZEROPOS) {
                return Satisfiability.NOT_SATISFIED;
            }
            return Satisfiability.UNKNOWN;
        }
        else if (this == ExtendedSignLattice.ZEROPOS) {
            if (other == ExtendedSignLattice.NONZERONEG) {
                return Satisfiability.SATISFIED;
            }
            return Satisfiability.UNKNOWN;
        }
        else if (this == ExtendedSignLattice.ZERONEG) {
            if (other == ExtendedSignLattice.NONZEROPOS) {
                return Satisfiability.NOT_SATISFIED;
            }
            return Satisfiability.UNKNOWN;
        }
        else {
            return Satisfiability.UNKNOWN;
        }
    }
}
