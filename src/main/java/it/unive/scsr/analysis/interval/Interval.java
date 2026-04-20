package it.unive.scsr.analysis.interval;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.util.numeric.MathNumber;

public class Interval implements BaseNonRelationalValueDomain<IntervalLattice> {

    @Override
    public IntervalLattice top() {
        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice bottom() {
        return IntervalLattice.BOTTOM;
    }

    @Override
    public IntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(constant.getValue() instanceof Integer){
            //I need to check the integer value to assign the right approx value
            Integer n = (Integer) constant.getValue();
            return new IntervalLattice(n,n);
        }

        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice evalUnaryExpression(UnaryExpression expression, IntervalLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(arg.i == null)
            return IntervalLattice.BOTTOM;

        if(expression.getOperator() == NumericNegation.INSTANCE){
            MathNumber u = arg.i.getHigh();
            MathNumber l = arg.i.getLow();

            return new IntervalLattice(u.multiply(MathNumber.MINUS_ONE),l.multiply(MathNumber.MINUS_ONE));
        }

        return IntervalLattice.TOP;
    }

    @Override
    public IntervalLattice evalBinaryExpression(BinaryExpression expression, IntervalLattice left, IntervalLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        if(left.i == null || right.i == null){
            return IntervalLattice.BOTTOM;
        }
        if(expression.getOperator() instanceof AdditionOperator){
            return evalBinaryAdditionExpression(left,right);
        }else if (expression.getOperator() instanceof MultiplicationOperator) {
            return evalBinaryMultiplicationOperator(left,right);
        } else if (expression.getOperator() instanceof SubtractionOperator) {
            return evalBinarySubtractionOperator(left,right);
        } else if (expression.getOperator() instanceof DivisionOperator) {
            return evalBinaryDivisionOperator(left,right);
        }
        // TODO: implmenent the other operators.

        return IntervalLattice.TOP;
    }

    private IntervalLattice evalBinaryAdditionExpression(IntervalLattice left, IntervalLattice right){
        if (left.isBottom() || right.isBottom())
            return IntervalLattice.BOTTOM;

        MathNumber u1 = left.i.getHigh();
        MathNumber u2 = right.i.getHigh();

        MathNumber l1 = left.i.getLow();
        MathNumber l2 = right.i.getLow();

        MathNumber lSum = l1.add(l2);
        MathNumber uSum = u1.add(u2);

        if(lSum.isNaN() || uSum.isNaN())
            return IntervalLattice.TOP;

        return new IntervalLattice(lSum,uSum);
    }

    private IntervalLattice evalBinaryMultiplicationOperator(IntervalLattice left, IntervalLattice right){
        MathNumber min = MathNumber.PLUS_INFINITY;
        MathNumber max = MathNumber.MINUS_INFINITY;

        if (left.isBottom() || right.isBottom())
            return IntervalLattice.BOTTOM;

        MathNumber[] leftLimit = {left.i.getLow(),left.i.getHigh()};
        MathNumber[] rightLimit = {right.i.getLow(),right.i.getHigh()};

        for (MathNumber elemL : leftLimit){
            for (MathNumber elemR : rightLimit){
                MathNumber eval;
                if (elemL.isNaN() || elemR.isNaN())
                    return IntervalLattice.TOP;
                else if((elemL.isZero() && elemR.isInfinite()) || (elemL.isInfinite() && elemR.isZero()))
                    eval = MathNumber.ZERO;
                else
                    eval = elemL.multiply(elemR);

                if (eval.isNaN())
                    return IntervalLattice.TOP;
                else {
                    if (eval.compareTo(min) < 0)
                        min = eval;
                    if (eval.compareTo(max) > 0)
                        max = eval;
                }
            }
        }

        return new IntervalLattice(min,max);
    }

    private IntervalLattice evalBinarySubtractionOperator(IntervalLattice left, IntervalLattice right) {
        if (left.isBottom() || right.isBottom())
            return IntervalLattice.BOTTOM;

        MathNumber u1 = left.i.getHigh();
        MathNumber u2 = right.i.getHigh();

        MathNumber l1 = left.i.getLow();
        MathNumber l2 = right.i.getLow();

        MathNumber lSub = l1.subtract(u2);
        MathNumber uSub = u1.subtract(l2);

        if(lSub.isNaN() || uSub.isNaN())
            return IntervalLattice.TOP;

        return new IntervalLattice(lSub,uSub);
    }

    private IntervalLattice evalBinaryDivisionOperator(IntervalLattice left, IntervalLattice right) {
        MathNumber min = MathNumber.PLUS_INFINITY;
        MathNumber max = MathNumber.MINUS_INFINITY;

        if (left.isBottom() || right.isBottom())
            return IntervalLattice.BOTTOM;
        else if (right.isZero()) // [a,b]/[0,0]
            return IntervalLattice.BOTTOM;
        else{
            MathNumber[] leftLimits = {left.i.getLow(), left.i.getHigh()};
            MathNumber[] rightLimits = {right.i.getLow(), right.i.getHigh()};

            // [a,b]/[0,c] = [a,b]/[c,0] = {a/0=infinity, a/c, b/0=infinity, b/c}
            // [a,0]/[b,c] = [0,a]/[b,c] = {a/b, a/c, 0/b=0, 0/c=0}
            // [0,a]/[0,b] = [a,0]/[0,b] = [0,a]/[b,0] = [a,0]/[b,0] =  {0/0=?, 0/b=0, a/0=infinity, a/b}
            // [0,0]/[b,c] = {0/b=0, 0/c=0, 0/b=0, 0/c=0}
            // --> [a/0=infinity, a/0=infinity, 0/0=?]

            // [a,b]/[infinity,c] = [a,b]/[c,infinity] = {a/infinity=0, a/c, b/infinity= 0, b/c}
            // [a,b]/[infinity,infinity] = {a/infinity=0, a/infinity=0, b/infinity=0, b/infinity=0}
            // [a,infinity]/[b,c] = [infinity,a]/[b,c] = {a/b, a/c, infinity/b=infinity, infinity/c=infinity}
            // [infinity,infinity]/[b,c] = {infinity/b=infinity, infinity/c=infinity, infinity/b=infinity, infinity/c=infinity}
            // [a,infinity]/[infinity,b] = [infinity,a]/[infinity,b] = [infinity,a]/[b,infinity] = [a,infinity]/[b,infinity] = {a/infinity=0, a/b, infinity/infinity=?, infinity/b=infinity}
            // [infinity/infinity]/[infinity/infinity] = {infinity/infinity=?, infinity/infinity=?, infinity/infinity=?, infinity/infinity=?}
            // --> [a/infinity=0,infinity/b=infinity, infinity/infinity=?]

            // [a,b]/[0,infinity] = [a,b]/[infinity,0] = {a/0=infinity,b/0=infinity,a/infinity=0,b/infinity=0}
            // [0,infinity]/[a,b] = [infinity,0]/[a,b] = {0/a=0, 0/b=0, infinity/a, infinity/b}
            // [0,a]/[0,infinity] = [a,0]/[0,infinity] = [0,a][infinity,0] = [a,0]/[infinity,0] = {0/0 = ? ,0/infinity = 0, a/0 = infinity, a/infinity = 0}
            // [infinity,a]/[0,infinity] = [a,infinity]/[0,infinity] = [infinity,a]/[infinity,0] = [a,infinity]/[infinity,a] = {infinity/0 = infinity, infinity/infinity = ?, a/0 = infinity, a/infinity = 0}
            // [infinity,infinity]/[0,infinity] = [infinity,infinity]/[infinity,0] = {infinity/0 = infinity, infinity/infinity = ? }
            // [0,0]/[0,infinity] = [0,0]/[infinity,0] = {0/0 = ?, 0/infinity = 0 }
            // --> [ 0/infinity = 0, infinity/0 = infinity, infinity/infinity = ?, 0/0 = ? ]

            // Attention cased: [a/infinity=0, infinity/b=infinity, a/0=infinity, 0/infinity = 0, infinity/0 = infinity, 0/0=?, infinity/infinity=?]
            // The divide function of MathNumber already consider cased: [a/infinity=0, infinity/b=infinity]
            // So need to handle: a/0=infinity, 0/infinity = 0, infinity/0 = infinity, 0/0=?, infinity/infinity=?

            for (MathNumber leftV: leftLimits) {
                for (MathNumber rightV : rightLimits){
                    MathNumber eval;
                    if (leftV.isNaN() || rightV.isNaN() || (leftV.isZero() && rightV.isZero()) || (leftV.isInfinite() && rightV.isInfinite())) // [0/0 = ?, infinity/infinity = ?]
                        return IntervalLattice.BOTTOM;
                    else if ((leftV.isFinite() &&  rightV.isZero()) || (leftV.isInfinite() && rightV.isZero())) // [a/0=infinity, infinity/0 = infinity]
                        eval = (leftV.isPositive()) ? MathNumber.PLUS_INFINITY : MathNumber.MINUS_INFINITY;
                    else if (leftV.isZero() && rightV.isInfinite()) // [0/infinity = 0]
                        eval = MathNumber.ZERO;
                    else
                        eval = leftV.divide(rightV);

                    if (eval.isNaN()) return IntervalLattice.BOTTOM;

                    if (eval.leq(min))
                        min = eval;
                    if (eval.geq(max))
                        max = eval;
                }
            }

            return new IntervalLattice(min,max);
        }
    }
}