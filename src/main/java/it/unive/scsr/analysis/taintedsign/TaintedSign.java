package it.unive.scsr.analysis.taintedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.extendedsign.ExtendedSign;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class TaintedSign
        implements BaseNonRelationalValueDomain<TaintedSignLattice> {

    public static final ExtendedSign SIGN_ANALYSIS = new ExtendedSign();
    public static final TaintThreeLevels TAINT_ANALYSIS = new TaintThreeLevels();

    @Override
    public TaintedSignLattice top() {
        return new TaintedSignLattice().top();
    }

    @Override
    public TaintedSignLattice bottom() {
        return new TaintedSignLattice().bottom();
    }

    private TaintedSignLattice annotate(ExtendedSignLattice sign, TaintThreeLevelsLattice taint, ProgramPoint pp) {
        if (pp != null && pp.getCFG() != null) {
            Annotations annotations = pp.getCFG().getDescriptor().getAnnotations();

            if (annotations.contains(BaseTaint.TAINTED_MATCHER))
                return new TaintedSignLattice(sign, TaintThreeLevelsLattice.TAINT);

        }

        return new TaintedSignLattice(sign, taint);
    }

    @Override
    public TaintedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        ExtendedSignLattice extendedSignResult = SIGN_ANALYSIS.evalConstant(constant, pp, oracle);
        TaintThreeLevelsLattice taintResult = TAINT_ANALYSIS.evalConstant(constant, pp, oracle);
        return annotate(extendedSignResult, taintResult, pp);
    }

    @Override
    public TaintedSignLattice evalUnaryExpression(UnaryExpression expression, TaintedSignLattice arg,
                                                  ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice extendedSignResult = SIGN_ANALYSIS.evalUnaryExpression(
                expression,
                arg.first,
                pp,
                oracle);
        TaintThreeLevelsLattice taintResult = TAINT_ANALYSIS.evalUnaryExpression(
                expression,
                arg.second,
                pp,
                oracle);
        return annotate(extendedSignResult, taintResult, pp);
    }

    @Override
    public TaintedSignLattice evalBinaryExpression(BinaryExpression expression, TaintedSignLattice left,
                                                   TaintedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice extendedSignResult = SIGN_ANALYSIS.evalBinaryExpression(
                expression,
                left.first,
                right.first,
                pp,
                oracle);

        TaintThreeLevelsLattice taintResult = TAINT_ANALYSIS.evalBinaryExpression(
                expression,
                left.second,
                right.second,
                pp,
                oracle);

        return annotate(extendedSignResult, taintResult, pp);
    }
}