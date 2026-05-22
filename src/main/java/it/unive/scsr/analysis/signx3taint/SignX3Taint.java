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
    public SignX3TaintLattice
    evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        String s;

        if(constant.getValue() instanceof Integer) {
            //I needt to check the integer value to
            // assign the right approx value
            Integer n = (Integer) constant.getValue();
            if(n == 0)
                s = SignX3TaintLattice.ZERO;
            else if(n > 0)
                s = SignX3TaintLattice.POS;
            else
                s = SignX3TaintLattice.NEG;
        }
        else
            s = SignX3TaintLattice.Top;

        return new SignX3TaintLattice(s, SignX3TaintLattice.Clean);
    }

    @Override
    public SignX3TaintLattice evalUnaryExpression(UnaryExpression expression, SignX3TaintLattice arg, ProgramPoint pp,
                                           SemanticOracle oracle) throws SemanticException {

        String a, s;
        s = SignX3TaintLattice.Top;
        a = arg.getSign();

        if(expression.getOperator() == NumericNegation.INSTANCE) {
            if(a == SignX3TaintLattice.NEG)
                s = SignX3TaintLattice.POS;
            else if(a == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.NEG;
            else if(a == SignX3TaintLattice.ZERO)
                s = SignX3TaintLattice.ZERO;
            else if(a == SignX3TaintLattice.Top)
                s = SignX3TaintLattice.Top;
            else if(a == SignX3TaintLattice.Bottom)
                s = SignX3TaintLattice.Bottom;
        }

        return new SignX3TaintLattice(s, arg.get3Taint());
    }

    @Override
    public SignX3TaintLattice evalBinaryExpression(BinaryExpression expression, SignX3TaintLattice l, SignX3TaintLattice r,
                                            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        String s, left, right;

        s = SignX3TaintLattice.Top;
        left = l.getSign();
        right = r.getSign();

        if(expression.getOperator() instanceof AdditionOperator) {
            if(left == SignX3TaintLattice.POS && right == SignX3TaintLattice.NEG
                    || left == SignX3TaintLattice.NEG && right == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.Top;
            if(left == SignX3TaintLattice.POS && (right == SignX3TaintLattice.POS || right == SignX3TaintLattice.ZERO)
                    || (left == SignX3TaintLattice.POS || left == SignX3TaintLattice.ZERO) && right == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.POS;
            if(left == SignX3TaintLattice.NEG && (right == SignX3TaintLattice.NEG || right == SignX3TaintLattice.ZERO)
                    || (left == SignX3TaintLattice.NEG || left == SignX3TaintLattice.ZERO) && right == SignX3TaintLattice.NEG)
                s = SignX3TaintLattice.NEG;
            if(left == SignX3TaintLattice.ZERO && right == SignX3TaintLattice.ZERO)
                s = SignX3TaintLattice.ZERO;
            if(left == SignX3TaintLattice.Bottom || right == SignX3TaintLattice.Bottom)
                s = SignX3TaintLattice.Bottom;
            if(left == SignX3TaintLattice.Top || right == SignX3TaintLattice.Top)
                s = SignX3TaintLattice.Top;

        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            if(left == SignX3TaintLattice.POS && right == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.POS;
            else if(left == SignX3TaintLattice.POS  && right == SignX3TaintLattice.NEG
                    || left == SignX3TaintLattice.NEG  && right == SignX3TaintLattice.POS)
                s =  SignX3TaintLattice.NEG;
            else if(left == SignX3TaintLattice.NEG && right == SignX3TaintLattice.NEG)
                s = SignX3TaintLattice.POS;
            else if(left == SignX3TaintLattice.Bottom || right == SignX3TaintLattice.Bottom)
                s = SignX3TaintLattice.Bottom;
            else if(left == SignX3TaintLattice.ZERO || right == SignX3TaintLattice.ZERO)
                s = SignX3TaintLattice.ZERO;
            else if(left == SignX3TaintLattice.Top || right == SignX3TaintLattice.Top)
                s = SignX3TaintLattice.Top;
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            UnaryExpression neg =
                    new UnaryExpression(
                            expression.getStaticType(),
                            expression.getRight(),
                            NumericNegation.INSTANCE,
                            expression.getCodeLocation());

            right = evalUnaryExpression(neg, r, pp, oracle).getSign();

            if(left == SignX3TaintLattice.POS && right == SignX3TaintLattice.NEG
                    || left == SignX3TaintLattice.NEG && right == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.Top;
            if(left == SignX3TaintLattice.POS && (right == SignX3TaintLattice.POS || right == SignX3TaintLattice.ZERO)
                    || (left == SignX3TaintLattice.POS || left == SignX3TaintLattice.ZERO) && right == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.POS;
            if(left == SignX3TaintLattice.NEG && (right == SignX3TaintLattice.NEG || right == SignX3TaintLattice.ZERO)
                    || (left == SignX3TaintLattice.NEG || left == SignX3TaintLattice.ZERO) && right == SignX3TaintLattice.NEG)
                s = SignX3TaintLattice.NEG;
            if(left == SignX3TaintLattice.ZERO && right == SignX3TaintLattice.ZERO)
                s = SignX3TaintLattice.ZERO;
            if(left == SignX3TaintLattice.Bottom || right == SignX3TaintLattice.Bottom)
                s = SignX3TaintLattice.Bottom;
            if(left == SignX3TaintLattice.Top || right == SignX3TaintLattice.Top)
                s = SignX3TaintLattice.Top;
        } else if (expression.getOperator() instanceof DivisionOperator) {
            // x / 0 => non definito
            if (right == SignX3TaintLattice.ZERO)
                s = SignX3TaintLattice.Top;

            // 0 / x => 0 (anche se x è Top, basta che non sia ZERO certo)
            if (left == SignX3TaintLattice.ZERO)
                s = SignX3TaintLattice.ZERO;

            // Top propagazione
            if (left == SignX3TaintLattice.Top || right == SignX3TaintLattice.Top)
                s = SignX3TaintLattice.Top;

            // Bottom propagazione
            if (left == SignX3TaintLattice.Bottom || right == SignX3TaintLattice.Bottom)
                s = SignX3TaintLattice.Bottom;

            // POS / POS = POS
            if (left == SignX3TaintLattice.POS && right == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.POS;

            // POS / NEG = NEG
            if (left == SignX3TaintLattice.POS && right == SignX3TaintLattice.NEG)
                s = SignX3TaintLattice.NEG;

            // NEG / POS = NEG
            if (left == SignX3TaintLattice.NEG && right == SignX3TaintLattice.POS)
                s = SignX3TaintLattice.NEG;

            // NEG / NEG = POS
            if (left == SignX3TaintLattice.NEG && right == SignX3TaintLattice.NEG)
                s = SignX3TaintLattice.POS;

        } else if (expression.getOperator() instanceof ModuloOperator)
            s = right;
        else if (expression.getOperator() instanceof RemainderOperator)
            s = left;




        return new SignX3TaintLattice(s, l.or(r).get3Taint());
    }


}