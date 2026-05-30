package it.unive.scsr.analysis.signxthreetaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import it.unive.scsr.analysis.extendedsign.ExtendedSign;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignXThreeTaint implements BaseNonRelationalValueDomain<SignXThreeTaintLattice> {

    private final ExtendedSign signDomain = new ExtendedSign();
    private final TaintThreeLevels taintDomain = new TaintThreeLevels();

    @Override
    public SignXThreeTaintLattice top() {
        return SignXThreeTaintLattice.TOP;
    }

    @Override
    public SignXThreeTaintLattice bottom() {
        return SignXThreeTaintLattice.BOTTOM;
    }

    @Override
    public SignXThreeTaintLattice evalConstant(Constant constant,
                                               ProgramPoint pp,
                                               SemanticOracle oracle)
            throws SemanticException {

        ExtendedSignLattice sign = signDomain.evalConstant(constant, pp, oracle);
        TaintThreeLevelsLattice taint = taintDomain.evalConstant(constant, pp, oracle);

        return new SignXThreeTaintLattice(sign, taint);
    }

    @Override
    public SignXThreeTaintLattice evalUnaryExpression(UnaryExpression expression,
                                                      SignXThreeTaintLattice arg,
                                                      ProgramPoint pp,
                                                      SemanticOracle oracle)
            throws SemanticException {

        ExtendedSignLattice sign = signDomain.evalUnaryExpression(
                expression, arg.getSign(), pp, oracle);

        TaintThreeLevelsLattice taint = taintDomain.evalUnaryExpression(
                expression, arg.getTaint(), pp, oracle);

        return new SignXThreeTaintLattice(sign, taint);
    }

    @Override
    public SignXThreeTaintLattice evalBinaryExpression(BinaryExpression expression,
                                                       SignXThreeTaintLattice left,
                                                       SignXThreeTaintLattice right,
                                                       ProgramPoint pp,
                                                       SemanticOracle oracle)
            throws SemanticException {

        ExtendedSignLattice sign = signDomain.evalBinaryExpression(
                expression, left.getSign(), right.getSign(), pp, oracle);

        TaintThreeLevelsLattice taint = taintDomain.evalBinaryExpression(
                expression, left.getTaint(), right.getTaint(), pp, oracle);

        return new SignXThreeTaintLattice(sign, taint);
    }
}
