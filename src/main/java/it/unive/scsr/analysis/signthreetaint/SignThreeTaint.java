package it.unive.scsr.analysis.signthreetaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.extendedsign.ExtendedSign;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.cfg.statement.call.Call;
import java.util.List;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.symbolic.value.Identifier;

public class SignThreeTaint implements BaseNonRelationalValueDomain<SignThreeTaintLattice> {
    
    public final ExtendedSign sign = new ExtendedSign();

    public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
    public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

    @Override
    public SignThreeTaintLattice top() { return SignThreeTaintLattice.TOP; }

    @Override
    public SignThreeTaintLattice bottom() { return SignThreeTaintLattice.BOTTOM; }

    @Override
    public SignThreeTaintLattice evalIdentifier(
        Identifier id, ValueEnvironment<SignThreeTaintLattice> env,
        ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

    if (id.getAnnotations().contains(BaseTaint.TAINTED_MATCHER))
        return new SignThreeTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.TAINT);

    if (id.getAnnotations().contains(BaseTaint.CLEAN_MATCHER)) {
        SignThreeTaintLattice current = env.getState(id);
        return new SignThreeTaintLattice(current.first, TaintThreeLevelsLattice.CLEAN);
    }

    return env.getState(id);
}

    @Override
    public SignThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice s = sign.evalConstant(constant, pp, oracle);
        return new SignThreeTaintLattice(s, TaintThreeLevelsLattice.CLEAN);
    }

    @Override
    public SignThreeTaintLattice evalUnaryExpression(UnaryExpression expression, SignThreeTaintLattice arg, ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice s = sign.evalUnaryExpression(expression, arg.first, pp, oracle);
        return new SignThreeTaintLattice(s, arg.second);
    }

    @Override
    public SignThreeTaintLattice evalBinaryExpression(BinaryExpression expression, SignThreeTaintLattice left, SignThreeTaintLattice right,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice signer = sign.evalBinaryExpression(expression, left.first, right.first, pp, oracle);
        if (signer == ExtendedSignLattice.BOTTOM) return SignThreeTaintLattice.BOTTOM;
        TaintThreeLevelsLattice taint = left.second.lub(right.second);
        return new SignThreeTaintLattice(signer, taint);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, SignThreeTaintLattice left, SignThreeTaintLattice right,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return sign.satisfiesBinaryExpression(expression, left.first, right.first, pp, oracle);
    }

    @Override
    public ValueEnvironment<SignThreeTaintLattice> assumeBinaryExpression(
        ValueEnvironment<SignThreeTaintLattice> env, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle
    ) throws SemanticException { 
        return env; // No relational information is tracked, so we cannot refine the environment
    }

    
}