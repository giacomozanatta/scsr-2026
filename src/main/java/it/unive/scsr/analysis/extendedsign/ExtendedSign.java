package it.unive.scsr.analysis.extendedsign;

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
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() == NumericNegation.INSTANCE) {
            if (arg == ExtendedSignLattice.NONZERONEG) {
                return ExtendedSignLattice.NONZEROPOS;
            }
            else if (arg == ExtendedSignLattice.NONZEROPOS) {
                return ExtendedSignLattice.NONZERONEG;
            }
            else if (arg == ExtendedSignLattice.ZEROPOS) {
                return ExtendedSignLattice.ZERONEG;
            }
            else if (arg == ExtendedSignLattice.ZERONEG) {
                return ExtendedSignLattice.ZEROPOS;
            }
            else {
                return arg;
            }
        }
        return null;
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (expression.getOperator() instanceof AdditionOperator) {
            // BOTTOM and TOP
            if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
                return ExtendedSignLattice.BOTTOM;
            }
            else if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
                return ExtendedSignLattice.TOP;
            }

            // Same sign
            if (left == right) {
                return left;
            }

            // Meaningful cases
            // L >= 0 || R >= 0
            if (left == ExtendedSignLattice.ZEROPOS || right == ExtendedSignLattice.ZEROPOS) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.ZEROPOS) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.ZERO) {
                    return ExtendedSignLattice.ZEROPOS;
                }
                else if (other == ExtendedSignLattice.ZERONEG) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.NONZEROPOS;
                }
            }
            // L == 0 || R == 0
            else if (left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO) {
                if (left == ExtendedSignLattice.ZERO) {
                    return right;
                }
                return left;
            }
            else if (left == ExtendedSignLattice.ZERONEG || right == ExtendedSignLattice.ZERONEG) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.ZERONEG) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.NONZERONEG;
                }
                else if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.TOP;
                }
            }
            else if (left == ExtendedSignLattice.NONZERONEG || right == ExtendedSignLattice.NONZERONEG) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.NONZERONEG) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.TOP;
                }
            }
            else if (left == ExtendedSignLattice.NONZEROPOS || right == ExtendedSignLattice.NONZEROPOS) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.NONZEROPOS) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
            }
            return ExtendedSignLattice.TOP;
        }
        else if (expression.getOperator() instanceof MultiplicationOperator) {
            if (left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO) {
                return ExtendedSignLattice.ZERO;
            }
            // BOTTOM and TOP
            if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
                return ExtendedSignLattice.BOTTOM;
            }
            else if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
                return ExtendedSignLattice.TOP;
            }
            // Meaningful cases
            if (left == ExtendedSignLattice.ZEROPOS || right == ExtendedSignLattice.ZEROPOS) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.ZEROPOS) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                return other.appendZero();
            }
            else if (left == ExtendedSignLattice.ZERONEG || right == ExtendedSignLattice.ZERONEG) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.ZERONEG) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                return other.appendZero().negate();
            }
            else if (left == ExtendedSignLattice.NONZERONEG || right == ExtendedSignLattice.NONZERONEG) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.NONZERONEG) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                return other.negate();
            }
            else if (left == ExtendedSignLattice.NONZEROPOS || right == ExtendedSignLattice.NONZEROPOS) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.NONZEROPOS) {
                    other = right;
                }
                else {
                    other = left;
                }
                // Other defined
                return other;
            }
            // left <> 0 && right <> 0
            return ExtendedSignLattice.TOP;
        }
        else if (expression.getOperator() instanceof DivisionOperator) {
            // BOTTOM and TOP
            if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
                return ExtendedSignLattice.BOTTOM;
            }
            else if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
                return ExtendedSignLattice.TOP;
            }
            // Meaningful cases
            if (right == ExtendedSignLattice.ZEROPOS ||
                right == ExtendedSignLattice.ZERO ||
                right == ExtendedSignLattice.ZERONEG) {
                return ExtendedSignLattice.BOTTOM;
            }
            // Right > 0, < 0, or <> 0
            if (left == ExtendedSignLattice.ZEROPOS) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.ZERONEG;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.ZEROPOS;
                }
                return ExtendedSignLattice.TOP;
            }
            else if (left == ExtendedSignLattice.ZERO) {
                return ExtendedSignLattice.ZERO;
            }
            else if (left == ExtendedSignLattice.ZERONEG) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.ZEROPOS;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.ZERONEG;
                }
                return ExtendedSignLattice.TOP;
            }
            else if (left == ExtendedSignLattice.NONZERONEG) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.NONZEROPOS;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.NONZERONEG;
                }
                return ExtendedSignLattice.NONZERO;
            }
            else if (left == ExtendedSignLattice.NONZEROPOS) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.NONZERONEG;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.NONZEROPOS;
                }
                return ExtendedSignLattice.NONZERO;
            }
            // left <> 0
            return ExtendedSignLattice.NONZERO;
        }
        else if (expression.getOperator() instanceof SubtractionOperator) {
            ExtendedSignLattice right_sub = right.negate();
            // BOTTOM and TOP
            if (left == ExtendedSignLattice.BOTTOM || right_sub == ExtendedSignLattice.BOTTOM) {
                return ExtendedSignLattice.BOTTOM;
            }
            else if (left == ExtendedSignLattice.TOP || right_sub == ExtendedSignLattice.TOP) {
                return ExtendedSignLattice.TOP;
            }

            // Same sign
            if (left == right_sub) {
                return left;
            }
            // Meaningful cases
            // L >= 0 || R >= 0
            if (left == ExtendedSignLattice.ZEROPOS || right_sub == ExtendedSignLattice.ZEROPOS) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.ZEROPOS) {
                    other = right_sub;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.ZERO) {
                    return ExtendedSignLattice.ZEROPOS;
                }
                else if (other == ExtendedSignLattice.ZERONEG) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.NONZEROPOS;
                }
            }
            else if (left == ExtendedSignLattice.ZERO || right_sub == ExtendedSignLattice.ZERO) {
                if (left == ExtendedSignLattice.ZERO) {
                    return right_sub;
                }
                return left;
            }
            else if (left == ExtendedSignLattice.ZERONEG || right_sub == ExtendedSignLattice.ZERONEG) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.ZERONEG) {
                    other = right_sub;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.NONZERONEG;
                }
                else if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.TOP;
                }
            }
            else if (left == ExtendedSignLattice.NONZERONEG || right_sub == ExtendedSignLattice.NONZERONEG) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.NONZERONEG) {
                    other = right_sub;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
                else if (other == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.TOP;
                }
            }
            else if (left == ExtendedSignLattice.NONZEROPOS || right_sub == ExtendedSignLattice.NONZEROPOS) {
                ExtendedSignLattice other;
                if (left == ExtendedSignLattice.NONZEROPOS) {
                    other = right_sub;
                }
                else {
                    other = left;
                }
                // Other defined
                if (other == ExtendedSignLattice.NONZERO) {
                    return ExtendedSignLattice.TOP;
                }
            }
            return ExtendedSignLattice.TOP;
        }
        else if (expression.getOperator() instanceof ModuloOperator) {
            // BOTTOM and TOP
            if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
                return ExtendedSignLattice.BOTTOM;
            }
            else if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
                return ExtendedSignLattice.TOP;
            }
            // Meaningful cases
            if (right == ExtendedSignLattice.ZEROPOS ||
                right == ExtendedSignLattice.ZERO ||
                right == ExtendedSignLattice.ZERONEG) {
                return ExtendedSignLattice.BOTTOM;
            }
            // Right > 0, < 0, or <> 0
            if (left == ExtendedSignLattice.ZEROPOS) {
                return ExtendedSignLattice.ZEROPOS;
            }
            else if (left == ExtendedSignLattice.ZERO) {
                return ExtendedSignLattice.ZERO;
            }
            else if (left == ExtendedSignLattice.ZERONEG) {
                return ExtendedSignLattice.ZEROPOS;
            }
            else if (left == ExtendedSignLattice.NONZERONEG) {
                return ExtendedSignLattice.NONZEROPOS;
            }
            else if (left == ExtendedSignLattice.NONZEROPOS) {
                return ExtendedSignLattice.NONZEROPOS;
            }
            // left <> 0
            return ExtendedSignLattice.NONZEROPOS;
        }
        else if (expression.getOperator() instanceof RemainderOperator) {
            // BOTTOM and TOP
            if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
                return ExtendedSignLattice.BOTTOM;
            }
            else if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
                return ExtendedSignLattice.TOP;
            }
            // Meaningful cases
            if (right == ExtendedSignLattice.ZEROPOS ||
                right == ExtendedSignLattice.ZERO ||
                right == ExtendedSignLattice.ZERONEG) {
                return ExtendedSignLattice.BOTTOM;
            }
            // Right > 0, < 0, or <> 0
            if (left == ExtendedSignLattice.ZEROPOS) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.ZERONEG;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.ZEROPOS;
                }
                return ExtendedSignLattice.TOP;
            }
            else if (left == ExtendedSignLattice.ZERO) {
                return ExtendedSignLattice.ZERO;
            }
            else if (left == ExtendedSignLattice.ZERONEG) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.ZEROPOS;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.ZERONEG;
                }
                return ExtendedSignLattice.TOP;
            }
            else if (left == ExtendedSignLattice.NONZERONEG) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.NONZEROPOS;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.NONZERONEG;
                }
                return ExtendedSignLattice.NONZERO;
            }
            else if (left == ExtendedSignLattice.NONZEROPOS) {
                if (right == ExtendedSignLattice.NONZERONEG) {
                    return ExtendedSignLattice.NONZERONEG;
                }
                else if (right == ExtendedSignLattice.NONZEROPOS) {
                    return ExtendedSignLattice.NONZEROPOS;
                }
                return ExtendedSignLattice.NONZERO;
            }
            // left <> 0
            return ExtendedSignLattice.NONZERO;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (constant.getValue() instanceof Integer n) {
            if (n == 0) {
                return ExtendedSignLattice.ZERO;
            }
            else if (n > 0) {
                return ExtendedSignLattice.NONZEROPOS;
            }
            return ExtendedSignLattice.NONZERONEG;
        }
        return ExtendedSignLattice.TOP;
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if (left == ExtendedSignLattice.TOP || right == ExtendedSignLattice.TOP) {
            return Satisfiability.UNKNOWN;
        }

        BinaryOperator bo = expression.getOperator();
        if (bo == ComparisonEq.INSTANCE) {
            return left.eq(right);
        }
        else if (bo == ComparisonGe.INSTANCE) {
            return left.eq(right).or(left.gt(right));
        }
        else if (bo == ComparisonGt.INSTANCE) {
            return left.gt(right);
        }
        else if (bo == ComparisonLe.INSTANCE) {
            return left.gt(right).negate();
        }
        else if (bo == ComparisonLt.INSTANCE) {
            return left.gt(right).negate().and(left.eq(right).negate());
        }
        else if (bo == ComparisonNe.INSTANCE) {
            return left.eq(right).negate();
        }
        return Satisfiability.UNKNOWN;
    }

    @Override
    public ExtendedSignLattice top() {
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return ExtendedSignLattice.BOTTOM;
    }

    @Override
    public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignLattice> environment,
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
            ExtendedSignLattice[] all = new ExtendedSignLattice[] { ExtendedSignLattice.ZERO,
                                                                    ExtendedSignLattice.ZEROPOS,
                                                                    ExtendedSignLattice.ZERONEG,
                                                                    ExtendedSignLattice.NONZERO,
                                                                    ExtendedSignLattice.NONZEROPOS,
                                                                    ExtendedSignLattice.NONZERONEG};
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
                        if (s.gt(eval).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
                        if (eval.gt(s).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                }
            else if (operator == ComparisonLt.INSTANCE)
                if (rightIsExpr) {
                    for (ExtendedSignLattice s : all)
                        if (s.gt(eval).or(s.eq(eval)).mightBeFalse())
                            update = update == null ? starting.glb(s) : update.lub(starting.glb(s));
                } else {
                    for (ExtendedSignLattice s : all)
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
