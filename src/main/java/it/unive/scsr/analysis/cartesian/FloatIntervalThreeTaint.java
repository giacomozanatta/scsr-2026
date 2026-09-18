package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;

import it.unive.scsr.analysis.floatInterval.FloatInterval;
import it.unive.scsr.analysis.floatInterval.FloatIntervalLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class FloatIntervalThreeTaint implements BaseNonRelationalValueDomain<FloatIntervalThreeTaintLattice> {

    private final FloatInterval intervalDomain = new FloatInterval();
    private final TaintThreeLevels taintDomain = new TaintThreeLevels();

    @Override
    public FloatIntervalThreeTaintLattice top() { return FloatIntervalThreeTaintLattice.TOP; }

    @Override
    public FloatIntervalThreeTaintLattice bottom() { return FloatIntervalThreeTaintLattice.BOTTOM; }

    @Override
    public FloatIntervalThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        FloatIntervalLattice interval = intervalDomain.evalConstant(constant, pp, oracle);
        TaintThreeLevelsLattice taint = taintDomain.evalConstant(constant, pp, oracle);
        return new FloatIntervalThreeTaintLattice(interval, taint);
    }

    @Override
    public FloatIntervalThreeTaintLattice fixedVariable(Identifier id, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        Annotations annots = id.getAnnotations();
        if (!annots.isEmpty()) {
            if (annots.contains(BaseTaint.TAINTED_MATCHER))
                return new FloatIntervalThreeTaintLattice(intervalDomain.top(), TaintThreeLevelsLattice.Taint);
            if (annots.contains(BaseTaint.CLEAN_MATCHER))
                return new FloatIntervalThreeTaintLattice(intervalDomain.top(), TaintThreeLevelsLattice.Clean);
        }
        return BaseNonRelationalValueDomain.super.fixedVariable(id, pp, oracle);
    }

    @Override
    public FloatIntervalThreeTaintLattice evalIdentifier(Identifier id, ValueEnvironment<FloatIntervalThreeTaintLattice> environment, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        Annotations annots = id.getAnnotations();
        if (!annots.isEmpty()) {
            if (annots.contains(BaseTaint.TAINTED_MATCHER)) {
                FloatIntervalLattice interval = intervalDomain.top(); // nessun vincolo noto sul valore numerico
                return new FloatIntervalThreeTaintLattice(interval, TaintThreeLevelsLattice.Taint);
            }
            if (annots.contains(BaseTaint.CLEAN_MATCHER)) {
                FloatIntervalLattice interval = intervalDomain.top();
                return new FloatIntervalThreeTaintLattice(interval, TaintThreeLevelsLattice.Clean);
            }
        }
        return environment.getState(id);
    }

    @Override
    public FloatIntervalThreeTaintLattice evalUnaryExpression(UnaryExpression expression, FloatIntervalThreeTaintLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        FloatIntervalLattice interval = intervalDomain.evalUnaryExpression(expression, arg.getInterval(), pp, oracle);
        TaintThreeLevelsLattice taint = taintDomain.evalUnaryExpression(expression, arg.getTaint(), pp, oracle);
        return new FloatIntervalThreeTaintLattice(interval, taint);
    }

    @Override
    public FloatIntervalThreeTaintLattice evalBinaryExpression(BinaryExpression expression, FloatIntervalThreeTaintLattice left, FloatIntervalThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        FloatIntervalLattice interval = intervalDomain.evalBinaryExpression(expression, left.getInterval(), right.getInterval(), pp, oracle);
        TaintThreeLevelsLattice taint = taintDomain.evalBinaryExpression(expression, left.getTaint(), right.getTaint(), pp, oracle);
        return new FloatIntervalThreeTaintLattice(interval, taint);
    }
}