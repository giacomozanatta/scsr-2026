package it.unive.scsr.analysis.signx3taint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class SignX3Taint extends BaseTaint<SignX3TaintLattice> {

    public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
    public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

    @Override
    public SignX3TaintLattice top() {
        return new SignX3TaintLattice(SignX3TaintLattice.Top, SignX3TaintLattice.Top);
    }

    @Override
    public SignX3TaintLattice bottom() {
        return new SignX3TaintLattice(SignX3TaintLattice.Bottom, SignX3TaintLattice.Bottom);
    }

    @Override
    protected SignX3TaintLattice tainted() {
        return new SignX3TaintLattice(SignX3TaintLattice.Top, SignX3TaintLattice.Taint);
    }

    @Override
    protected SignX3TaintLattice clean() {
        return new SignX3TaintLattice(SignX3TaintLattice.Top, SignX3TaintLattice.Clean);
    }

    @Override
    public SignX3TaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        String s;
        if (constant.getValue() instanceof Integer) {
            Integer n = (Integer) constant.getValue();
            if (n == 0)     s = SignX3TaintLattice.ZERO;
            else if (n > 0) s = SignX3TaintLattice.POS;
            else            s = SignX3TaintLattice.NEG;
        } else {
            s = SignX3TaintLattice.Top;
        }
        return new SignX3TaintLattice(s, SignX3TaintLattice.Clean);
    }

    @Override
    public SignX3TaintLattice evalUnaryExpression(UnaryExpression expression, SignX3TaintLattice arg,
                                                  ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        String s = SignX3TaintLattice.Top;
        String a = arg.getSign();

        if (expression.getOperator() == NumericNegation.INSTANCE) {
            if      (SignX3TaintLattice.NEG.equals(a))    s = SignX3TaintLattice.POS;
            else if (SignX3TaintLattice.POS.equals(a))    s = SignX3TaintLattice.NEG;
            else if (SignX3TaintLattice.ZERO.equals(a))   s = SignX3TaintLattice.ZERO;
            else if (SignX3TaintLattice.Top.equals(a))    s = SignX3TaintLattice.Top;
            else if (SignX3TaintLattice.Bottom.equals(a)) s = SignX3TaintLattice.Bottom;
        }

        return new SignX3TaintLattice(s, arg.get3Taint());
    }

    @Override
    public SignX3TaintLattice evalBinaryExpression(BinaryExpression expression, SignX3TaintLattice l,
                                                   SignX3TaintLattice r, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        String s = SignX3TaintLattice.Top;
        String left  = l.getSign();
        String right = r.getSign();

        final String POS  = SignX3TaintLattice.POS;
        final String NEG  = SignX3TaintLattice.NEG;
        final String ZERO = SignX3TaintLattice.ZERO;
        final String T    = SignX3TaintLattice.Top;
        final String BOT  = SignX3TaintLattice.Bottom;

        if (expression.getOperator() instanceof AdditionOperator) {
            if ((POS.equals(left) && NEG.equals(right)) || (NEG.equals(left) && POS.equals(right)))
                s = T;
            if ((POS.equals(left) && (POS.equals(right) || ZERO.equals(right)))
                    || ((POS.equals(left) || ZERO.equals(left)) && POS.equals(right)))
                s = POS;
            if ((NEG.equals(left) && (NEG.equals(right) || ZERO.equals(right)))
                    || ((NEG.equals(left) || ZERO.equals(left)) && NEG.equals(right)))
                s = NEG;
            if (ZERO.equals(left) && ZERO.equals(right))
                s = ZERO;
            if (BOT.equals(left) || BOT.equals(right))
                s = BOT;
            if (T.equals(left) || T.equals(right))
                s = T;

        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            if (POS.equals(left) && POS.equals(right))
                s = POS;
            else if ((POS.equals(left) && NEG.equals(right)) || (NEG.equals(left) && POS.equals(right)))
                s = NEG;
            else if (NEG.equals(left) && NEG.equals(right))
                s = POS;
            else if (BOT.equals(left) || BOT.equals(right))
                s = BOT;
            else if (ZERO.equals(left) || ZERO.equals(right))
                s = ZERO;
            else if (T.equals(left) || T.equals(right))
                s = T;

        } else if (expression.getOperator() instanceof SubtractionOperator) {
            UnaryExpression neg = new UnaryExpression(
                    expression.getStaticType(),
                    expression.getRight(),
                    NumericNegation.INSTANCE,
                    expression.getCodeLocation());

            right = evalUnaryExpression(neg, r, pp, oracle).getSign();

            if ((POS.equals(left) && NEG.equals(right)) || (NEG.equals(left) && POS.equals(right)))
                s = T;
            if ((POS.equals(left) && (POS.equals(right) || ZERO.equals(right)))
                    || ((POS.equals(left) || ZERO.equals(left)) && POS.equals(right)))
                s = POS;
            if ((NEG.equals(left) && (NEG.equals(right) || ZERO.equals(right)))
                    || ((NEG.equals(left) || ZERO.equals(left)) && NEG.equals(right)))
                s = NEG;
            if (ZERO.equals(left) && ZERO.equals(right))
                s = ZERO;
            if (BOT.equals(left) || BOT.equals(right))
                s = BOT;
            if (T.equals(left) || T.equals(right))
                s = T;

        } else if (expression.getOperator() instanceof DivisionOperator) {
            if (BOT.equals(left) || BOT.equals(right))
                s = BOT;
            else if (ZERO.equals(right))
                s = T;
            else if (ZERO.equals(left))
                s = ZERO;
            else if (T.equals(left) || T.equals(right))
                s = T;
            else if (POS.equals(left) && POS.equals(right))
                s = POS;
            else if (POS.equals(left) && NEG.equals(right))
                s = NEG;
            else if (NEG.equals(left) && POS.equals(right))
                s = NEG;
            else if (NEG.equals(left) && NEG.equals(right))
                s = POS;

        } else if (expression.getOperator() instanceof ModuloOperator) {
            s = right;
        } else if (expression.getOperator() instanceof RemainderOperator) {
            s = left;
        }

        return new SignX3TaintLattice(s, l.or(r).get3Taint());
    }
}