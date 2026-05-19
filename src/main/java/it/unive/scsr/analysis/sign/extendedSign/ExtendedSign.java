package it.unive.scsr.analysis.sign.extendedSign;

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

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {
    @Override
    public ExtendedSignLattice top() { return ExtendedSignLattice.TOP; }

    @Override
    public ExtendedSignLattice bottom() { return ExtendedSignLattice.BOTTOM; }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException{
        if(constant.getValue() instanceof Integer){
            Integer n = (Integer) constant.getValue();
            if(n == 0) return ExtendedSignLattice.ZERO;
            else if(n > 0) return ExtendedSignLattice.POS;
            return ExtendedSignLattice.NEG;
        }
        return ExtendedSignLattice.TOP;
    }

    private ExtendedSignLattice negSign(ExtendedSignLattice arg){
        if(arg.equals(ExtendedSignLattice.NEG)) return ExtendedSignLattice.POS;
        if(arg.equals(ExtendedSignLattice.POS)) return ExtendedSignLattice.NEG;
        if(arg.equals(ExtendedSignLattice.NEQZERO)) return ExtendedSignLattice.NEQZERO;
        if(arg.equals(ExtendedSignLattice.GEQZERO)) return ExtendedSignLattice.LEQZERO;
        if(arg.equals(ExtendedSignLattice.LEQZERO)) return ExtendedSignLattice.GEQZERO;
        if(arg.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
        return arg;
    }

    private ExtendedSignLattice computeAddition(ExtendedSignLattice left, ExtendedSignLattice right){
        if(right.equals(ExtendedSignLattice.ZERO)) return left;
        if(left.equals(ExtendedSignLattice.ZERO)) return right;
        if(right.equals(ExtendedSignLattice.POS) && (left.equals(ExtendedSignLattice.POS) || left.equals(ExtendedSignLattice.GEQZERO))) return ExtendedSignLattice.POS;
        if(left.equals(ExtendedSignLattice.POS) && (right.equals(ExtendedSignLattice.POS) || right.equals(ExtendedSignLattice.GEQZERO))) return ExtendedSignLattice.POS;
        if(right.equals(ExtendedSignLattice.NEG) && (left.equals(ExtendedSignLattice.NEG) || left.equals(ExtendedSignLattice.LEQZERO))) return ExtendedSignLattice.NEG;
        if(left.equals(ExtendedSignLattice.NEG) && (right.equals(ExtendedSignLattice.NEG) || right.equals(ExtendedSignLattice.LEQZERO))) return ExtendedSignLattice.NEG;
        if(right.equals(ExtendedSignLattice.GEQZERO) && left.equals(ExtendedSignLattice.GEQZERO)) return ExtendedSignLattice.GEQZERO;
        if(right.equals(ExtendedSignLattice.LEQZERO) && left.equals(ExtendedSignLattice.LEQZERO)) return ExtendedSignLattice.LEQZERO;
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException  {
        if(expression.getOperator() == NumericNegation.INSTANCE) {
            return negSign(arg);
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right,  ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(right.isBottom() || left.isBottom()) return ExtendedSignLattice.BOTTOM;

        if(expression.getOperator() instanceof AdditionOperator) return computeAddition(left, right);
        if(expression.getOperator() instanceof SubtractionOperator) return computeAddition(left, negSign(right));
        if(expression.getOperator() instanceof MultiplicationOperator){
            if(left.equals(ExtendedSignLattice.ZERO) || right.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
            if(left.equals(ExtendedSignLattice.POS)) return right;
            if(right.equals(ExtendedSignLattice.POS)) return left;
            if(left.equals(ExtendedSignLattice.NEG)) return negSign(right);
            if(right.equals(ExtendedSignLattice.NEG)) return negSign(left);
            if(left.equals(ExtendedSignLattice.NEQZERO) && right.equals(ExtendedSignLattice.NEQZERO)) return ExtendedSignLattice.NEQZERO;
            if(left.equals(ExtendedSignLattice.GEQZERO) && right.equals(ExtendedSignLattice.GEQZERO)) return ExtendedSignLattice.GEQZERO;
            if(left.equals(ExtendedSignLattice.LEQZERO) && right.equals(ExtendedSignLattice.LEQZERO)) return ExtendedSignLattice.GEQZERO;
            return ExtendedSignLattice.TOP;
        }
        if(expression.getOperator() instanceof DivisionOperator){
            if(left.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.ZERO;
            if(right.equals(ExtendedSignLattice.ZERO)) return ExtendedSignLattice.BOTTOM;
            if((left.equals(ExtendedSignLattice.POS) && right.equals(ExtendedSignLattice.POS)) || (left.equals(ExtendedSignLattice.NEG) && right.equals(ExtendedSignLattice.NEG))) return ExtendedSignLattice.GEQZERO;
            if((left.equals(ExtendedSignLattice.NEG) && right.equals(ExtendedSignLattice.POS)) || (left.equals(ExtendedSignLattice.POS) && right.equals(ExtendedSignLattice.NEG))) return ExtendedSignLattice.LEQZERO;
            return ExtendedSignLattice.TOP;
        }
        if(expression.getOperator() instanceof ModuloOperator) return right;
        if(expression.getOperator() instanceof RemainderOperator) return left;
        if(right.isTop() || left.isTop()) return ExtendedSignLattice.TOP;
        return ExtendedSignLattice.TOP;
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) {
        if (left.isTop() || right.isTop()) return Satisfiability.UNKNOWN;

        BinaryOperator operator = expression.getOperator();
        if (operator == ComparisonEq.INSTANCE) return left.eq(right);
        if (operator == ComparisonGe.INSTANCE) return left.eq(right).or(left.gt(right));
        if (operator == ComparisonGt.INSTANCE) return left.gt(right);
        if (operator == ComparisonLe.INSTANCE) return left.gt(right).negate();
        if (operator == ComparisonLt.INSTANCE) return left.gt(right).negate().and(left.eq(right).negate());
        if (operator == ComparisonNe.INSTANCE) return left.eq(right).negate();
        return Satisfiability.UNKNOWN;
    }

    @Override
    public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED) return environment.bottom();
        if (sat == Satisfiability.SATISFIED) return environment;

        Identifier id;
        ExtendedSignLattice eval;
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
        } else {
            return environment;
        }

        ExtendedSignLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom()) return environment.bottom();

        ExtendedSignLattice update = starting;

        if (eval.equals(ExtendedSignLattice.ZERO)) {
            if (operator == ComparisonEq.INSTANCE) update = starting.glb(ExtendedSignLattice.ZERO);
            else if (operator == ComparisonNe.INSTANCE) update = starting.glb(ExtendedSignLattice.NEQZERO);
            else if (operator == ComparisonGe.INSTANCE) update = rightIsExpr ? starting.glb(ExtendedSignLattice.GEQZERO) : starting.glb(ExtendedSignLattice.LEQZERO);
            else if (operator == ComparisonLe.INSTANCE) update = rightIsExpr ? starting.glb(ExtendedSignLattice.LEQZERO) : starting.glb(ExtendedSignLattice.GEQZERO);
            else if (operator == ComparisonGt.INSTANCE) update = rightIsExpr ? starting.glb(ExtendedSignLattice.POS) : starting.glb(ExtendedSignLattice.NEG);
            else if (operator == ComparisonLt.INSTANCE) update = rightIsExpr ? starting.glb(ExtendedSignLattice.NEG) : starting.glb(ExtendedSignLattice.POS);
        }
        else if (eval.equals(ExtendedSignLattice.POS)) {
            if (operator == ComparisonEq.INSTANCE) update = starting.glb(ExtendedSignLattice.POS);
            else if (operator == ComparisonGe.INSTANCE || operator == ComparisonGt.INSTANCE) {
                if (rightIsExpr) update = starting.glb(ExtendedSignLattice.POS);
            } else if (operator == ComparisonLe.INSTANCE || operator == ComparisonLt.INSTANCE) {
                if (!rightIsExpr) update = starting.glb(ExtendedSignLattice.POS); // Copertura totale!
            }
        }
        else if (eval.equals(ExtendedSignLattice.NEG)) {
            if (operator == ComparisonEq.INSTANCE) update = starting.glb(ExtendedSignLattice.NEG);
            else if (operator == ComparisonLe.INSTANCE || operator == ComparisonLt.INSTANCE) {
                if (rightIsExpr) update = starting.glb(ExtendedSignLattice.NEG);
            } else if (operator == ComparisonGe.INSTANCE || operator == ComparisonGt.INSTANCE) {
                if (!rightIsExpr) update = starting.glb(ExtendedSignLattice.NEG); // Copertura totale!
            }
        }

        if (update.isBottom()) return environment.bottom();
        return environment.putState(id, update);
    }
}
