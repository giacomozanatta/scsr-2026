package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.analysis.SemanticOracle;

import it.unive.scsr.analysis.extendedSign.ExtendedSign;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignThreeTaint implements BaseNonRelationalValueDomain<SignThreeTaintLattice> {

    private final ExtendedSign signDomain = new ExtendedSign();
    private final TaintThreeLevels taintDomain = new TaintThreeLevels();

    @Override
    public SignThreeTaintLattice top() {return SignThreeTaintLattice.TOP;}

    @Override
    public SignThreeTaintLattice bottom() {return SignThreeTaintLattice.BOTTOM;}

    @Override
    public SignThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalConstant(constant, pp, oracle);
        TaintThreeLevelsLattice taint = taintDomain.evalConstant(constant, pp, oracle);
        return new SignThreeTaintLattice(sign, taint);
    }

    @Override
    public SignThreeTaintLattice evalIdentifier(Identifier id, ValueEnvironment<SignThreeTaintLattice> environment, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return environment.getState(id);
    }

    @Override
    public SignThreeTaintLattice evalUnaryExpression(UnaryExpression expression, SignThreeTaintLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalUnaryExpression(expression, arg.getSign(), pp, oracle);

        TaintThreeLevelsLattice taint = taintDomain.evalUnaryExpression(expression, arg.getTaint(), pp, oracle);

        return new SignThreeTaintLattice(sign, taint);
    }

    @Override
    public SignThreeTaintLattice evalBinaryExpression(BinaryExpression expression, SignThreeTaintLattice left, SignThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);

        TaintThreeLevelsLattice taint = taintDomain.evalBinaryExpression(expression, left.getTaint(), right.getTaint(), pp, oracle);

        return new SignThreeTaintLattice(sign, taint);
    }

}
