package it.unive.scsr.analysis.extendedinterval;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

public class ExtendedIntervalLattice
        implements BaseLattice<ExtendedIntervalLattice>, Comparable<ExtendedIntervalLattice> {

    MathNumber u;
    MathNumber l;
    Boolean bot;

    public static ExtendedIntervalLattice TOP = new ExtendedIntervalLattice(MathNumber.MINUS_INFINITY, MathNumber.PLUS_INFINITY, false);
    public static ExtendedIntervalLattice BOTTOM = new ExtendedIntervalLattice(null, null, true);
    public static ExtendedIntervalLattice ZERO = new ExtendedIntervalLattice(0, 0, false);

    public ExtendedIntervalLattice(MathNumber l, MathNumber u, Boolean isbot) {
        this.u = u;
        this.l = l;
        this.bot = isbot;
    }

    public ExtendedIntervalLattice(int l, int u, Boolean isbot) {
        this.u = new MathNumber(u);
        this.l = new MathNumber(l);
        this.bot = isbot;
    }

    public ExtendedIntervalLattice() {
        this(null, null, true);
    }


    @Override
    public ExtendedIntervalLattice top() {
        return TOP;
    }

    @Override
    public ExtendedIntervalLattice bottom() {
        return BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == BOTTOM)
            return Lattice.bottomRepresentation();

        return new StringRepresentation("[" + this.l + "," + this.u + "]");
    }

    @Override
    public ExtendedIntervalLattice lubAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.bot || other.bot)
            return BOTTOM;

        MathNumber l1 = this.l;
        MathNumber l2 = other.l;

        MathNumber lResult;
        if (l1.leq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.u;
        MathNumber u2 = other.u;

        MathNumber uResult;

        if (u1.geq(u2))
            uResult = u1;
        else
            uResult = u2;

        return new ExtendedIntervalLattice(lResult, uResult, false);
    }


    @Override
    public ExtendedIntervalLattice glbAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.bot || other.bot)
            return BOTTOM;

        MathNumber l1 = this.l;
        MathNumber l2 = other.l;

        MathNumber lResult;
        if (l1.geq(l2))
            lResult = l1;
        else
            lResult = l2;

        MathNumber u1 = this.u;
        MathNumber u2 = other.u;

        MathNumber uResult;
        if (u1.leq(u2))
            uResult = u1;
        else
            uResult = u2;

        return new ExtendedIntervalLattice(lResult, uResult, false);
    }

    @Override
    public boolean lessOrEqualAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.bot || other.bot)
            return false;
        return this.l.compareTo(other.l) <= 0 && this.u.compareTo(other.u) >= 0;
    }

    @Override
    public ExtendedIntervalLattice wideningAux(ExtendedIntervalLattice other) throws SemanticException {
        if (this.bot || other.bot)
            return BOTTOM;

        MathNumber u1 = this.u;
        MathNumber u2 = other.u;

        MathNumber uResult = u1;
        if (u2.gt(u1))
            uResult = MathNumber.PLUS_INFINITY;

        MathNumber l1 = this.l;
        MathNumber l2 = other.l;

        MathNumber lResult = l1;
        if (l2.lt(l1)) {
            lResult = MathNumber.MINUS_INFINITY;
        }

        return new ExtendedIntervalLattice(lResult, uResult, false);

    }

    @Override
    public int hashCode() {
        return Objects.hash(l,u);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExtendedIntervalLattice other = (ExtendedIntervalLattice) obj;
        return this.l.equals(other.l) && this.u.equals(other.u);
    }

    @Override
    public int compareTo(ExtendedIntervalLattice other) {
        if (isBottom())
            return other.isBottom() ? 0 : -1;
        if (isTop())
            return other.isTop() ? 0 : 1;
        if (other.isBottom())
            return 1;
        if (isTop())
            return -1;
        int cmp;
        return (cmp = this.l.compareTo(other.l)) != 0 ? cmp : this.u.compareTo(other.u);
    }
}
