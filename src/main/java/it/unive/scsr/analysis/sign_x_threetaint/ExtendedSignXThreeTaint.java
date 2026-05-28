package it.unive.scsr.analysis.sign_x_threetaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.scsr.analysis.extendedsign.ExtendedSign;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class ExtendedSignXThreeTaint implements BaseNonRelationalValueDomain<ExtendedSignXThreeTaintLattice> {
    public static ExtendedSign extSign = new ExtendedSign();
    public static TaintThreeLevels threeTaint = new TaintThreeLevels();

    public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
    public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

    @Override
    public ExtendedSignXThreeTaintLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignXThreeTaintLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        TaintThreeLevelsLattice projectedTaint = threeTaint.evalUnaryExpression(expression, arg.threeTaint, pp, oracle);
        if (pp != null && pp.getCFG() != null){
            Annotations annotations = pp.getCFG().getDescriptor().getAnnotations();
            if (annotations.contains(BaseTaint.TAINTED_MATCHER)) {
                projectedTaint = TaintThreeLevelsLattice.TAINT;
            }
            else if (annotations.contains(BaseTaint.CLEAN_MATCHER)){
                projectedTaint = TaintThreeLevelsLattice.CLEAN;
            }
        }
        return new ExtendedSignXThreeTaintLattice(extSign.evalUnaryExpression(expression, arg.extSign, pp, oracle),projectedTaint);
    }

    @Override
    public ExtendedSignXThreeTaintLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignXThreeTaintLattice left, ExtendedSignXThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        TaintThreeLevelsLattice projectedTaint = threeTaint.evalBinaryExpression(expression, left.threeTaint, right.threeTaint, pp, oracle);
        if (pp != null && pp.getCFG() != null){
            Annotations annotations = pp.getCFG().getDescriptor().getAnnotations();
            if (annotations.contains(BaseTaint.TAINTED_MATCHER)) {
                projectedTaint = TaintThreeLevelsLattice.TAINT;
            }
            else if (annotations.contains(BaseTaint.CLEAN_MATCHER)){
                projectedTaint = TaintThreeLevelsLattice.CLEAN;
            }
        }
        return new ExtendedSignXThreeTaintLattice(extSign.evalBinaryExpression(expression, left.extSign, right.extSign, pp, oracle),projectedTaint);
    }

    @Override
    public ExtendedSignXThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        TaintThreeLevelsLattice projectedTaint = threeTaint.evalConstant(constant, pp, oracle);
        if (pp != null && pp.getCFG() != null){
            Annotations annotations = pp.getCFG().getDescriptor().getAnnotations();
            if (annotations.contains(BaseTaint.TAINTED_MATCHER)) {
                projectedTaint = TaintThreeLevelsLattice.TAINT;
            }
            else if (annotations.contains(BaseTaint.CLEAN_MATCHER)){
                projectedTaint = TaintThreeLevelsLattice.CLEAN;
            }
        }
        return new ExtendedSignXThreeTaintLattice(extSign.evalConstant(constant, pp, oracle),projectedTaint);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignXThreeTaintLattice left, ExtendedSignXThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return extSign.satisfiesBinaryExpression(expression, left.extSign, right.extSign, pp, oracle).glb(threeTaint.satisfiesBinaryExpression(expression, left.threeTaint, right.threeTaint, pp, oracle));
    }

    @Override
    public ValueEnvironment<ExtendedSignXThreeTaintLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignXThreeTaintLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
        return environment;
    }

    @Override
    public ExtendedSignXThreeTaintLattice top() {
        return ExtendedSignXThreeTaintLattice.TOP;
    }

    @Override
    public ExtendedSignXThreeTaintLattice bottom() {
        return ExtendedSignXThreeTaintLattice.BOTTOM;
    }
}
