package it.unive.scsr.analysis.sign;

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
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

    @Override public ExtendedSignLattice top()    { return ExtendedSignLattice.TOP; }
    @Override public ExtendedSignLattice bottom() { return ExtendedSignLattice.BOTTOM; }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int n = (Integer) constant.getValue();
            if (n > 0) return ExtendedSignLattice.POS;
            if (n < 0) return ExtendedSignLattice.NEG;
            return ExtendedSignLattice.ZERO;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE)
            return arg.negate();
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
            ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left.isBottom() || right.isBottom())
            return ExtendedSignLattice.BOTTOM;

        if (expression.getOperator() instanceof AdditionOperator)
            return add(left, right);
        if (expression.getOperator() instanceof SubtractionOperator)
            return add(left, right.negate());
        if (expression.getOperator() instanceof MultiplicationOperator)
            return mul(left, right);
        if (expression.getOperator() instanceof DivisionOperator)
            return div(left, right);

        return ExtendedSignLattice.TOP;
    }

    private static ExtendedSignLattice add(ExtendedSignLattice l, ExtendedSignLattice r) {
        boolean ln = l.hasNeg(), lz = l.hasZero(), lp = l.hasPos();
        boolean rn = r.hasNeg(), rz = r.hasZero(), rp = r.hasPos();
        boolean mixed = (ln && rp) || (lp && rn);
        boolean resNeg  = (ln && (rn || rz)) || (lz && rn) || mixed;
        boolean resZero = (lz && rz)         || mixed;
        boolean resPos  = (lp && (rp || rz)) || (lz && rp) || mixed;
        return ExtendedSignLattice.fromAtoms(resNeg, resZero, resPos);
    }

    private static ExtendedSignLattice mul(ExtendedSignLattice l, ExtendedSignLattice r) {
        boolean ln = l.hasNeg(), lz = l.hasZero(), lp = l.hasPos();
        boolean rn = r.hasNeg(), rz = r.hasZero(), rp = r.hasPos();
        boolean resNeg  = (ln && rp) || (lp && rn);
        boolean resZero = lz || rz;
        boolean resPos  = (ln && rn) || (lp && rp);
        return ExtendedSignLattice.fromAtoms(resNeg, resZero, resPos);
    }

    private static ExtendedSignLattice div(ExtendedSignLattice l, ExtendedSignLattice r) {
        if (r == ExtendedSignLattice.ZERO) return ExtendedSignLattice.BOTTOM;
        boolean ln = l.hasNeg(), lz = l.hasZero(), lp = l.hasPos();
        boolean rn = r.hasNeg(), rp = r.hasPos();
        if (!rn && !rp) return ExtendedSignLattice.BOTTOM;
        boolean resNeg  = (ln && rp) || (lp && rn);
        boolean resZero = lz || (ln && rn) || (ln && rp) || (lp && rn) || (lp && rp);
        boolean resPos  = (ln && rn) || (lp && rp);
        return ExtendedSignLattice.fromAtoms(resNeg, resZero, resPos);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left,
            ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) {
        if (left.isTop() || right.isTop()) return Satisfiability.UNKNOWN;
        BinaryOperator op = expression.getOperator();
        if (op == ComparisonEq.INSTANCE) return left.eq(right);
        if (op == ComparisonGe.INSTANCE) return left.eq(right).or(left.gt(right));
        if (op == ComparisonGt.INSTANCE) return left.gt(right);
        if (op == ComparisonLe.INSTANCE) return left.gt(right).negate();
        if (op == ComparisonLt.INSTANCE) return left.gt(right).negate().and(left.eq(right).negate());
        if (op == ComparisonNe.INSTANCE) return left.eq(right).negate();
        return Satisfiability.UNKNOWN;
    }

    @Override
    public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(
            ValueEnvironment<ExtendedSignLattice> environment,
            BinaryExpression expression,
            ProgramPoint src, ProgramPoint dest,
            SemanticOracle oracle) throws SemanticException {
        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED) return environment.bottom();
        if (sat == Satisfiability.SATISFIED)     return environment;

        Identifier id;
        ExtendedSignLattice eval;
        boolean rightIsExpr;
        BinaryOperator op = expression.getOperator();
        ValueExpression left  = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();

        if (left instanceof Identifier) {
            eval = eval(environment, right, src, oracle);
            id = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            eval = eval(environment, left, src, oracle);
            id = (Identifier) right;
            rightIsExpr = false;
        } else {
            return environment;
        }

        ExtendedSignLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom()) return environment.bottom();

        ExtendedSignLattice[] atoms = { ExtendedSignLattice.NEG, ExtendedSignLattice.ZERO, ExtendedSignLattice.POS };
        ExtendedSignLattice update = null;

        if (op == ComparisonEq.INSTANCE) {
            update = starting.glb(eval);
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
                    ExtendedSignLattice candidate = starting.glb(s);
                    update = update == null ? candidate : update.lub(candidate);
                }
            }
        }

        if (update == null)    return environment;
        if (update.isBottom()) return environment.bottom();
        return environment.putState(id, update);
    }
}
