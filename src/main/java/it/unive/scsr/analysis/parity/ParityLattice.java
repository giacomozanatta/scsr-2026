package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

/**
 *    TOP
 *   /   \
 * EVEN  ODD
 *   \   /
 *   BOTTOM
 *
 * !!! EVEN e ODD non sono comparabili tra loro. !!!
 *
 * - BOTTOM: nessun valore possibile (codice irraggiungibile, ad esempio a causa di una condizione palesemente falsa -> if (i % 2 = 0 && i == 3))
 * - EVEN: la variabile è sicuramente pari (es. 0, 2, 4, -6)
 * - ODD: la variabile è sicuramente dispari (es. 1, 3, -5)
 * - TOP: parità sconosciuta, rappresenta tutto l'insieme Z (es. il parametro di una funzione, il risultato di una divisione o il merge di due rami in cui la parità è diversa)
 */
public class ParityLattice implements BaseLattice<ParityLattice> {

	public static final ParityLattice TOP = new ParityLattice(0);
	public static final ParityLattice EVEN = new ParityLattice(1);
	public static final ParityLattice ODD = new ParityLattice(2);
	public static final ParityLattice BOTTOM = new ParityLattice(3);

	private final int element;

	public ParityLattice(int element) {
		this.element = element;
	}

	@Override
	public ParityLattice top() {
		return TOP;
	}

	@Override
	public ParityLattice bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == BOTTOM) {
			return Lattice.bottomRepresentation();
		}
		if (this == TOP) {
			return Lattice.topRepresentation();
		}
		if (this == EVEN) {
			return new StringRepresentation("EVEN");
		}
		return new StringRepresentation("ODD");
	}

	/**
	 * Se arrivo qui, vuol dire che in due rami distinti (es: if else)
	 * la stessa variabile è pari da una parte, dispari dall'altra,
	 * di conseguenza può essere un qualunque valore (TOP)
	 *
	 * Esempio:
	 *   if (b) {
	 *   	x = 2; // EVEN
	 *   } else {
	 *    	x = 3; // ODD
	 *   }
	 *   conclusione: x = TOP
	 */
	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		return TOP;
	}

	/**
	 * BOTTOM <= EVEN <= TOP
	 * BOTTOM <= ODD <= TOP.
	 * EVEN e ODD non possono essere messi uno dopo l'altro, non sono comparabili, quindi restituisco false
	 */
	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(element);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ParityLattice other)) {
			return false;
		}

		return element == other.element;
	}
}
