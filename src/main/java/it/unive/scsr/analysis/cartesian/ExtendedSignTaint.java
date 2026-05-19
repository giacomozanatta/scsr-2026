package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.sign.extendedSign.ExtendedSign;
import it.unive.scsr.analysis.sign.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.analysis.informationFlow.BaseTaint;

public class ExtendedSignTaint implements BaseNonRelationalValueDomain<ExtendedSignTaintLattice> {
    public static final ExtendedSign signDomain = new ExtendedSign();
    public static final TaintThreeLevels taintDomain = new TaintThreeLevels();

    @Override
    public ExtendedSignTaintLattice top() {
        return new ExtendedSignTaintLattice(signDomain.top(), taintDomain.top());
    }

    @Override
    public ExtendedSignTaintLattice bottom() {
        return new ExtendedSignTaintLattice(signDomain.bottom(), taintDomain.bottom());
    }

    private ExtendedSignTaintLattice applyAnnotations(ExtendedSignTaintLattice result, ProgramPoint pp) {
        if (pp != null && pp.getCFG() != null) {
            var annotations = pp.getCFG().getDescriptor().getAnnotations();

            if (annotations.contains(new BasicAnnotationMatcher(BaseTaint.TAINTED_ANNOTATION))) {
                return new ExtendedSignTaintLattice(result.getSign(), TaintThreeLevelsLattice.Taint);
            }
            else if (annotations.contains(new BasicAnnotationMatcher(BaseTaint.CLEAN_ANNOTATION))) {
                return new ExtendedSignTaintLattice(result.getSign(), TaintThreeLevelsLattice.Clean);
            }
        }
        return result;
    }

    @Override
    public ExtendedSignTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignTaintLattice res = new ExtendedSignTaintLattice(signDomain.evalConstant(constant, pp, oracle), taintDomain.evalConstant(constant, pp, oracle));
        return applyAnnotations(res, pp);
    }

    @Override
    public ExtendedSignTaintLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignTaintLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignTaintLattice res = new ExtendedSignTaintLattice(signDomain.evalUnaryExpression(expression, arg.getSign(), pp, oracle), taintDomain.evalUnaryExpression(expression, arg.getTaint(), pp, oracle));
        return applyAnnotations(res, pp);
    }

    @Override
    public ExtendedSignTaintLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignTaintLattice left, ExtendedSignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignTaintLattice res = new ExtendedSignTaintLattice(signDomain.evalBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle), taintDomain.evalBinaryExpression(expression, left.getTaint(), right.getTaint(), pp, oracle));
        return applyAnnotations(res, pp);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignTaintLattice left, ExtendedSignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        Satisfiability signSat = signDomain.satisfiesBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);
        Satisfiability taintSat = taintDomain.satisfiesBinaryExpression(expression, left.getTaint(), right.getTaint(), pp, oracle);
        return signSat.glb(taintSat);
    }

    @Override
    public ValueEnvironment<ExtendedSignTaintLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignTaintLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
        ValueEnvironment<ExtendedSignLattice> signEnv = new ValueEnvironment<>(ExtendedSignLattice.TOP);
        ValueEnvironment<TaintThreeLevelsLattice> taintEnv = new ValueEnvironment<>(TaintThreeLevelsLattice.TOP);

        for (Identifier id : environment.getKeys()) {
            ExtendedSignTaintLattice pair = environment.getState(id);
            signEnv = signEnv.putState(id, pair.getSign());
            taintEnv = taintEnv.putState(id, pair.getTaint());
        }

        ValueEnvironment<ExtendedSignLattice> refinedSign = signDomain.assumeBinaryExpression(signEnv, expression, src, dest, oracle);
        ValueEnvironment<TaintThreeLevelsLattice> refinedTaint = taintDomain.assumeBinaryExpression(taintEnv, expression, src, dest, oracle);

        if (refinedSign.isBottom() || refinedTaint.isBottom()) {
            return environment.bottom();
        }

        ValueEnvironment<ExtendedSignTaintLattice> result = new ValueEnvironment<>(
                new ExtendedSignTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TOP)
        );

        for (Identifier id : refinedSign.getKeys()) {
            ExtendedSignLattice s = refinedSign.getState(id);
            TaintThreeLevelsLattice t = refinedTaint.getState(id);
            result = result.putState(id, new ExtendedSignTaintLattice(s, t));
        }
        return result;
    }
}
