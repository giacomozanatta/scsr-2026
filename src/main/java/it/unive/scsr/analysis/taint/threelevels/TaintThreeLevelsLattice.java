package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/**
 * Three-level taint lattice (same shape as LiSA {@code ThreeTaint}):
 *
 * <pre>
 *        Top (⊤, possibly tainted or clean)
 *       / \
 *   Clean  Tainted
 *       \ /
 *     Bottom (⊥)
 * </pre>
 *
 * {@code ⊤} means the value might be tainted or clean along different paths.
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

	public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice((byte) 3);

	public static final TaintThreeLevelsLattice TAINTED = new TaintThreeLevelsLattice((byte) 2);

	public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice((byte) 1);

	public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice((byte) 0);

	private final byte level;

	public TaintThreeLevelsLattice() {
		this((byte) 3);
	}

	private TaintThreeLevelsLattice(byte level) {
		this.level = level;
	}

	@Override
	public TaintThreeLevelsLattice top() {
		return TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this == BOTTOM)
			return Lattice.bottomRepresentation();
		if (this == CLEAN)
			return new StringRepresentation("_");
		if (this == TAINTED)
			return new StringRepresentation("#");
		return Lattice.topRepresentation();
	}

	@Override
	public TaintThreeLevelsLattice tainted() {
		return TAINTED;
	}

	@Override
	public TaintThreeLevelsLattice clean() {
		return CLEAN;
	}

	@Override
	public boolean isAlwaysTainted() {
		return this == TAINTED;
	}

	@Override
	public boolean isPossiblyTainted() {
		return this == TOP;
	}

	@Override
	public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
		// Clean and tainted are incomparable: join is ⊤
		return TOP;
	}

	@Override
	public TaintThreeLevelsLattice wideningAux(TaintThreeLevelsLattice other) throws SemanticException {
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
		return false;
	}

	@Override
	public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
		if (this == TAINTED || other == TAINTED)
			return TAINTED;
		if (this == TOP || other == TOP)
			return TOP;
		return CLEAN;
	}

	@Override
	public int hashCode() {
		return level;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		return level == ((TaintThreeLevelsLattice) obj).level;
	}

	@Override
	public String toString() {
		return representation().toString();
	}
}
