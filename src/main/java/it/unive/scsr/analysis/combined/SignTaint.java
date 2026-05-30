package it.unive.scsr.analysis.combined;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.Sign;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import java.util.Objects;

public class SignTaint implements BaseNonRelationalValueDomain<SignTaint>, Lattice<SignTaint> {

    private final SignLattice sign;
    private final TaintThreeLevelsLattice taint;

    public SignTaint(SignLattice sign, TaintThreeLevelsLattice taint) {
        this.sign = sign;
        this.taint = taint;
    }

    public SignTaint() {
        this(SignLattice.TOP, TaintThreeLevelsLattice.Top);
    }

    @Override
    public SignTaint top() {
        return new SignTaint(SignLattice.TOP, TaintThreeLevelsLattice.Top);
    }

    @Override
    public SignTaint bottom() {
        return new SignTaint(SignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);
    }

    @Override
    public boolean isTop() {
        return sign == SignLattice.TOP && taint == TaintThreeLevelsLattice.Top;
    }

    @Override
    public boolean isBottom() {
        return sign == SignLattice.BOTTOM && taint == TaintThreeLevelsLattice.Bottom;
    }

    @Override
    public SignTaint unknownValue(Identifier id) {
        return top();
    }

    @Override
    public SignTaint lub(SignTaint other) throws SemanticException {
        return new SignTaint(sign.lub(other.sign), taint.lub(other.taint));
    }

    @Override
    public boolean lessOrEqual(SignTaint other) throws SemanticException {
        return sign.lessOrEqual(other.sign) && taint.lessOrEqual(other.taint);
    }

    @Override
    public StructuredRepresentation representation() {
        if (isTop()) return Lattice.topRepresentation();
        if (isBottom()) return Lattice.bottomRepresentation();
        return new StringRepresentation("(" + sign.representation() + ", " + taint.representation() + ")");
    }

    public SignLattice getSign() { return sign; }
    public TaintThreeLevelsLattice getTaint() { return taint; }

    // Убрали @Override, чтобы не ругался на несовпадение сигнатур в базовом классе
    public SignTaint eval(ValueExpression expression, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ValueEnvironment<SignLattice> signEnv = new ValueEnvironment<>(SignLattice.TOP);
        ValueEnvironment<TaintThreeLevelsLattice> taintEnv = new ValueEnvironment<>(TaintThreeLevelsLattice.Top);

        // Используем порядок, который требовал компилятор (env первым)
        SignLattice s = new Sign().eval(signEnv, expression, pp, oracle);
        TaintThreeLevelsLattice t = new TaintThreeLevels().eval(taintEnv, expression, pp, oracle);

        return new SignTaint(s, t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignTaint other = (SignTaint) o;
        return sign == other.sign && taint == other.taint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, taint);
    }
}