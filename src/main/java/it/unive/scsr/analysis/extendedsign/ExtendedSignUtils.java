package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.symbolic.value.Constant;
import it.unive.scsr.analysis.extendedsign.base.ExtendedSignLattice;

/**
 * Servizio condiviso sia da ExtendedSign che da ExtendedSignTaint,
 * così da non dover ripetere codice
 */
public class ExtendedSignUtils {

	public static ExtendedSignLattice evalConstant(Constant constant) {
		if (constant.getValue() instanceof Integer) {
			int n = (Integer) constant.getValue();
			if (n < 0) {
				return ExtendedSignLattice.NEG;
			}
			if (n == 0) {
				return ExtendedSignLattice.ZERO;
			}
			return ExtendedSignLattice.POS;
		}
		return ExtendedSignLattice.TOP;
	}

	public static ExtendedSignLattice negate(ExtendedSignLattice value) {
		if (value == ExtendedSignLattice.NEG) {
			return ExtendedSignLattice.POS;
		}
		if (value == ExtendedSignLattice.POS) {
			return ExtendedSignLattice.NEG;
		}
		if (value == ExtendedSignLattice.ZERO) {
			return ExtendedSignLattice.ZERO;
		}
		if (value == ExtendedSignLattice.LTE_ZERO) {
			return ExtendedSignLattice.GTE_ZERO;
		}
		if (value == ExtendedSignLattice.GTE_ZERO) {
			return ExtendedSignLattice.LTE_ZERO;
		}
		return value; // NON_ZERO, TOP, BOTTOM are self-symmetric under negation
	}

	public static ExtendedSignLattice add(ExtendedSignLattice left, ExtendedSignLattice right) {
		// uno dei due elementi non è raggiungibile: allora anche il risultato non lo è
		if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
			return ExtendedSignLattice.BOTTOM;
		}

		// uno dei due elementi è zero, restituisco il valore dell'altro
		if (left == ExtendedSignLattice.ZERO) {
			return right;
		}
		if (right == ExtendedSignLattice.ZERO) {
			return left;
		}

