package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
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
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.scsr.analysis.sign.ExtendedSign;
import it.unive.scsr.analysis.sign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignTaint implements BaseNonRelationalValueDomain<SignTaintLattice> {

    public static final Annotation SINK_ANNOTATION = new Annotation("lisa.signtaint.Sink");
    public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

    private static final ExtendedSign SIGN = new ExtendedSign();

    @Override public SignTaintLattice top()    { return SignTaintLattice.TOP; }
    @Override public SignTaintLattice bottom() { return SignTaintLattice.BOTTOM; }

    @Override
    public SignTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        ExtendedSignLattice s = SIGN.evalConstant(constant, pp, oracle);
        return new SignTaintLattice(s, TaintThreeLevelsLattice.CLEAN);
    }

    @Override
    public SignTaintLattice evalUnaryExpression(UnaryExpression expression, SignTaintLattice arg,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice s = SIGN.evalUnaryExpression(expression, arg.sign, pp, oracle);
        return new SignTaintLattice(s, arg.taint);
    }

    @Override
    public SignTaintLattice evalBinaryExpression(BinaryExpression expression, SignTaintLattice left,
            SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left.isBottom() || right.isBottom()) return SignTaintLattice.BOTTOM;
        ExtendedSignLattice s = SIGN.evalBinaryExpression(expression, left.sign, right.sign, pp, oracle);
        TaintThreeLevelsLattice t = left.taint.or(right.taint);
        return new SignTaintLattice(s, t);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, SignTaintLattice left,
            SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) {
        return SIGN.satisfiesBinaryExpression(expression, left.sign, right.sign, pp, oracle);
    }

    @Override
    public ValueEnvironment<SignTaintLattice> assumeBinaryExpression(
            ValueEnvironment<SignTaintLattice> environment,
            BinaryExpression expression,
            ProgramPoint src, ProgramPoint dest,
            SemanticOracle oracle) throws SemanticException {

        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED) return environment.bottom();
        if (sat == Satisfiability.SATISFIED)     return environment;

        BinaryOperator op = expression.getOperator();
        ValueExpression left  = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();

        Identifier id;
        ExtendedSignLattice eval;
        boolean rightIsExpr;

        if (left instanceof Identifier) {
            eval = eval(environment, right, src, oracle).sign;
            id = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            eval = eval(environment, left, src, oracle).sign;
            id = (Identifier) right;
            rightIsExpr = false;
        } else {
            return environment;
        }

        SignTaintLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom()) return environment.bottom();

        ExtendedSignLattice[] atoms = { ExtendedSignLattice.NEG, ExtendedSignLattice.ZERO, ExtendedSignLattice.POS };
        ExtendedSignLattice signUpdate = null;

        if (op == ComparisonEq.INSTANCE) {
            signUpdate = starting.sign.glb(eval);
        } else {
            for (ExtendedSignLattice s : atoms) {
                boolean relevant;
                if (op == ComparisonGe.INSTANCE)
                    relevant = rightIsExpr ? s.gt(eval).or(s.eq(eval)).mightBeTrue()
                                          : eval.gt(s).or(eval.eq(s)).mightBeTrue();
                else if (op == ComparisonGt.INSTANCE)
                    relevant = rightIsExpr ? s.gt(eval).mightBeTrue() : eval.gt(s).mightBeTrue();
                else if (op == ComparisonLe.INSTANCE)
                    relevant = rightIsExpr ? s.gt(eval).mightBeFalse() : eval.gt(s).mightBeFalse();
                else if (op == ComparisonLt.INSTANCE)
                    relevant = rightIsExpr ? s.gt(eval).or(s.eq(eval)).mightBeFalse()
                                          : eval.gt(s).or(eval.eq(s)).mightBeFalse();
                else continue;
                if (relevant) {
                    ExtendedSignLattice candidate = starting.sign.glb(s);
                    signUpdate = signUpdate == null ? candidate : signUpdate.lub(candidate);
                }
            }
        }

        if (signUpdate == null)          return environment;
        if (signUpdate.isBottom())       return environment.bottom();
        return environment.putState(id, new SignTaintLattice(signUpdate, starting.taint));
    }
}
