package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

/**
 * Lattice for the Parity abstract domain.
 *
 * Elements form a diamond-shaped lattice:
 *
 * <pre>
 *         ⊤
 *        / \
 *     Even  Odd
 *        \ /
 *         ⊥
 * </pre>
 *
 * Even and Odd are incomparable — neither is less than the other.
 */
public class ParityLattice implements BaseLattice<ParityLattice> {

	// Numeric encoding for each lattice element
	private static final int TOP_VAL    = 0;
	private static final int EVEN_VAL   = 1;
	private static final int ODD_VAL    = 2;
	private static final int BOTTOM_VAL = 3;

	// Singleton instances
	public static final ParityLattice TOP    = new ParityLattice(TOP_VAL);
	public static final ParityLattice EVEN   = new ParityLattice(EVEN_VAL);
	public static final ParityLattice ODD    = new ParityLattice(ODD_VAL);
	public static final ParityLattice BOTTOM = new ParityLattice(BOTTOM_VAL);

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

	/**
	 * Least upper bound (join) of two incomparable elements.
	 *
	 * Called only when this != other and neither is TOP/BOTTOM
	 * (those cases are handled by BaseLattice).
	 * Since Even and Odd are the only incomparable pair → result is TOP.
	 */
	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// Even ⊔ Odd = ⊤  (only case that reaches here)
		return TOP;
	}

	/**
	 * Partial order: this ≤ other.
	 *
	 * Called only when this != other and neither is TOP/BOTTOM.
	 * Even and Odd are incomparable → always false here.
	 */
	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// Even and Odd are incomparable — neither is below the other
		return false;
	}

	/**
	 * Human-readable representation used in analysis output.
	 */
	@Override
	public StructuredRepresentation representation() {
		if (this == BOTTOM) return Lattice.bottomRepresentation();
		if (this == TOP)    return Lattice.topRepresentation();
		if (this == EVEN)   return new StringRepresentation("Even");
		return new StringRepresentation("Odd");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		return element == ((ParityLattice) obj).element;
	}

	@Override
	public int hashCode() {
		return Objects.hash(element);
	}
}