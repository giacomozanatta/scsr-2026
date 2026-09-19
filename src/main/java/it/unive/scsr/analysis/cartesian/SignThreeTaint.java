package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.analysis.SemanticOracle;

import it.unive.scsr.analysis.extendedSign.ExtendedSign;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignThreeTaint implements BaseNonRelationalValueDomain<SignThreeTaintLattice> {

    private final ExtendedSign signDomain = new ExtendedSign();
    private final TaintThreeLevels taintDomain = new TaintThreeLevels();

    @Override
    public SignThreeTaintLattice top() {return SignThreeTaintLattice.TOP;}

    @Override
    public SignThreeTaintLattice bottom() {return SignThreeTaintLattice.BOTTOM;}

    @Override
    public SignThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalConstant(constant, pp, oracle);
        TaintThreeLevelsLattice taint = taintDomain.evalConstant(constant, pp, oracle);
        return new SignThreeTaintLattice(sign, taint);
    }

    @Override
    public SignThreeTaintLattice evalIdentifier(Identifier id, ValueEnvironment<SignThreeTaintLattice> environment, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (id.getAnnotations().contains(BaseTaint.TAINTED_MATCHER))
            return new SignThreeTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.Taint);

        if (id.getAnnotations().contains(BaseTaint.CLEAN_MATCHER)) {
            SignThreeTaintLattice current = environment.getState(id);
            return new SignThreeTaintLattice(current.getSign(), TaintThreeLevelsLattice.Clean);
        }

        return environment.getState(id);
    }

    @Override
    public SignThreeTaintLattice evalUnaryExpression(UnaryExpression expression, SignThreeTaintLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalUnaryExpression(expression, arg.getSign(), pp, oracle);

        TaintThreeLevelsLattice taint = taintDomain.evalUnaryExpression(expression, arg.getTaint(), pp, oracle);

        return new SignThreeTaintLattice(sign, taint);
    }

    @Override
    public SignThreeTaintLattice evalBinaryExpression(BinaryExpression expression, SignThreeTaintLattice left, SignThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);

        TaintThreeLevelsLattice taint = taintDomain.evalBinaryExpression(expression, left.getTaint(), right.getTaint(), pp, oracle);

        return new SignThreeTaintLattice(sign, taint);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, SignThreeTaintLattice left, SignThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        return signDomain.satisfiesBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle
        );
    }


    @Override
    public ValueEnvironment<SignThreeTaintLattice> assumeBinaryExpression(
            ValueEnvironment<SignThreeTaintLattice> environment,
            BinaryExpression expression,
            ProgramPoint src,
            ProgramPoint dest,
            SemanticOracle oracle) throws SemanticException {

        Satisfiability sat = satisfies(environment, expression, src, oracle);

        if (sat == Satisfiability.NOT_SATISFIED)
            return environment.bottom();

        if (sat == Satisfiability.SATISFIED)
            return environment;

        Identifier id;
        SignThreeTaintLattice eval;
        boolean rightIsExpr;

        ValueExpression left = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();

        if (left instanceof Identifier) {
            id = (Identifier) left;
            eval = eval(environment, right, src, oracle);
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            id = (Identifier) right;
            eval = eval(environment, left, src, oracle);
            rightIsExpr = false;
        } else {
            return environment;
        }

        SignThreeTaintLattice starting = environment.getState(id);

        if (eval.isBottom() || starting.isBottom())
            return environment.bottom();

        ExtendedSignLattice[] all = new ExtendedSignLattice[] {
                ExtendedSignLattice.NEG,
                ExtendedSignLattice.ZERO,
                ExtendedSignLattice.POS
        };

        SignThreeTaintLattice update = null;

        for (ExtendedSignLattice candidate : all) {

            Satisfiability candidateSat;

            if (rightIsExpr) {
                candidateSat = signDomain.satisfiesBinaryExpression(
                        expression,
                        candidate,
                        eval.getSign(),
                        src,
                        oracle
                );
            } else {
                candidateSat = signDomain.satisfiesBinaryExpression(
                        expression,
                        eval.getSign(),
                        candidate,
                        src,
                        oracle
                );
            }

            if (candidateSat != Satisfiability.NOT_SATISFIED) {

                ExtendedSignLattice refinedSign =
                        starting.getSign().glb(candidate);

                if (!refinedSign.isBottom()) {

                    SignThreeTaintLattice refined =
                            new SignThreeTaintLattice(
                                    refinedSign,
                                    starting.getTaint()
                            );

                    update = update == null
                            ? refined
                            : update.lub(refined);
                }
            }
        }

        if (update == null || update.isBottom())
            return environment.bottom();

        return environment.putState(id, update);
    }

}
