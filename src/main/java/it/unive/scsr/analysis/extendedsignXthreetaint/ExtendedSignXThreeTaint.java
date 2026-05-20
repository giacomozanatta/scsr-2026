package it.unive.scsr.analysis.extendedsignXthreetaint;

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
import it.unive.lisa.symbolic.value.operator.binary.*;
        import it.unive.scsr.analysis.extendedSign.ExtendedSign;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class ExtendedSignXThreeTaint {}
/*
implements BaseNonRelationalValueDomain<ExtendedSignXThreeTaintLattice> {
    private final ExtendedSign signDomain = new ExtendedSign();

    @Override
    public ExtendedSignXThreeTaintLattice top() {
        return new ExtendedSignXThreeTaintLattice(ExtendedSignLattice.TOP, TaintThreeLevelsLattice.Top);
    }

    @Override
    public ExtendedSignXThreeTaintLattice bottom() {
        return new ExtendedSignXThreeTaintLattice(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);
    }

    @Override
    public ExtendedSignXThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalConstant(constant, pp, oracle);
        return new ExtendedSignXThreeTaintLattice(sign, TaintThreeLevelsLattice.Clean);
    }

    @Override
    public ExtendedSignXThreeTaintLattice evalUnaryExpression(UnaryExpression expression,
                                                              ExtendedSignXThreeTaintLattice arg, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalUnaryExpression(expression, arg.first, pp, oracle);
        return new ExtendedSignXThreeTaintLattice(sign, arg.second); // taint flows through
    }

    @Override
    public ExtendedSignXThreeTaintLattice evalBinaryExpression(BinaryExpression expression,
                                                               ExtendedSignXThreeTaintLattice left, ExtendedSignXThreeTaintLattice right,
                                                               ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice sign = signDomain.evalBinaryExpression(
                expression, left.first, right.first, pp, oracle);
        TaintThreeLevelsLattice taint = left.second.or(right.second);
        return new ExtendedSignXThreeTaintLattice(sign, taint);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression,
                                                    ExtendedSignXThreeTaintLattice left, ExtendedSignXThreeTaintLattice right,
                                                    ProgramPoint pp, SemanticOracle oracle) {
        return signDomain.satisfiesBinaryExpression(
                expression, left.first, right.first, pp, oracle);
    }

    @Override
    public ValueEnvironment<ExtendedSignXThreeTaintLattice> assumeBinaryExpression(
            ValueEnvironment<ExtendedSignXThreeTaintLattice> environment,
            BinaryExpression expression,
            ProgramPoint src, ProgramPoint dest,
            SemanticOracle oracle) throws SemanticException {

        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED) return environment.bottom();
        if (sat == Satisfiability.SATISFIED)     return environment;

        BinaryOperator op   = expression.getOperator();
        ValueExpression lhs = (ValueExpression) expression.getLeft();
        ValueExpression rhs = (ValueExpression) expression.getRight();

        Identifier id;
        ExtendedSignLattice evalSign;
        boolean idOnLeft;

        if (lhs instanceof Identifier) {
            id = (Identifier) lhs;
            evalSign = eval(environment, rhs, src, oracle).first;
            idOnLeft = true;
        } else if (rhs instanceof Identifier) {
            id = (Identifier) rhs;
            evalSign = eval(environment, lhs, src, oracle).first;
            idOnLeft = false;
        } else {
            return environment;
        }

        ExtendedSignXThreeTaintLattice current = environment.getState(id);
        if (current.isBottom()) return environment.bottom();

        ExtendedSignLattice        currentSign  = current.first;
        TaintThreeLevelsLattice    currentTaint = current.second; // preserved

        ExtendedSignLattice[] atoms = {
                ExtendedSignLattice.LT0, ExtendedSignLattice.EQ0, ExtendedSignLattice.GT0
        };

        ExtendedSignLattice refinedSign = null;
        for (ExtendedSignLattice atom : atoms) {
            if (checkAtom(op, atom, evalSign, idOnLeft).mightBeTrue()) {
                try {
                    ExtendedSignLattice candidate = currentSign.glb(atom);
                    refinedSign = (refinedSign == null) ? candidate : refinedSign.lub(candidate);
                } catch (SemanticException e) {  }
            }
        }

        if (refinedSign == null || refinedSign.isBottom()) return environment.bottom();
        return environment.putState(id, new ExtendedSignXThreeTaintLattice(refinedSign, currentTaint));
    }


    private Satisfiability checkAtom(BinaryOperator op,
                                     ExtendedSignLattice atom,
                                     ExtendedSignLattice eval,
                                     boolean idOnLeft) {
        ExtendedSignLattice l = idOnLeft ? atom : eval;
        ExtendedSignLattice r = idOnLeft ? eval : atom;

        if (op == ComparisonEq.INSTANCE) return l.eq(r);
        if (op == ComparisonGt.INSTANCE) return l.gt(r);
        if (op == ComparisonGe.INSTANCE) return l.gt(r).or(l.eq(r));
        if (op == ComparisonLt.INSTANCE) return l.gt(r).negate().and(l.eq(r).negate());
        if (op == ComparisonLe.INSTANCE) return l.gt(r).negate();
        if (op == ComparisonNe.INSTANCE) return l.eq(r).negate();
        return Satisfiability.UNKNOWN;
    }

}

 */