package it.unive.scsr.analysis.signxthreetaint;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignXThreeTaintLattice implements BaseLattice<SignXThreeTaintLattice> {

    private final ExtendedSignLattice sign;
    private final TaintThreeLevelsLattice taint;

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
        return new SignXThreeTaintLattice(
                ExtendedSignLattice.TOP,
                TaintThreeLevelsLattice.TOP
        );
    }

    @Override
    public SignXThreeTaintLattice bottom() {
        return new SignXThreeTaintLattice(
                ExtendedSignLattice.BOTTOM,
                TaintThreeLevelsLattice.BOTTOM
        );
    }

    @Override
    public SignXThreeTaintLattice lubAux(SignXThreeTaintLattice other) {
        return new SignXThreeTaintLattice(
                this.sign.lub(other.sign),
                this.taint.lub(other.taint)
        );
    }

    @Override
    public boolean lessOrEqualAux(SignXThreeTaintLattice other) {
        return this.sign.lessOrEqual(other.sign)
                && this.taint.lessOrEqual(other.taint);
    }

    @Override
    public String toString() {
        return "(" + sign.toString() + ", " + taint.toString() + ")";
    }
}
