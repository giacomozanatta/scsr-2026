package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

    @Override
    public ExtendedSignLattice top() {
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return ExtendedSignLattice.BOTTOM;
    }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(constant.getValue() instanceof Integer n){
            if(n == 0)
                return ExtendedSignLattice.ZERO;
            if(n > 0)
                return ExtendedSignLattice.POS;
            return ExtendedSignLattice.NEG;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(expression.getOperator() != NumericNegation.INSTANCE){
            return ExtendedSignLattice.TOP;
        }

        if(arg == ExtendedSignLattice.POS) return ExtendedSignLattice.NEG;
        if(arg == ExtendedSignLattice.NOTNEG) return ExtendedSignLattice.NOTPOS;
        if(arg == ExtendedSignLattice.ZERO) return ExtendedSignLattice.ZERO;
        if(arg == ExtendedSignLattice.NOTPOS) return ExtendedSignLattice.NOTNEG;
        if(arg == ExtendedSignLattice.NEG) return ExtendedSignLattice.POS;

        if(arg == ExtendedSignLattice.NOTZERO) return ExtendedSignLattice.NOTZERO;
        if(arg == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice L, ExtendedSignLattice R, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        ExtendedSignLattice TOP = ExtendedSignLattice.TOP;
        ExtendedSignLattice BOT = ExtendedSignLattice.BOTTOM;
        ExtendedSignLattice POS = ExtendedSignLattice.POS;
        ExtendedSignLattice NOTNEG = ExtendedSignLattice.NOTNEG;
        ExtendedSignLattice ZERO = ExtendedSignLattice.ZERO;
        ExtendedSignLattice NOTPOS = ExtendedSignLattice.NOTPOS;
        ExtendedSignLattice NEG = ExtendedSignLattice.NEG;
        ExtendedSignLattice NOTZERO = ExtendedSignLattice.NOTZERO;

        if(expression.getOperator() instanceof AdditionOperator){
            if(L == BOT || R == BOT) return BOT;
            if(L == TOP || R == TOP) return TOP;

            /*if( (L == TOP || R == TOP)
                || (L == NEG && (R == POS || R == NOTNEG || R == NOTZERO))
                || (L == POS && (R == NEG || R == NOTPOS || R == NOTZERO))
                || (L == NOTZERO && (R == POS || R == NEG || R == NOTZERO || R == NOTPOS || R == NOTNEG) )
                || (L == NOTPOS && (R == NOTNEG || R == NOTZERO || R == POS) )
                || (L == NOTNEG && (R == NOTPOS || R == NOTZERO || R == NEG) )
            ){
                return TOP;
            } */

            if( (L == POS && R == POS)
                || ( (L == NOTNEG || R == NOTNEG ) && (L == POS || R == POS) )
                || ( (L == ZERO || R == ZERO ) && (L == POS || R == POS) )
            ){
                return POS;
            }

            if( L == ZERO && R == ZERO ){
                return ZERO;
            }

            if( (L == NEG && R == NEG)
                || ( (L == NOTPOS || R == NOTPOS) && (L == NEG || R == NEG) )
                || ( (L == ZERO || R == ZERO ) && (L == NEG || R == NEG) )
            ){
                return NEG;
            }

            if( (L == NOTNEG && R == NOTNEG)
                || ( (L == ZERO || R == ZERO ) && (L == NOTNEG || R == NOTNEG) )
            ){
                return NOTNEG;
            }
            if( (L == NOTPOS && R == NOTPOS)
                || ( (L == ZERO || R == ZERO ) && (L == NOTPOS || R == NOTPOS) )
            ){
                return NOTPOS;
            }
            if( ( (L == ZERO || R == ZERO ) && (L == NOTZERO || R == NOTZERO) )
            ){
                return NOTZERO;
            }

            return TOP;
            // At end of function we default to returning TOP
        }
        else if(expression.getOperator() instanceof SubtractionOperator){
            if(L == BOT || R == BOT) return BOT;
            if(L == TOP || R == TOP) return TOP;

            if( ( L == NOTPOS && (R == NOTNEG || R == ZERO) )
            || ( L == ZERO && R == NOTNEG )
            ){
                return NOTPOS;
            }
            else if( (L == NEG && (R == ZERO || R == POS))
                  || (L == ZERO && (R == POS))
            ){
                return NEG;
            }
            else if(L == ZERO && R == ZERO){
                return ZERO;
            }
            else if( (L == NOTNEG && (R == NOTPOS || R == ZERO))
                  || (L == ZERO && (R == NOTPOS))
            ){
                return NOTNEG;
            }
            else if( (L == POS && (R == ZERO || R == NEG))
                  || (L == ZERO && (R == NEG))
            ){
                return POS;
            }
            else if( (L == NOTZERO || R == NOTZERO) && (L == ZERO || R == ZERO)
            ){
                return NOTZERO;
            }


            return TOP;
        }
        else if(expression.getOperator() instanceof MultiplicationOperator){
            if(L == BOT || R == BOT) return BOT;
            if(L == TOP || R == TOP)
                return TOP;

            if((L == POS || R == POS) && (L == NEG || R == NEG)
            ){
                return NEG;
            }
            else if(
                    ((L == POS || R == POS) && (L == NOTPOS || R == NOTPOS))
                 || ((L == NEG || R == NEG) && (L == NOTNEG || R == NOTNEG))
            ){
                return NOTPOS;
            }
            else if( (L == ZERO || R == ZERO)
            ){
                return ZERO;
            }
            else if((L == POS || R == POS) && (L == NOTNEG || R == NOTNEG)
                 || (L == NOTNEG && R == NOTNEG)
                 || (L == NEG || R == NEG) && (L == NOTPOS || R == NOTPOS)
            ){
                return NOTNEG;
            }
            else if( (L == POS && R == POS) || (L == NEG && R == NEG)){
                return POS;
            }
            else if((L == NOTZERO || R == NOTZERO)){
                return NOTZERO;
            }

            return TOP;

        }
        else if(expression.getOperator() instanceof DivisionOperator){
            if(L == BOT || R == BOT || R == ZERO || R == NOTNEG || R == NOTPOS) return BOT;
            if(L == TOP || R == TOP) return TOP;

            if((L == POS || R == POS) && (L == NEG || R == NEG)
            ){
                return NEG;
            }
            else if(
                    (L == NOTPOS && R == POS) || (L == NOTNEG && R == NEG)
            ){
                return NOTPOS;
            }
            else if( L == ZERO ){
                return ZERO;
            }
            else if(
                    (L == NOTPOS && R == NEG) || (L == NOTNEG && R == POS)
            ){
                return NOTNEG;
            }
            else if(
                    (L == POS && R == POS) || (L == NEG && R == NEG)
            ){
                return POS;
            }
            else if(L == NOTZERO || R == NOTZERO){
                return NOTZERO;
            }

            return TOP;
        }
        return TOP;
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
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
    public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED)
            return environment.bottom();
        if (sat == Satisfiability.SATISFIED)
            return environment;

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
        } else
            return environment;

        ExtendedSignLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom())
            return environment.bottom();

        ExtendedSignLattice update = null;
        if (operator == ComparisonEq.INSTANCE)
            update = starting.glb(eval);
        else {
            // the rule for an operator op is:
            // - if `start op eval`, `update = U { start n v | v op eval, v in {
            // +, 0, -} }`
            // - if `eval op start`, `update = U { start n v | eval op v, v in {
            // +, 0, -} }`

            ExtendedSignLattice[] all = new ExtendedSignLattice[] { ExtendedSignLattice.NEG, ExtendedSignLattice.ZERO, ExtendedSignLattice.POS,
            ExtendedSignLattice.NOTNEG, ExtendedSignLattice.NOTPOS, ExtendedSignLattice.NOTZERO};
            if (operator == ComparisonGe.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        if (s.gt(eval).or(s.eq(eval)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        if (eval.gt(s).or(eval.eq(s)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLe.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (s.gt(eval).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (eval.gt(s).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLt.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (eval.gt(s).or(eval.eq(s)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonGt.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        if (s.gt(eval).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
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