		// stesso segno: anche il risultato ha lo stesso segno
		if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG) {
			return ExtendedSignLattice.NEG;
		}
		if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS) {
			return ExtendedSignLattice.POS;
		}

		// NEG + LTE_ZERO = NEG
		if (
			(left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.LTE_ZERO)
			|| (left == ExtendedSignLattice.LTE_ZERO && right == ExtendedSignLattice.NEG)
		) {
			return ExtendedSignLattice.NEG;
		}

		// LTE_ZERO + LTE_ZERO = LTE_ZERO
		if (left == ExtendedSignLattice.LTE_ZERO && right == ExtendedSignLattice.LTE_ZERO) {
			return ExtendedSignLattice.LTE_ZERO;
		}

		// POS + GTE_ZERO = POS
		if (
			(left == ExtendedSignLattice.POS && right == ExtendedSignLattice.GTE_ZERO)
			|| (left == ExtendedSignLattice.GTE_ZERO && right == ExtendedSignLattice.POS)
		) {
			return ExtendedSignLattice.POS;
		}

		// GTE_ZERO + GTE_ZERO = GTE_ZERO
		if (left == ExtendedSignLattice.GTE_ZERO && right == ExtendedSignLattice.GTE_ZERO) {
			return ExtendedSignLattice.GTE_ZERO;
		}

		return ExtendedSignLattice.TOP;
	}

	public static ExtendedSignLattice multiply(ExtendedSignLattice left, ExtendedSignLattice right) {
		// uno dei due elementi non è raggiungibile: allora anche il risultato non lo è
		if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
			return ExtendedSignLattice.BOTTOM;
		}

		// moltiplicazione per zero, da zero
		if (left == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.ZERO) {
			return ExtendedSignLattice.ZERO;
		}

		// NEG * NEG = POS
		if (left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.NEG) {
			return ExtendedSignLattice.POS;
		}

		// POS * POS = POS
		if (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.POS) {
			return ExtendedSignLattice.POS;
		}

		// NEG * POS = POS * NEG = NEG
		if (
			(left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.POS)
			|| (left == ExtendedSignLattice.POS && right == ExtendedSignLattice.NEG)
		) {
			return ExtendedSignLattice.NEG;
		}

		// NON_ZERO * { NEG, POS, NON_ZERO } = NON_ZERO
		if (
			left == ExtendedSignLattice.NON_ZERO
			&& (right == ExtendedSignLattice.NEG || right == ExtendedSignLattice.POS || right == ExtendedSignLattice.NON_ZERO)
		) {
			return ExtendedSignLattice.NON_ZERO;
		}
		if (
			right == ExtendedSignLattice.NON_ZERO
			&& (left == ExtendedSignLattice.NEG || left == ExtendedSignLattice.POS)
		) {
			return ExtendedSignLattice.NON_ZERO;
		}

		// LTE_ZERO * LTE_ZERO = GTE_ZERO
		if (left == ExtendedSignLattice.LTE_ZERO && right == ExtendedSignLattice.LTE_ZERO) {
			return ExtendedSignLattice.GTE_ZERO;
		}
		// NEG * LTE_ZERO = LTE_ZERO * NEG = GTE_ZERO
		if (
			(left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.LTE_ZERO)
			|| (left == ExtendedSignLattice.LTE_ZERO && right == ExtendedSignLattice.NEG)
		) {
			return ExtendedSignLattice.GTE_ZERO;
		}

		// GTE_ZERO * GTE_ZERO = GTE_ZERO
		if (left == ExtendedSignLattice.GTE_ZERO && right == ExtendedSignLattice.GTE_ZERO) {
			return ExtendedSignLattice.GTE_ZERO;
		}
		// POS * GTE_ZERO = GTE_ZERO * POS = GTE_ZERO
		if (
			(left == ExtendedSignLattice.POS && right == ExtendedSignLattice.GTE_ZERO)
			|| (left == ExtendedSignLattice.GTE_ZERO && right == ExtendedSignLattice.POS)
		) {
			return ExtendedSignLattice.GTE_ZERO;
		}

		// LTE_ZERO * GTE_ZERO = LTE_ZERO
		if (
			(left == ExtendedSignLattice.LTE_ZERO && right == ExtendedSignLattice.GTE_ZERO)
			|| (left == ExtendedSignLattice.GTE_ZERO && right == ExtendedSignLattice.LTE_ZERO)
		) {
			return ExtendedSignLattice.LTE_ZERO;
		}

		// NEG * GTE_ZERO = GTE_ZERO * NEG = LTE_ZERO
		if (
			(left == ExtendedSignLattice.NEG && right == ExtendedSignLattice.GTE_ZERO)
			|| (left == ExtendedSignLattice.GTE_ZERO && right == ExtendedSignLattice.NEG)
		) {
			return ExtendedSignLattice.LTE_ZERO;
		}

		// POS * LTE_ZERO = LTE_ZERO * POS = LTE_ZERO
		if (
			(left == ExtendedSignLattice.POS && right == ExtendedSignLattice.LTE_ZERO)
			|| (left == ExtendedSignLattice.LTE_ZERO && right == ExtendedSignLattice.POS)
		) {
			return ExtendedSignLattice.LTE_ZERO;
		}

		// tutti i casi rimanenti TOP
		return ExtendedSignLattice.TOP;
	}

	public static ExtendedSignLattice divide(ExtendedSignLattice left, ExtendedSignLattice right) {
		// divisione per ZERO/BOTTOM = BOTTOM
		if (right == ExtendedSignLattice.ZERO || right == ExtendedSignLattice.BOTTOM) {
			return ExtendedSignLattice.BOTTOM;
		}

		// dividere BOTTOM = BOTTOM
		if (left == ExtendedSignLattice.BOTTOM) {
			return ExtendedSignLattice.BOTTOM;
		}

		// zero diviso qualsiasi valore non-zero = zero
		if (left == ExtendedSignLattice.ZERO) {
			return ExtendedSignLattice.ZERO;
		}

		// divisore negativo, ma non zero
		// NB: includo sempre zero nel risultato, perché un valore piccolo diviso
		//     per uno molto grande può restituire zero (troncamento intero)
		if (right == ExtendedSignLattice.NEG) {
			if (left == ExtendedSignLattice.NEG) {
				return ExtendedSignLattice.GTE_ZERO; // NEG / NEG = GTE_ZERO
			}
			if (left == ExtendedSignLattice.POS) {
				return ExtendedSignLattice.LTE_ZERO; // POS / NEG = LTE_ZERO
			}
			if (left == ExtendedSignLattice.LTE_ZERO) {
				return ExtendedSignLattice.GTE_ZERO; // LTE_ZERO / NEG = GTE_ZERO
			}
			if (left == ExtendedSignLattice.GTE_ZERO) {
				return ExtendedSignLattice.LTE_ZERO; // GTE_ZERO / NEG = LTE_ZERO
			}
			return ExtendedSignLattice.TOP;
		}

		// divisore positivo
		// NB: come sopra per quanto riguarda la scelta di includere sempre lo ZERO
		if (right == ExtendedSignLattice.POS) {
			if (left == ExtendedSignLattice.NEG) {
				return ExtendedSignLattice.LTE_ZERO; // NEG / POS = LTE_ZERO
			}
			if (left == ExtendedSignLattice.POS) {
				return ExtendedSignLattice.GTE_ZERO; // POS / POS = GTE_ZERO
			}
			if (left == ExtendedSignLattice.LTE_ZERO) {
				return ExtendedSignLattice.LTE_ZERO; // LTE_ZERO / POS = LTE_ZERO
			}
			if (left == ExtendedSignLattice.GTE_ZERO) {
				return ExtendedSignLattice.GTE_ZERO; // GTE_ZERO / POS = GTE_ZERO
			}
			return ExtendedSignLattice.TOP;
		}

		// tutti i casi rimanenti TOP
		return ExtendedSignLattice.TOP;
	}

	/**
	 * Il risultato dell'operazione remainder segue il segno del dividendo;
	 * in pratica, il segno del divisore non è importante, a meno che non sia BOTTOM o ZERO
	 */
	public static ExtendedSignLattice remainder(ExtendedSignLattice left, ExtendedSignLattice right) {
		// uno dei due elementi non è raggiungibile: allora anche il risultato non lo è
		if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
			return ExtendedSignLattice.BOTTOM;
		}

		// divisione per zero = BOTTOM
		if (right == ExtendedSignLattice.ZERO) {
			return ExtendedSignLattice.BOTTOM;
		}

		// il resto di una divisione dello zero è zero
		if (left == ExtendedSignLattice.ZERO) {
			return ExtendedSignLattice.ZERO;
		}

		// se dividendo < 0 o <= 0, allora il risultato è <= 0
		if (left == ExtendedSignLattice.NEG || left == ExtendedSignLattice.LTE_ZERO) {
			return ExtendedSignLattice.LTE_ZERO;
		}

		// se dividendo > 0 o >= 0, allora il risultato è >= 0
		if (left == ExtendedSignLattice.POS || left == ExtendedSignLattice.GTE_ZERO) {
			return ExtendedSignLattice.GTE_ZERO;
		}

		// left == NON_ZERO | TOP: il risultato può essere qualunque cosa
		return ExtendedSignLattice.TOP;
	}

	/**
	 * Il risultato dell'operazione modulo segue il segno del divisore;
	 * in pratica, il segno del dividendo non è importante, a meno che non sia BOTTOM o ZERO
	 */
	public static ExtendedSignLattice modulo(ExtendedSignLattice left, ExtendedSignLattice right) {
		// uno dei due elementi non è raggiungibile: allora anche il risultato non lo è
		if (left == ExtendedSignLattice.BOTTOM || right == ExtendedSignLattice.BOTTOM) {
			return ExtendedSignLattice.BOTTOM;
		}

		// divisione per zero = BOTTOM
		if (right == ExtendedSignLattice.ZERO) {
			return ExtendedSignLattice.BOTTOM;
		}

		// il modulo di una divisione dello zero è zero
		if (left == ExtendedSignLattice.ZERO) {
			return ExtendedSignLattice.ZERO;
		}

		// se divisore > 0 o >= 0, allora il risultato è >= 0
		if (right == ExtendedSignLattice.POS || right == ExtendedSignLattice.GTE_ZERO) {
			return ExtendedSignLattice.GTE_ZERO;
		}

		// se divisore > 0 o >= 0, allora il risultato è >= 0
		if (right == ExtendedSignLattice.NEG || right == ExtendedSignLattice.LTE_ZERO) {
			return ExtendedSignLattice.LTE_ZERO;
		}

		// right == NON_ZERO | TOP: il risultato può essere qualunque cosa
		return ExtendedSignLattice.TOP;
	}
}
