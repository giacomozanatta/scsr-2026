package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.sign.extended.ExtendedSign;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;

public class SignTaint implements BaseNonRelationalValueDomain<SignTaintLattice> {

    private static final ExtendedSign SIGN = new ExtendedSign();
    private static final TaintThreeLevels TAINT = new TaintThreeLevels();

    @Override
    public SignTaintLattice top() {
        return SignTaintLattice.TOP;
    }

    @Override
    public SignTaintLattice bottom() {
        return SignTaintLattice.BOTTOM;
    }

    @Override
    public SignTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        return new SignTaintLattice(
                SIGN.evalConstant(constant, pp, oracle),
                TAINT.evalConstant(constant, pp, oracle)
        );
    }

    @Override
    public SignTaintLattice evalUnaryExpression(UnaryExpression expression, SignTaintLattice arg,
                                                ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new SignTaintLattice(
                SIGN.evalUnaryExpression(expression, arg.sign, pp, oracle),
                TAINT.evalUnaryExpression(expression, arg.taint, pp, oracle)
        );
    }

    @Override
    public SignTaintLattice evalBinaryExpression(BinaryExpression expression, SignTaintLattice left,
                                                 SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new SignTaintLattice(
                SIGN.evalBinaryExpression(expression, left.sign, right.sign, pp, oracle),
                TAINT.evalBinaryExpression(expression, left.taint, right.taint, pp, oracle)
        );
    }

}