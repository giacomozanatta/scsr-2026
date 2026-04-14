package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.scsr.analysis.parity.ParityLattice;

public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

	@Override
    public ParityLattice top() { return ParityLattice.TOP; }

    @Override
    public ParityLattice bottom() { return ParityLattice.BOTTOM; }

    @Override
    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(constant.getValue() instanceof Integer n) {
            if(n % 2 == 0)
                return ParityLattice.EVEN;
            else
                return ParityLattice.ODD;
        }
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(expression.getOperator() == NumericNegation.INSTANCE)
            // an EVEN number is still EVEN after the negation; same for ODD
            return arg;

        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(left == ParityLattice.TOP || right == ParityLattice.TOP)
            return ParityLattice.TOP;
        else if(left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM)
            return ParityLattice.BOTTOM;

        if(expression.getOperator() instanceof AdditionOperator) {
            // if left and right are both EVENs or ODDs, then their sum is EVEN or ODD too
            return left.equals(right) ? ParityLattice.EVEN : ParityLattice.ODD;
        }
        else if(expression.getOperator() instanceof SubtractionOperator) {
            // if left and right are both EVENs or ODDs, then their subtraction is EVEN or ODD too
            return left.equals(right) ? ParityLattice.EVEN : ParityLattice.ODD;
        }
        else if(expression.getOperator() instanceof MultiplicationOperator) {
            if(left == ParityLattice.EVEN || right == ParityLattice.EVEN)
                return ParityLattice.EVEN;
            return ParityLattice.ODD;
        }
        else if(expression.getOperator() instanceof DivisionOperator)
            // we can't determine is a division is EVEN or ODD; it depends on the two factors...
            return ParityLattice.TOP;
        else if(expression.getOperator() instanceof ModuloOperator)
            return ParityLattice.TOP;
        else if(expression.getOperator() instanceof RemainderOperator)
            return ParityLattice.TOP;

        return ParityLattice.TOP;
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right, ProgramPoint pp, SemanticOracle oracle) {
        if (left.isTop() || right.isTop())
            return Satisfiability.UNKNOWN;

        BinaryOperator operator = expression.getOperator();
        if (operator == ComparisonEq.INSTANCE)
            // x = y
            return left.eq(right);
        else if (operator == ComparisonGe.INSTANCE)
            // x >= y
            return left.eq(right).or(left.gt(right));
        else if (operator == ComparisonGt.INSTANCE)
            // x > y
            return left.gt(right);
        else if (operator == ComparisonLe.INSTANCE)
            // e1 <= e2 same as !(e1 > e2)
            return left.gt(right).negate();
        else if (operator == ComparisonLt.INSTANCE)
            // e1 < e2 -> !(e1 >= e2) && !(e1 == e2)
            return left.gt(right).negate().and(left.eq(right).negate());
        else if (operator == ComparisonNe.INSTANCE)
            // x != y
            return left.eq(right).negate();
        else
            return Satisfiability.UNKNOWN;
    }

    @Override
    public ValueEnvironment<ParityLattice> assumeBinaryExpression(ValueEnvironment<ParityLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED)
            return environment.bottom();
        if (sat == Satisfiability.SATISFIED)
            return environment;

        Identifier id;
        ParityLattice eval;
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

        ParityLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom())
            return environment.bottom();

        ParityLattice update = null;
        if (operator == ComparisonEq.INSTANCE)
            update = starting.glb(eval);
        else {
            // the rule for an operator op is:
            // - if `start op eval`, `update = U { start n v | v op eval, v in {
            // +, 0, -} }`
            // - if `eval op start`, `update = U { start n v | eval op v, v in {
            // +, 0, -} }`

            ParityLattice[] all = new ParityLattice[] { ParityLattice.EVEN, ParityLattice.ODD };
            if (operator == ComparisonGe.INSTANCE)
                if (rightIsExpr) {
                    for (ParityLattice s : all)
                        if (s.gt(eval).or(s.eq(eval)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ParityLattice s : all)
                        if (eval.gt(s).or(eval.eq(s)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLe.INSTANCE)
                if (rightIsExpr) {
                    for (ParityLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (s.gt(eval).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ParityLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (eval.gt(s).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLt.INSTANCE)
                if (rightIsExpr) {
                    for (ParityLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ParityLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (eval.gt(s).or(eval.eq(s)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonGt.INSTANCE)
                if (rightIsExpr) {
                    for (ParityLattice s : all)
                        if (s.gt(eval).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ParityLattice s : all)
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