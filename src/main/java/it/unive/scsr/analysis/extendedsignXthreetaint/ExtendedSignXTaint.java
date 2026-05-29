package it.unive.scsr.analysis.extendedsignXthreetaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.scsr.analysis.extendedSign.ExtendedSign;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class ExtendedSignXTaint implements BaseNonRelationalValueDomain<ExtendedSignXTaintLattice> {
    private final ExtendedSign signDomain = new ExtendedSign();

    @Override
    public ExtendedSignXTaintLattice top() {
        return ExtendedSignXTaintLattice.TOP;
    }

    @Override
    public ExtendedSignXTaintLattice bottom() {
        return ExtendedSignXTaintLattice.BOTTOM;
    }

    @Override
    public ExtendedSignXTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalConstant(constant, pp, oracle);
        return new ExtendedSignXTaintLattice(sign, TaintThreeLevelsLattice.Clean);
    }

    @Override
    public ExtendedSignXTaintLattice evalUnaryExpression(UnaryExpression expression,
                                                              ExtendedSignXTaintLattice arg, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalUnaryExpression(expression, arg.getSign(), pp, oracle);
        return new ExtendedSignXTaintLattice(sign, arg.getTaint()); // taint flows through
    }

    @Override
    public ExtendedSignXTaintLattice evalBinaryExpression(BinaryExpression expression,
                                                               ExtendedSignXTaintLattice left, ExtendedSignXTaintLattice right,
                                                               ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalBinaryExpression(
                expression, left.getSign(), right.getSign(), pp, oracle);
        TaintThreeLevelsLattice taint = left.getTaint().or(right.getTaint());
        return new ExtendedSignXTaintLattice(sign, taint);
    }

    @Override
    public ExtendedSignXTaintLattice fixedVariable(
            Identifier id, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        Annotations annots = id.getAnnotations();

        // Sign component: use the sign domain's fixed variable
        ExtendedSignLattice sign = signDomain.fixedVariable(id, pp, oracle);

        if (annots.contains(BaseTaint.TAINTED_MATCHER))
            return new ExtendedSignXTaintLattice(sign, TaintThreeLevelsLattice.Tainted);
        if (annots.contains(BaseTaint.CLEAN_MATCHER))
            return new ExtendedSignXTaintLattice(sign, TaintThreeLevelsLattice.Clean);

        // No taint annotation: fall through to default (sign domain decides, taint is Clean)
        return new ExtendedSignXTaintLattice(sign, TaintThreeLevelsLattice.Clean);
    }

    @Override
    public ExtendedSignXTaintLattice evalIdentifier(
            Identifier id,
            ValueEnvironment<ExtendedSignXTaintLattice> environment,
            ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        Annotations annots = id.getAnnotations();

        // If the identifier carries a taint annotation, it overrides the environment
        if (annots.contains(BaseTaint.TAINTED_MATCHER)) {
            ExtendedSignXTaintLattice current = environment.getState(id);
            ExtendedSignLattice sign = current.isBottom() ? ExtendedSignLattice.TOP : current.getSign();
            return new ExtendedSignXTaintLattice(sign, TaintThreeLevelsLattice.Tainted);
        }
        if (annots.contains(BaseTaint.CLEAN_MATCHER)) {
            ExtendedSignXTaintLattice current = environment.getState(id);
            ExtendedSignLattice sign = current.isBottom() ? ExtendedSignLattice.TOP : current.getSign();
            return new ExtendedSignXTaintLattice(sign, TaintThreeLevelsLattice.Clean);
        }

        // No annotation: just look up the environment (normal behaviour)
        return environment.getState(id);
    }


}
