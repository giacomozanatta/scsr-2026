/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.unive.scsr.analysis.cartesian;

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
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import it.unive.scsr.analysis.sign.extended.ExtendedSign;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;

/**
 *
 * @author brauny
 */
public class CartesianSignTaint implements BaseNonRelationalValueDomain<CartesianSignTaintLattice> {

    private TaintThreeLevels taint = new TaintThreeLevels();
    private ExtendedSign sign = new ExtendedSign();
    
    @Override
    public CartesianSignTaintLattice evalUnaryExpression(UnaryExpression expression, CartesianSignTaintLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException 
    {
        var signL = sign.evalUnaryExpression(expression, arg.sign, pp, oracle);
        return new CartesianSignTaintLattice(arg.taint, signL);
    }

    @Override
    public CartesianSignTaintLattice evalBinaryExpression(BinaryExpression expression, CartesianSignTaintLattice left, CartesianSignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException 
    {
        if (left.isBottom() || right.isBottom())
        {
            return CartesianSignTaintLattice.BOTTOM;
        }
        ExtendedSignLattice signL = sign.evalBinaryExpression(expression, left.sign, right.sign, pp, oracle);
        TaintThreeLevelsLattice taintL = taint.evalBinaryExpression(expression, left.taint, right.taint, pp, oracle);
        return new CartesianSignTaintLattice(taintL, signL);
    }

    @Override
    public CartesianSignTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException 
    {
        ExtendedSignLattice signL = sign.evalConstant(constant, pp, oracle);
        return new CartesianSignTaintLattice(TaintThreeLevelsLattice.CLEAN, signL);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, CartesianSignTaintLattice left, CartesianSignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException
    {
        return sign.satisfiesBinaryExpression(expression, left.sign, right.sign, pp, oracle);
    }
    
    @Override
    public ValueEnvironment<CartesianSignTaintLattice> assumeBinaryExpression(ValueEnvironment<CartesianSignTaintLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException
    {
        Satisfiability sat = satisfies(environment, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED)
            return environment.bottom();
        if (sat == Satisfiability.SATISFIED)
            return environment;
        Identifier id;
        CartesianSignTaintLattice eval;
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
        
        CartesianSignTaintLattice starting = environment.getState(id);
        if (eval.isBottom() || starting.isBottom())
            return environment.bottom();
        
        CartesianSignTaintLattice update = null;
        if (operator == ComparisonEq.INSTANCE)
            update = starting.glb(eval);
        else 
        {
            CartesianSignTaintLattice[] all = new CartesianSignTaintLattice[]
            { 
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.CLEAN, ExtendedSignLattice.NOTPOS),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.CLEAN, ExtendedSignLattice.NOTNEG),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.CLEAN, ExtendedSignLattice.NOTZERO),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.CLEAN, ExtendedSignLattice.POS),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.CLEAN, ExtendedSignLattice.ZERO),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.CLEAN, ExtendedSignLattice.NEG),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.TAINT, ExtendedSignLattice.NOTPOS),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.TAINT, ExtendedSignLattice.NOTNEG),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.TAINT, ExtendedSignLattice.NOTZERO),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.TAINT, ExtendedSignLattice.POS),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.TAINT, ExtendedSignLattice.ZERO),
                new CartesianSignTaintLattice(TaintThreeLevelsLattice.TAINT, ExtendedSignLattice.NEG)
            };
            if (operator == ComparisonGe.INSTANCE)
                if (rightIsExpr) {
                    for (CartesianSignTaintLattice s : all)
                        if (s.gt(eval).or(s.eq(eval)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (CartesianSignTaintLattice s : all)
                        if (eval.gt(s).or(eval.eq(s)).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLe.INSTANCE)
                if (rightIsExpr) {
                    for (CartesianSignTaintLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (s.gt(eval).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (CartesianSignTaintLattice s : all)
                        // we invert <= to > and look at the failing ones
                        if (eval.gt(s).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLt.INSTANCE)
                if (rightIsExpr) {
                    for (CartesianSignTaintLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (CartesianSignTaintLattice s : all)
                        // we invert < to >= and look at the failing ones
                        if (eval.gt(s).or(eval.eq(s)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonGt.INSTANCE)
                if (rightIsExpr) {
                    for (CartesianSignTaintLattice s : all)
                        if (s.gt(eval).mightBeTrue())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (CartesianSignTaintLattice s : all)
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
    
    @Override
    public CartesianSignTaintLattice top() {
        return CartesianSignTaintLattice.TOP;
    }

    @Override
    public CartesianSignTaintLattice bottom() {
        return CartesianSignTaintLattice.BOTTOM;
    }
    
}
