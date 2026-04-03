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
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class Sign implements BaseNonRelationalValueDomain<SignLattice>{

    @Override
    public SignLattice top() {
        return SignLattice.TOP;
    }

    @Override
    public SignLattice bottom() {
        return SignLattice.BOTTOM;
    }

    @Override
    public SignLattice
    evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {

        if(constant.getValue() instanceof Integer) {
            //I needt to check the integer value to
            // assign the right approx value
            Integer n = (Integer) constant.getValue();
            if(n == 0)
                return SignLattice.ZERO;
            else if(n > 0)
                return SignLattice.POS;
            return SignLattice.NEG;
        }

        return SignLattice.TOP;
    }

    @Override
    public SignLattice evalUnaryExpression(UnaryExpression expression, SignLattice arg, ProgramPoint pp,
                                           SemanticOracle oracle) throws SemanticException {

        if(expression.getOperator() == NumericNegation.INSTANCE) {
            if(arg == SignLattice.NEG)
                return SignLattice.POS;
            else if(arg == SignLattice.POS)
                return SignLattice.NEG;
            else if(arg == SignLattice.ZERO)
                return SignLattice.ZERO;
            else if(arg == SignLattice.TOP)
                return SignLattice.TOP;
            else if(arg == SignLattice.BOTTOM)
                return SignLattice.BOTTOM;
        }

        return SignLattice.TOP;
    }

    @Override
    public SignLattice evalBinaryExpression(BinaryExpression expression, SignLattice left, SignLattice right,
                                            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if(expression.getOperator() instanceof AdditionOperator) {
            if(left == SignLattice.POS && right == SignLattice.NEG
                    || left == SignLattice.NEG && right == SignLattice.POS)
                return SignLattice.TOP;
            if(left == SignLattice.POS && (right == SignLattice.POS || right == SignLattice.ZERO)
                    || (left == SignLattice.POS || left == SignLattice.ZERO) && right == SignLattice.POS)
                return SignLattice.POS;
            if(left == SignLattice.NEG && (right == SignLattice.NEG || right == SignLattice.ZERO)
                    || (left == SignLattice.NEG || left == SignLattice.ZERO) && right == SignLattice.NEG)
                return SignLattice.NEG;
            if(left == SignLattice.ZERO && right == SignLattice.ZERO)
                return SignLattice.ZERO;
            if(left == SignLattice.BOTTOM || right == SignLattice.BOTTOM)
                return SignLattice.BOTTOM;
            if(left == SignLattice.TOP || right == SignLattice.TOP)
                return SignLattice.TOP;

        } else if (expression.getOperator() instanceof MultiplicationOperator) {
            if(left == SignLattice.POS && right == SignLattice.POS)
                return SignLattice.POS;
            else if(left == SignLattice.POS  && right == SignLattice.NEG
                    || left == SignLattice.NEG  && right == SignLattice.POS)
                return  SignLattice.NEG;
            else if(left == SignLattice.NEG && right == SignLattice.NEG)
                return SignLattice.POS;
            else if(left == SignLattice.BOTTOM || right == SignLattice.BOTTOM)
                return SignLattice.BOTTOM;
            else if(left == SignLattice.ZERO || right == SignLattice.ZERO)
                // this handles also the case ZERO mul TOP and TOP mul ZERO
                return SignLattice.ZERO;
            else if(left == SignLattice.TOP || right == SignLattice.TOP)
                return SignLattice.TOP;
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            // TODO: homework
        } else if (expression.getOperator() instanceof DivisionOperator) {
            // TODO: homework
        } else if (expression.getOperator() instanceof ModuloOperator)
            return right;
        else if (expression.getOperator() instanceof RemainderOperator)
            return left;

        return SignLattice.TOP;
    }

    // Some information can be inferred also checking boolean expression and guards!
    // a = T, b = +; b < a means that  a = +
    // These behaviors can be handeled with the following implementations

    @Override
    public Satisfiability satisfiesBinaryExpression(
            BinaryExpression expression,
            SignLattice left,
            SignLattice right,
            ProgramPoint pp,
            SemanticOracle oracle) {
        if (left.isTop() || right.isTop())
            return Satisfiability.UNKNOWN;

        BinaryOperator operator = expression.getOperator();
        if (operator == ComparisonEq.INSTANCE)
            return left.eq(right);
        else if (operator == ComparisonGe.INSTANCE)
            return left.eq(right).or(left.gt(right));
        else if (operator == ComparisonGt.INSTANCE)
            return left.gt(right);
        else if (operator == ComparisonLe.INSTANCE)
            // e1 <= e2 same as !(e1 > e2)
            return left.gt(right).negate();
        else if (operator == ComparisonLt.INSTANCE)
            // e1 < e2 -> !(e1 >= e2) && !(e1 == e2)
            return left.gt(right).negate().and(left.eq(right).negate());
        else if (operator == ComparisonNe.INSTANCE)
            return left.eq(right).negate();
        else
            return Satisfiability.UNKNOWN;
    }

    @Override
    public ValueEnvironment<SignLattice> assumeBinaryExpression(
            ValueEnvironment<SignLattice> environment,
            BinaryExpression expression,
            ProgramPoint src,
            ProgramPoint dest,
            SemanticOracle oracle)
            throws SemanticException {
        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED)
            return environment.bottom();
        if (sat == Satisfiability.SATISFIED)
            return environment;

        Identifier id;
        SignLattice eval;
        boolean rightIsExpr;
        BinaryOperator operator = expression.getOperator();
        ValueExpression left = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();
        if (left instanceof Identifier) {
            eval = eval(environment, right, src, oracle);
            id = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            eval = eval(environment, left, src, oracle);
            id = (Identifier) right;
            rightIsExpr = false;
        } else
            return environment;

        SignLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom())
            return environment.bottom();

        SignLattice update = null;
        if (operator == ComparisonEq.INSTANCE)
            update = starting.glb(eval);
        else {
            // the rule for an operator op is:
            // - if `start op eval`, `update = U { start n v | v op eval, v in {
            // +, 0, -} }`
            // - if `eval op start`, `update = U { start n v | eval op v, v in {
            // +, 0, -} }`

            SignLattice[] all = new SignLattice[] { SignLattice.NEG, SignLattice.ZERO, SignLattice.POS };
            if (operator == ComparisonGe.INSTANCE)
                if (rightIsExpr) {
                    for (SignLattice s : all)
                        if (s.gt(eval).or(s.eq(eval)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (SignLattice s : all)
                        if (eval.gt(s).or(eval.eq(s)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLe.INSTANCE)
                if (rightIsExpr) {
                    for (SignLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (s.gt(eval).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (SignLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (eval.gt(s).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLt.INSTANCE)
                if (rightIsExpr) {
                    for (SignLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (SignLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (eval.gt(s).or(eval.eq(s)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonGt.INSTANCE)
                if (rightIsExpr) {
                    for (SignLattice s : all)
                        if (s.gt(eval).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (SignLattice s : all)
                        if (eval.gt(s).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
        }

        if (update == null)
            return environment;
        else if (update.isBottom())
            return environment.bottom();
        else
            return environment.putState(id, update);
    }

}