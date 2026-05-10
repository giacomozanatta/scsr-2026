package it.unive.scsr.analysis.extendedsign.base;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.scsr.analysis.extendedsign.ExtendedSignUtils;

/**
 * Estensione di sign.
 * Il segno della variabile viene gestito tramite 8 valori:
 * BOTTOM, NEG, ZERO, POS, LTE_ZERO, NON_ZERO, GTE_ZERO, TOP.
 *
 * In questo modo si ha una maggiore precisione rispetto a sign, in quanto
 * se so che un valore è certamento diverso da zero, posso evitare, ad esempio, di emettere dei warning per divisione per zero
 * anche laddove non è così (ad esempio dopo un if else, dove il divisore è positivo in un caso, negativo nell'altro);
 * in questo caso, sign darebbe che divisore è TOP (quindi anche zero), mente extended sign da NON_ZERO
 */
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
	public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return ExtendedSignUtils.evalConstant(constant);
	}

	@Override
	public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression,
			ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		if (expression.getOperator() == NumericNegation.INSTANCE) {
			return ExtendedSignUtils.negate(arg);
		}
		return ExtendedSignLattice.TOP;
	}

	@Override
	public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression,
			ExtendedSignLattice left, ExtendedSignLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		if (expression.getOperator() instanceof AdditionOperator) {
			return ExtendedSignUtils.add(left, right);
		}
		if (expression.getOperator() instanceof SubtractionOperator) {
			return ExtendedSignUtils.add(left, ExtendedSignUtils.negate(right));
		}
		if (expression.getOperator() instanceof MultiplicationOperator) {
			return ExtendedSignUtils.multiply(left, right);
		}
		if (expression.getOperator() instanceof DivisionOperator) {
			return ExtendedSignUtils.divide(left, right);
		}
		if (expression.getOperator() instanceof ModuloOperator) {
			return ExtendedSignUtils.modulo(left, right);
		}
		if (expression.getOperator() instanceof RemainderOperator) {
			return ExtendedSignUtils.remainder(left, right);
		}
		return ExtendedSignLattice.TOP;
	}
}
