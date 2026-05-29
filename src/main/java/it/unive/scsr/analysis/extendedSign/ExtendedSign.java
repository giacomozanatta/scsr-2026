package it.unive.scsr.analysis.extendedSign;

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
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

   
    @Override
    public ExtendedSignLattice top() { return ExtendedSignLattice.TOP; }

    @Override
    public ExtendedSignLattice bottom() { return ExtendedSignLattice.BOTTOM; }

    // -------------------------------------------------------------------------
    // Constant / expression evaluation
    // -------------------------------------------------------------------------

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        Object v = constant.getValue();
        if (v instanceof Integer) {
            int n = (Integer) v;
            if(n >= 0){
                if (n == 0) return ExtendedSignLattice.EQ0;
                return ExtendedSignLattice.GT0;
            }
            return ExtendedSignLattice.LT0;

            //return n > 0 ? ExtendedSignLattice.GT0 : (n == 0 ? ExtendedSignLattice.EQ0 : ExtendedSignLattice.LT0);
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg,
                                                   ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE){
            return negate(arg);
        }
        if (expression.getOperator() == Numeric32BitAdd.INSTANCE) return arg;

        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression,
                                                    ExtendedSignLattice left, ExtendedSignLattice right,
                                                    ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        if (expression.getOperator() instanceof AdditionOperator)       return add(left, right);
        if (expression.getOperator() instanceof SubtractionOperator)    return sub(left, right);
        if (expression.getOperator() instanceof MultiplicationOperator) return mul(left, right);
        if (expression.getOperator() instanceof DivisionOperator)       return div(left, right);
        if (expression.getOperator() instanceof ModuloOperator)         return right;
        if (expression.getOperator() instanceof RemainderOperator)      return left;
        return ExtendedSignLattice.TOP;
    }

    // -------------------------------------------------------------------------
    // Arithmetic helpers
    // Each operation is computed from the concrete-set semantics and is SOUND
    // (i.e., may over-approximate but never loses information).
    // -------------------------------------------------------------------------

    /** Unary minus: flips sign direction. */
    private ExtendedSignLattice negate(ExtendedSignLattice v) {
        if (v == ExtendedSignLattice.GT0)  return ExtendedSignLattice.LT0;
        if (v == ExtendedSignLattice.LT0)  return ExtendedSignLattice.GT0;
        if (v == ExtendedSignLattice.EQ0)  return ExtendedSignLattice.EQ0;
        if (v == ExtendedSignLattice.GEQ0) return ExtendedSignLattice.LEQ0;
        if (v == ExtendedSignLattice.LEQ0) return ExtendedSignLattice.GEQ0;
        if (v == ExtendedSignLattice.NEQ0) return ExtendedSignLattice.NEQ0;   // {pos,neg} negated = {neg,pos}
        return v;                    // ExtendedSignLattice.TOP and BOTTOM are fixed
    }

    /**
     * Addition truth table (derived from concrete sets).
     *
     * Key observations:
     *   EQ0 is the identity element (0 + x = x).
     *   pos + pos = pos,  neg + neg = neg,
     *   pos + neg = ExtendedSignLattice.TOP  (unknown sign),
     *   pos + GEQ0 = pos  (pos + any nonneg = pos),
     *   neg + LEQ0 = neg,
     *   GEQ0 + GEQ0 = GEQ0,  LEQ0 + LEQ0 = LEQ0.
     */
    private ExtendedSignLattice add(ExtendedSignLattice l, ExtendedSignLattice r) {
        if (l == ExtendedSignLattice.BOTTOM || r == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;
        if (l == ExtendedSignLattice.TOP || r == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;

        if (l == ExtendedSignLattice.EQ0) return r;       // 0 + x = x
        if (r == ExtendedSignLattice.EQ0) return l;       // x + 0 = x

        if (l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.GT0) return ExtendedSignLattice.GT0;
        if (l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.LT0) return ExtendedSignLattice.LT0;
        if ((l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.LT0) || (l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.GT0)) return ExtendedSignLattice.TOP;

        // pos + nonneg = pos  (pos + 0 = pos, pos + pos = pos)
        if ((l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.GEQ0) || (l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.GT0)) return ExtendedSignLattice.GT0;
        // neg + nonpos = neg
        if ((l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.LEQ0) || (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.LT0)) return ExtendedSignLattice.LT0;

        if (l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.GEQ0) return ExtendedSignLattice.GEQ0;
        if (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.LEQ0) return ExtendedSignLattice.LEQ0;

        // All remaining combinations involve mixed signs → ExtendedSignLattice.TOP
        return ExtendedSignLattice.TOP;
    }

    /** Subtraction via  l - r  =  l + (-r). */
    private ExtendedSignLattice sub(ExtendedSignLattice l, ExtendedSignLattice r) {
        return add(l, negate(r));
    }

    /**
     * Multiplication truth table (derived from concrete sets).
     *
     * Key observations:
     *   EQ0 is the absorbing element (0 * x = 0).
     *   Signs multiply like real-number signs.
     *   nonzero * nonzero = nonzero.
     */
    private ExtendedSignLattice mul(ExtendedSignLattice l, ExtendedSignLattice r) {
        if (l == ExtendedSignLattice.BOTTOM || r == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;
        if (l == ExtendedSignLattice.EQ0  || r == ExtendedSignLattice.EQ0)  return ExtendedSignLattice.EQ0;   // 0 * anything = 0
        if (l == ExtendedSignLattice.TOP || r == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;

        if (l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.GT0) return ExtendedSignLattice.GT0;
        if (l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.LT0) return ExtendedSignLattice.GT0;
        if ((l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.LT0) || (l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.GT0)) return ExtendedSignLattice.LT0;

        // pos * nonneg: pos*0=0, pos*pos=pos  → GEQ0
        if ((l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.GEQ0) || (l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.GT0)) return ExtendedSignLattice.GEQ0;
        // pos * nonpos: pos*0=0, pos*neg=neg  → LEQ0
        if ((l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.LEQ0) || (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.GT0)) return ExtendedSignLattice.LEQ0;
        // pos * nonzero: pos*pos=pos, pos*neg=neg  → NEQ0
        if ((l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.NEQ0) || (l == ExtendedSignLattice.NEQ0 && r == ExtendedSignLattice.GT0)) return ExtendedSignLattice.NEQ0;

        // neg * nonneg: neg*0=0, neg*pos=neg  → LEQ0
        if ((l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.GEQ0) || (l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.LT0)) return ExtendedSignLattice.LEQ0;
        // neg * nonpos: neg*0=0, neg*neg=pos  → GEQ0
        if ((l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.LEQ0) || (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.LT0)) return ExtendedSignLattice.GEQ0;
        // neg * nonzero: neg*pos=neg, neg*neg=pos  → NEQ0
        if ((l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.NEQ0) || (l == ExtendedSignLattice.NEQ0 && r == ExtendedSignLattice.LT0)) return ExtendedSignLattice.NEQ0;

        // nonneg * nonneg: 0*x=0, pos*pos=pos  → GEQ0
        if (l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.GEQ0) return ExtendedSignLattice.GEQ0;
        // nonneg * nonpos: 0*x=0, pos*neg=neg  → LEQ0
        if ((l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.LEQ0) || (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.GEQ0)) return ExtendedSignLattice.LEQ0;
        // nonpos * nonpos: 0*x=0, neg*neg=pos  → GEQ0
        if (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.LEQ0) return ExtendedSignLattice.GEQ0;

        // nonzero * nonzero: pos*pos=pos, neg*neg=pos, pos*neg=neg  → NEQ0
        if (l == ExtendedSignLattice.NEQ0 && r == ExtendedSignLattice.NEQ0) return ExtendedSignLattice.NEQ0;

        // Mixed ExtendedSignLattice.GEQ0/ExtendedSignLattice.NEQ0 or ExtendedSignLattice.LEQ0/ExtendedSignLattice.NEQ0: result spans {neg, 0, pos}  → ExtendedSignLattice.TOP
        return ExtendedSignLattice.TOP;
    }

    /**
     * Division truth table.
     *
     * If the divisor is definitely EQ0  → BOTTOM (path is infeasible / exception).
     * If the divisor might include 0    → ExtendedSignLattice.TOP    (safe over-approximation).
     * Otherwise compute precisely.
     */
    private ExtendedSignLattice div(ExtendedSignLattice l, ExtendedSignLattice r) {
        if (l == ExtendedSignLattice.BOTTOM || r == ExtendedSignLattice.BOTTOM) return ExtendedSignLattice.BOTTOM;
        if (r == ExtendedSignLattice.EQ0)  return ExtendedSignLattice.TOP;   // definite division by zero
        if (l == ExtendedSignLattice.EQ0)  return ExtendedSignLattice.EQ0;    // 0 / nonzero = 0
        if (l == ExtendedSignLattice.TOP || r == ExtendedSignLattice.TOP) return ExtendedSignLattice.TOP;

        // Divisors that cannot be zero: ExtendedSignLattice.GT0, ExtendedSignLattice.LT0, ExtendedSignLattice.NEQ0
        if (r == ExtendedSignLattice.GT0 || r == ExtendedSignLattice.LT0 || r == ExtendedSignLattice.NEQ0) {
            if (l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.GT0) return ExtendedSignLattice.GT0;
            if (l == ExtendedSignLattice.GT0 && r == ExtendedSignLattice.LT0) return ExtendedSignLattice.LT0;
            if (l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.GT0) return ExtendedSignLattice.LT0;
            if (l == ExtendedSignLattice.LT0 && r == ExtendedSignLattice.LT0) return ExtendedSignLattice.GT0;

            if (l == ExtendedSignLattice.NEQ0 || r == ExtendedSignLattice.NEQ0) return ExtendedSignLattice.NEQ0;

            if (l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.GT0) return ExtendedSignLattice.GEQ0;   // nonneg/pos
            if (l == ExtendedSignLattice.GEQ0 && r == ExtendedSignLattice.LT0) return ExtendedSignLattice.LEQ0;   // nonneg/neg
            if (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.GT0) return ExtendedSignLattice.LEQ0;   // nonpos/pos
            if (l == ExtendedSignLattice.LEQ0 && r == ExtendedSignLattice.LT0) return ExtendedSignLattice.GEQ0;   // nonpos/neg
        }

        // Divisors ExtendedSignLattice.GEQ0 / ExtendedSignLattice.LEQ0 might contain zero → over-approximate to ExtendedSignLattice.TOP
        return ExtendedSignLattice.TOP;
    }
}