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

public class Parity implements
		BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	    public ParityLattice top() {
			return ParityLattice.TOP;
	    }

	    @Override
	    public ParityLattice bottom() {
	    	return ParityLattice.BOTTOM;
	    }

        @Override
	    public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
                throws SemanticException {
            if (constant.getValue() instanceof Integer n) {
                if (n % 2 == 0) {
                    return ParityLattice.EVEN;
                }
                return ParityLattice.ODD;
            }
            return ParityLattice.TOP;
        }

        @Override
        public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp,
                SemanticOracle oracle) throws SemanticException {
            // if x is EVEN then -x is EVEN, if x is ODD then -x is ODD
            if (expression.getOperator() == NumericNegation.INSTANCE) {
                return arg;
            }
            return ParityLattice.TOP;
        }

        @Override
        public ParityLattice evalBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
                                                  ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
            BinaryOperator op = expression.getOperator();

            if (op instanceof AdditionOperator || op instanceof SubtractionOperator) {
                if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM) {
                    return ParityLattice.BOTTOM;
                }
                if (left == ParityLattice.TOP || right == ParityLattice.TOP) {
                    return ParityLattice.TOP;
                }
                if (left == right) {
                    return ParityLattice.EVEN;
                }
                return ParityLattice.ODD;
            } else if (op instanceof MultiplicationOperator) {
                if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM) {
                    return ParityLattice.BOTTOM;
                }
                if (left == ParityLattice.EVEN || right == ParityLattice.EVEN) {   // EVEN * x is always EVEN
                    return ParityLattice.EVEN;
                }
                if (left == ParityLattice.TOP || right == ParityLattice.TOP) {
                    return ParityLattice.TOP;
                }
                return ParityLattice.ODD;  // ODD * ODD is ODD
            } else if (op instanceof ModuloOperator || op instanceof RemainderOperator) {
                if (right == ParityLattice.EVEN) {
                    return left;
                }
                return ParityLattice.TOP;
            } else {
                return ParityLattice.TOP;  // for other operators, we don't know how they affect parity
            }
        }

        @Override
        public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ParityLattice left, ParityLattice right,
                                                        ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
            if (left.isTop() || right.isTop()) {
                return Satisfiability.UNKNOWN;
            }

            BinaryOperator op = expression.getOperator();

            if (op instanceof ComparisonEq)     // == operator
                return left.eq(right);          // EVEN == ODD is NOT_SATISFIED, EVEN == EVEN is UNKNOWN
            if (op instanceof ComparisonNe)     // != operator
                return left.eq(right).negate(); // EVEN != ODD is SATISFIED, EVEN != EVEN is UNKNOWN

            return Satisfiability.UNKNOWN;      // <, >, <=, >= don't have a clear meaning
        }

        @Override
        public ValueEnvironment<ParityLattice> assumeBinaryExpression(
                ValueEnvironment<ParityLattice> environment,
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

            BinaryOperator operator = expression.getOperator();
            ValueExpression left  = (ValueExpression) expression.getLeft();
            ValueExpression right = (ValueExpression) expression.getRight();

            Identifier id;
            ParityLattice eval;
            if (left instanceof Identifier) {
                eval = eval(environment, right, src, oracle);
                id = (Identifier) left;
            } else if (right instanceof Identifier) {
                eval = eval(environment, left, src, oracle);
                id = (Identifier) right;
            } else {
                return environment;
            }

            ParityLattice starting = environment.getState(id);
            if (eval.isBottom() || starting.isBottom())
                return environment.bottom();

            ParityLattice update = null;

            if (operator instanceof ComparisonEq) {
                update = starting.glb(eval);

            } else if (operator instanceof ComparisonNe) {
                if (eval == ParityLattice.EVEN)
                    update = starting.glb(ParityLattice.ODD);
                else if (eval == ParityLattice.ODD)
                    update = starting.glb(ParityLattice.EVEN);
                else
                    return environment;

            } else {
                return environment;
            }

            if (update == null)
                return environment;
            else if (update.isBottom())
                return environment.bottom();
            else
                return environment.putState(id, update);
        }
}