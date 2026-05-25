package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.informationFlow.TaintLattice;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

/**
 * Three-level taint lattice.
 *
 * Each variable at a program point is abstracted to one of four states:
 *
 *         ⊤  (unknown — might be tainted or clean)
 *        / \
 *       T   C  (definitely tainted / definitely clean)
 *        \ /
 *         ⊥  (unreachable)
 *
 * T and C are incomparable: their join is ⊤.
 */
public class TaintThreeLevelsLattice implements TaintLattice<TaintThreeLevelsLattice> {

	// We encode the four states as an integer level:
	// 0 = BOTTOM, 1 = CLEAN, 2 = TAINTED, 3 = TOP
	// This encoding lets us avoid byte casts and makes intent clearer.
	private enum Level { BOTTOM, CLEAN, TAINTED, TOP }

	public static final TaintThreeLevelsLattice BOTTOM  = new TaintThreeLevelsLattice(Level.BOTTOM);
	public static final TaintThreeLevelsLattice CLEAN   = new TaintThreeLevelsLattice(Level.CLEAN);
	public static final TaintThreeLevelsLattice TAINTED = new TaintThreeLevelsLattice(Level.TAINTED);
	public static final TaintThreeLevelsLattice TOP     = new TaintThreeLevelsLattice(Level.TOP);

	private final Level level;

	/** Default constructor — conservative, starts at TOP. */
	public TaintThreeLevelsLattice() {
		this(Level.TOP);
	}

	private TaintThreeLevelsLattice(Level level) {
		this.level = level;
	}

	// --- TaintLattice interface ---

	@Override
	public TaintThreeLevelsLattice tainted() { return TAINTED; }

	@Override
	public TaintThreeLevelsLattice clean() { return CLEAN; }

	/**
	 * Returns true only when this value is definitely tainted —
	 * used by the checker to emit definite warnings.
	 */
	@Override
	public boolean isAlwaysTainted() { return level == Level.TAINTED; }

	/**
	 * Returns true when the taint status is unknown (TOP) —
	 * used by the checker to emit possible warnings.
	 */
	@Override
	public boolean isPossiblyTainted() { return level == Level.TOP; }

	// --- Lattice interface ---

	@Override
	public TaintThreeLevelsLattice top() { return TOP; }

	@Override
	public TaintThreeLevelsLattice bottom() { return BOTTOM; }

	/**
	 * Called only when this and other are incomparable.
	 * The only incomparable pair in this lattice is TAINTED and CLEAN,
	 * whose least upper bound is TOP.
	 */
	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other)
			throws SemanticException {
		return TOP;
	}

	/**
	 * Widening equals lub here since the lattice has finite height.
	 */
	@Override
	public TaintThreeLevelsLattice wideningAux(TaintThreeLevelsLattice other)
			throws SemanticException {
		return TOP;
	}

	/**
	 * Called only for incomparable elements — TAINTED and CLEAN are
	 * never in a ≤ relationship, so always false.
	 */
	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other)
			throws SemanticException {
		return false;
	}

	/**
	 * Combines taint from two sub-expressions (e.g. x + y).
	 *
	 * Rules:
	 *   TAINTED or anything = TAINTED  (taint always wins)
	 *   TOP     or anything = TOP      (uncertainty propagates)
	 *   CLEAN   or CLEAN    = CLEAN
	 */
	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other)
			throws SemanticException {
		// Taint is "contagious" — if either side is tainted, result is tainted
		if (this.isAlwaysTainted() || other.isAlwaysTainted())
			return TAINTED;

		// If either side is unknown, result is unknown
		if (this.isPossiblyTainted() || other.isPossiblyTainted())
			return TOP;

		// Both sides are clean
		return CLEAN;
	}

	// --- Representation ---

	@Override
	public StructuredRepresentation representation() {
		switch (level) {
			case BOTTOM:  return Lattice.bottomRepresentation();
			case CLEAN:   return new StringRepresentation("clean");
			case TAINTED: return new StringRepresentation("tainted");
			default:      return Lattice.topRepresentation();
		}
	}

	@Override
	public String toString() { return representation().toString(); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TaintThreeLevelsLattice)) return false;
		return level == ((TaintThreeLevelsLattice) obj).level;
	}

	@Override
	public int hashCode() { return Objects.hash(level); }
}