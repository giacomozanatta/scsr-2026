package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.numeric.Addition;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.scsr.analysis.sign.SignLattice;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice>{

	@Override
	public ExtendedSignLattice top() {
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice bottom() {
		return ExtendedSignLattice.BOTTOM;
	}

	@Override
	public ExtendedSignLattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {

		if(constant.getValue() instanceof Integer) {

			Integer n = (Integer) constant.getValue();
			if(n == 0)
				return ExtendedSignLattice.ZERO;
			else if(n > 0)
				return ExtendedSignLattice.POS;
			return ExtendedSignLattice.NEG;
		}

		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp,
												   SemanticOracle oracle) throws SemanticException {

		if(expression.getOperator() == NumericNegation.INSTANCE) {
			if(arg == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.POS;
			else if(arg == ExtendedSignLattice.POS)
				return ExtendedSignLattice.NEG;
			if(arg == ExtendedSignLattice.NONNEG)
				return ExtendedSignLattice.NONPOS;
			else if(arg == ExtendedSignLattice.NONPOS)
				return ExtendedSignLattice.NONNEG;
			else if(arg == ExtendedSignLattice.ZERO)
				return ExtendedSignLattice.ZERO;
			else if(arg == ExtendedSignLattice.TOP)
				return ExtendedSignLattice.TOP;
			else if(arg == ExtendedSignLattice.BOTTOM)
				return ExtendedSignLattice.BOTTOM;
			else if(arg == ExtendedSignLattice.NONZERO)
				return ExtendedSignLattice.NONZERO;
		}

		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right,
													ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if(expression.getOperator() instanceof AdditionOperator) {

			// bottom
			if (left == ExtendedSignLattice.BOTTOM ||
					right == ExtendedSignLattice.BOTTOM)
				return ExtendedSignLattice.BOTTOM;

			// top
			if (left == ExtendedSignLattice.TOP ||
					right == ExtendedSignLattice.TOP)
				return ExtendedSignLattice.TOP;

				// ZERO
			else if (left == ExtendedSignLattice.ZERO)
				return right;

			else if (right == ExtendedSignLattice.ZERO)
				return left;

				// POS + POS = POS
			else if (left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.POS)
				return ExtendedSignLattice.POS;

				// NEG + NEG = NEG
			else if (left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.NEG;

				// POS + NEG = TOP
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NEG) ||

					(left == ExtendedSignLattice.NEG &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.TOP;

				// POS + NONNEG = POS
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONNEG) ||

					(left == ExtendedSignLattice.NONNEG &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.POS;

				// NEG + NONPOS = NEG
			else if ((left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NONPOS) ||

					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NEG;

				// POS + NONPOS = TOP
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONPOS) ||

					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.TOP;

				// NEG + NONNEG = TOP
			else if ((left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NONNEG) ||

					(left == ExtendedSignLattice.NONNEG &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.TOP;

				// POS + NONZERO = TOP
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONZERO) ||
					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.TOP;

				// NEG + NONZERO = TOP
			else if ((left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NONZERO) ||
					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.TOP;

				// NONNEG + NONNEG = NONNEG
			else if (left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONNEG)
				return ExtendedSignLattice.NONNEG;

				// NONPOS + NONPOS = NONPOS
			else if (left == ExtendedSignLattice.NONPOS &&
					right == ExtendedSignLattice.NONPOS)
				return ExtendedSignLattice.NONPOS;

				// NONNEG + NONPOS = TOP
			else if ((left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONPOS) ||

					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.NONNEG))
				return ExtendedSignLattice.TOP;

				// NONNEG + NONZERO = TOP
			else if ((left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONZERO) ||

					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NONNEG))
				return ExtendedSignLattice.TOP;

				// NONPOS + NONZERO = TOP
			else if ((left == ExtendedSignLattice.NONPOS &&
					right == ExtendedSignLattice.NONZERO) ||

					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NONPOS))
				return ExtendedSignLattice.TOP;

				// NONZERO + NONZERO = TOP
			else if (left == ExtendedSignLattice.NONZERO &&
					right == ExtendedSignLattice.NONZERO)
				return ExtendedSignLattice.TOP;

		} else if (expression.getOperator() instanceof SubtractionOperator) {
			//negate the right argument
			UnaryExpression neg =
					new UnaryExpression(
							expression.getStaticType(),
							expression.getRight(),
							NumericNegation.INSTANCE,
							expression.getCodeLocation());

			right = evalUnaryExpression(neg, right, pp, oracle);

			//same sum logic
			// bottom
			if (left == ExtendedSignLattice.BOTTOM ||
					right == ExtendedSignLattice.BOTTOM)
				return ExtendedSignLattice.BOTTOM;

			// top
			if (left == ExtendedSignLattice.TOP ||
					right == ExtendedSignLattice.TOP)
				return ExtendedSignLattice.TOP;

				// ZERO
			else if (left == ExtendedSignLattice.ZERO)
				return right;

			else if (right == ExtendedSignLattice.ZERO)
				return left;

				// POS + POS = POS
			else if (left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.POS)
				return ExtendedSignLattice.POS;

				// NEG + NEG = NEG
			else if (left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.NEG;

				// POS + NEG = TOP
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NEG) ||

					(left == ExtendedSignLattice.NEG &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.TOP;

				// POS + NONNEG = POS
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONNEG) ||

					(left == ExtendedSignLattice.NONNEG &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.POS;

				// NEG + NONPOS = NEG
			else if ((left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NONPOS) ||

					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NEG;

				// POS + NONPOS = TOP
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONPOS) ||

					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.TOP;

				// NEG + NONNEG = TOP
			else if ((left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NONNEG) ||

					(left == ExtendedSignLattice.NONNEG &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.TOP;

				// POS + NONZERO = TOP
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONZERO) ||
					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.TOP;

				// NEG + NONZERO = TOP
			else if ((left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NONZERO) ||
					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.TOP;

				// NONNEG + NONNEG = NONNEG
			else if (left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONNEG)
				return ExtendedSignLattice.NONNEG;

				// NONPOS + NONPOS = NONPOS
			else if (left == ExtendedSignLattice.NONPOS &&
					right == ExtendedSignLattice.NONPOS)
				return ExtendedSignLattice.NONPOS;

				// NONNEG + NONPOS = TOP
			else if ((left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONPOS) ||

					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.NONNEG))
				return ExtendedSignLattice.TOP;

				// NONNEG + NONZERO = TOP
			else if ((left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONZERO) ||

					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NONNEG))
				return ExtendedSignLattice.TOP;

				// NONPOS + NONZERO = TOP
			else if ((left == ExtendedSignLattice.NONPOS &&
					right == ExtendedSignLattice.NONZERO) ||

					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NONPOS))
				return ExtendedSignLattice.TOP;

				// NONZERO + NONZERO = TOP
			else if (left == ExtendedSignLattice.NONZERO &&
					right == ExtendedSignLattice.NONZERO)
				return ExtendedSignLattice.TOP;

		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			// bottom
			if (left == ExtendedSignLattice.BOTTOM ||
					right == ExtendedSignLattice.BOTTOM)
				return ExtendedSignLattice.BOTTOM;

			// ZERO
			if (left == ExtendedSignLattice.ZERO ||
					right == ExtendedSignLattice.ZERO)
				return ExtendedSignLattice.ZERO;

			// TOP
			if (left == ExtendedSignLattice.TOP ||
					right == ExtendedSignLattice.TOP)
				return ExtendedSignLattice.TOP;

				// POS * POS = POS
			else if (left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.POS)
				return ExtendedSignLattice.POS;

				// NEG * NEG = POS
			else if (left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.POS;

				// POS * NEG = NEG
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NEG) ||
					(left == ExtendedSignLattice.NEG &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.NEG;

				// POS * NONNEG = NONNEG
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONNEG) ||
					(left == ExtendedSignLattice.NONNEG &&
							right == ExtendedSignLattice.POS))
				return ExtendedSignLattice.NONNEG;

				// NEG * NONPOS = NONPOS
			else if ((left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NONPOS) ||
					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NONPOS;

				// NONNEG * NONNEG = NONNEG
			else if (left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONNEG)
				return ExtendedSignLattice.NONNEG;

				// NONPOS * NONPOS = NONNEG
			else if (left == ExtendedSignLattice.NONPOS &&
					right == ExtendedSignLattice.NONPOS)
				return ExtendedSignLattice.NONNEG;

				// NONZERO * NONZERO = NONZERO
			else if (left == ExtendedSignLattice.NONZERO &&
					right == ExtendedSignLattice.NONZERO)
				return ExtendedSignLattice.NONZERO;

				// NONZERO * POS = NONZERO
			else if ((left == ExtendedSignLattice.NONZERO &&
					right == ExtendedSignLattice.POS) ||
					(left == ExtendedSignLattice.POS &&
							right == ExtendedSignLattice.NONZERO))
				return ExtendedSignLattice.NONZERO;

				// NONZERO * NEG = NONZERO
			else if ((left == ExtendedSignLattice.NONZERO &&
					right == ExtendedSignLattice.NEG) ||
					(left == ExtendedSignLattice.NEG &&
							right == ExtendedSignLattice.NONZERO))
				return ExtendedSignLattice.NONZERO;

				// NONNEG * NONPOS = TOP
			else if ((left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONPOS) ||
					(left == ExtendedSignLattice.NONPOS &&
							right == ExtendedSignLattice.NONNEG))
				return ExtendedSignLattice.TOP;

				// NONNEG * NONZERO = TOP
			else if ((left == ExtendedSignLattice.NONNEG &&
					right == ExtendedSignLattice.NONZERO) ||
					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NONNEG))
				return ExtendedSignLattice.TOP;

				// NONPOS * NONZERO = TOP
			else if ((left == ExtendedSignLattice.NONPOS &&
					right == ExtendedSignLattice.NONZERO) ||
					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NONPOS))
				return ExtendedSignLattice.TOP;
		} else if (expression.getOperator() instanceof DivisionOperator) {
			// bottom
			if (left == ExtendedSignLattice.BOTTOM ||
					right == ExtendedSignLattice.BOTTOM)
				return ExtendedSignLattice.BOTTOM;

			// X / ZERO = TOP
			if (right == ExtendedSignLattice.ZERO)
				return ExtendedSignLattice.TOP;

			// ZERO / X = ZERO
			if (left == ExtendedSignLattice.ZERO)
				return ExtendedSignLattice.ZERO;

			// TOP
			if (left == ExtendedSignLattice.TOP ||
					right == ExtendedSignLattice.TOP)
				return ExtendedSignLattice.TOP;

				// POS / POS = POS
			else if (left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.POS)
				return ExtendedSignLattice.POS;

				// POS / NEG = NEG
			else if (left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.NEG;

				// NEG / POS = NEG
			else if (left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.POS)
				return ExtendedSignLattice.NEG;

				// NEG / NEG = POS
			else if (left == ExtendedSignLattice.NEG &&
					right == ExtendedSignLattice.NEG)
				return ExtendedSignLattice.POS;

				// NONZERO / POS = NONZERO
			else if ((left == ExtendedSignLattice.NONZERO &&
					right == ExtendedSignLattice.POS) ||
					(left == ExtendedSignLattice.NONZERO &&
							right == ExtendedSignLattice.NEG))
				return ExtendedSignLattice.NONZERO;

				// POS / NONZERO = NONZERO
			else if ((left == ExtendedSignLattice.POS &&
					right == ExtendedSignLattice.NONZERO) ||
					(left == ExtendedSignLattice.NEG &&
							right == ExtendedSignLattice.NONZERO))
				return ExtendedSignLattice.NONZERO;

				// NONZERO / NONZERO = TOP
			else if (left == ExtendedSignLattice.NONZERO &&
					right == ExtendedSignLattice.NONZERO)
				return ExtendedSignLattice.TOP;
		}

		return ExtendedSignLattice.TOP;
	}



}
