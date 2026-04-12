package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;

public class Parity implements BaseNonRelationalValueDomain<ParityLattice> {

	@Override
	public ParityLattice top() {
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		return ParityLattice.BOTTOM;
	}

	@Override
	public ParityLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)  throws SemanticException {
		if (constant.getValue() instanceof Integer i) {
			// se divisibile per 2 (quindi resto 0), allora è pari, altrimenti dispari
			return (i % 2 == 0) ? ParityLattice.EVEN : ParityLattice.ODD;
		}

		// se il valore non è intero, non posso dire se è pari o dispari (es 4.2)
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalUnaryExpression(UnaryExpression expression, ParityLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		// se il numero è negativo, rimane comunque pari o dispari
		if (expression.getOperator() == NumericNegation.INSTANCE) {
			return arg;
		}

		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice evalBinaryExpression(
			BinaryExpression expression,
			ParityLattice left,
	        ParityLattice right,
			ProgramPoint pp,
			SemanticOracle oracle
	) throws SemanticException {

		// se uno dei due operandi è BOTTOM, allora anche il risultato è BOTTOM,
		if (left == ParityLattice.BOTTOM || right == ParityLattice.BOTTOM) {
			return ParityLattice.BOTTOM;
		}

		BinaryOperator binaryOperator = expression.getOperator();

		if (binaryOperator instanceof AdditionOperator || binaryOperator instanceof SubtractionOperator) {
			return evalAdditionSubstractionBinaryExpression(left, right);
		} else if (binaryOperator instanceof MultiplicationOperator) {
			return evalMultiplicationBinaryExpression(left, right);
		} else if (binaryOperator instanceof ModuloOperator || binaryOperator instanceof RemainderOperator) {
			return evalModuloRemainderBinaryExpression(left, right);
		}

		// per la divisione restituiamo sempre TOP, in quanto anche conoscendo se l'operando è pari o dispari,
		// non possiamo dire nulla sul fatto che il risultato sia pari o dispari: ad esempio 4/2 = 2 (EVEN), 6/2 = 3 (ODD)
		return ParityLattice.TOP;
	}

	/**
	 * Una sola regola: se il divisore è pari, allora il risultato è pari/dispari a seconda che il dividendo sia pari/dispari:
	 * es: 5%4=1 (ODD % EVEN --> ODD); 6%4=2 (EVEN % EVEN --> EVEN)
	 *
	 * Per i casi rimanenti, non si può dire nulla:
	 * es: 6%3=0 (EVEN), 6%5=1 (ODD), 7%3=1 (ODD), 8%3=2 (EVEN)
	 */
	private static ParityLattice evalModuloRemainderBinaryExpression(ParityLattice left, ParityLattice right) {
		if (right == ParityLattice.EVEN) {
			return left;
		}

		return ParityLattice.TOP;
	}

	/**
	 * Regole:
	 * 1: EVEN * qualunque cosa = EVEN (multiplo di 2 * qualunque cosa da sempre un numero pari, in quanto il risultato diventa multiplo di 2 a sua volta)
	 * 2: ODD * ODD = ODD (dispari * dispari, rimane sempre dispari) --> 3*3=9, 5*5 = 25, 3*5 = 15 etc
	 * 3: TOP * ODD = TOP (qualunque cosa * dispari, può essere sia pari che dispari)
	 * 4: TOP * TOP = TOP
	 */
	private static ParityLattice evalMultiplicationBinaryExpression(ParityLattice left, ParityLattice right) {
		// uno dei due operandi è pari [regola 1]
		if (left == ParityLattice.EVEN || right == ParityLattice.EVEN) {
			return ParityLattice.EVEN;
		}

		// entrambi gli operatori sono dispari [regola 2] -> il risultato è a sua volta dispari
		if (left == ParityLattice.ODD && right == ParityLattice.ODD) {
			return ParityLattice.ODD;
		}

		// [regole 3 e 4]: nessuno dei due operandi è pari; non sono entrambi dispari, quindi abbiamo uno o entrambi gli operandi che non è noto se sono pari o dispari
		// di conseguenza nemmeno sul risultato si può asserire qualcosa
		return ParityLattice.TOP;
	}

	/**
	 * Regole:
	 * 1: EVEN +- EVEN = EVEN --> (4+2=6, 4-2=2)
	 * 2: ODD +- ODD = EVEN --> (3+5=8, 5-3=2)
	 * 3: EVEN +- ODD = ODD --> (4+3=7, 4-3=1)
	 * 4: ODD +- EVEN = ODD --> (3+6=9, 6-3=3)
	 * 5: TOP +- qualunque cosa = TOP (non sapendo se l'operatore è pari o dispari, non posso asserire nulla sul risultato)
	 */
	private static ParityLattice evalAdditionSubstractionBinaryExpression(ParityLattice left, ParityLattice right) {
		// regola 5, uno dei due operandi non è noto se è pari o dispari -> allora neanche il risultato si sa se è pari o dispari
		if (left == ParityLattice.TOP || right == ParityLattice.TOP) {
			return ParityLattice.TOP;
		}

		// se sono entrambi pari o entrambi dispari (left.equals(right) = true, quindi EVEN+-EVEN oppure ODD+-ODD) [regole 1 e 2] → EVEN;
		// altrimenti [regole 3 e 4] → ODD
		return left.equals(right) ? ParityLattice.EVEN : ParityLattice.ODD;
	}
}
