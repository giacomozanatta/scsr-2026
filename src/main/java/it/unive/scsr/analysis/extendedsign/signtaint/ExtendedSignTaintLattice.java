package it.unive.scsr.analysis.extendedsign.signtaint;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.informationFlow.TaintLattice;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedsign.base.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import java.util.Objects;

/**
 * ExtendedSign x ThreeTaint.
 *
 * Ogni istanza è una coppia (sign, taint), che combina le informazioni di segno
 * ({@link ExtendedSignLattice}) e taint ({@link TaintThreeLevelsLattice}).
 *
 * Partial order: (s1, t1) <= (s2, t2)  <==>  s1 <= s2  and  t1 <= t2
 * Lub: (s1, t1) lub (s2, t2) = (s1 lub s2, t1 lub t2)
 */
public class ExtendedSignTaintLattice implements TaintLattice<ExtendedSignTaintLattice> {

	/**
	 * Entrambe le componenti sono TOP
	 */
	public static final ExtendedSignTaintLattice TOP = new ExtendedSignTaintLattice(
			ExtendedSignLattice.TOP,
			TaintThreeLevelsLattice.TOP
	);

	/**
	 * Entrambe le componenti sono BOTTOM
	 */
	public static final ExtendedSignTaintLattice BOTTOM = new ExtendedSignTaintLattice(
			ExtendedSignLattice.BOTTOM,
			TaintThreeLevelsLattice.BOTTOM
	);

	/**
	 * Elemento tainted: segno sconosciuto (TOP), ma taint certo (TAINT).
	 * Questa costante viene usata per le funzioni "source"
	 */
	public static final ExtendedSignTaintLattice TAINTED_ELEM = new ExtendedSignTaintLattice(
			ExtendedSignLattice.TOP,
			TaintThreeLevelsLattice.TAINT
	);

	/**
	 * Elemento sano: segno sconosciuto (TOP), ma sicuramente non taint (CLEAN).
	 * Questa costante viene usata per le funzioni "sanitizer"
	 */
	public static final ExtendedSignTaintLattice CLEAN_ELEM = new ExtendedSignTaintLattice(
			ExtendedSignLattice.TOP,
			TaintThreeLevelsLattice.CLEAN
	);

	public final ExtendedSignLattice sign;
	public final TaintThreeLevelsLattice taint;

	public ExtendedSignTaintLattice(ExtendedSignLattice sign, TaintThreeLevelsLattice taint) {
		this.sign = sign;
		this.taint = taint;
	}

	/** Di default restituisco BOTTOM. */
	public ExtendedSignTaintLattice() {
		this(ExtendedSignLattice.BOTTOM, TaintThreeLevelsLattice.BOTTOM);
	}

	@Override
	public ExtendedSignTaintLattice top() {
		return TOP;
	}

	@Override
	public ExtendedSignTaintLattice bottom() {
		return BOTTOM;
	}

	@Override
	public ExtendedSignTaintLattice lubAux(ExtendedSignTaintLattice other) throws SemanticException {
		// (s1, t1) lub (s2, t2) = (s1 lub s2, t1 lub t2)
		return new ExtendedSignTaintLattice(
			this.sign.lub(other.sign),
			this.taint.lub(other.taint)
		);
	}

	@Override
	public boolean lessOrEqualAux(ExtendedSignTaintLattice other) throws SemanticException {
		// (s1, t1) <= (s2, t2)  <==>  s1 <= s2  and  t1 <= t2
		return this.sign.lessOrEqual(other.sign)
			&& this.taint.lessOrEqual(other.taint);
	}

	@Override
	public ExtendedSignTaintLattice tainted() {
		return TAINTED_ELEM;
	}

	@Override
	public ExtendedSignTaintLattice clean() {
		return CLEAN_ELEM;
	}

	@Override
	public ExtendedSignTaintLattice or(ExtendedSignTaintLattice other) throws SemanticException {
		// mi trovo in un caso tipo a + b
		return new ExtendedSignTaintLattice(
			this.sign.lub(other.sign), // qui è giusto utilizzare lub, in quanto devo scegliere l'elemento immediatamente superiore o uguale. lub poi chiama lubAux se necessario
			this.taint.or(other.taint) // qui è giusto or: nel caso uno dei due sia tain, allora anche il risultato lo è
		);
	}

	@Override
	public boolean isAlwaysTainted() {
		return taint.isAlwaysTainted();
	}

	@Override
	public boolean isPossiblyTainted() {
		return taint.isPossiblyTainted();
	}

	@Override
	public StructuredRepresentation representation() {
		if (this.equals(BOTTOM)) {
			return Lattice.bottomRepresentation();
		}
		return new StringRepresentation("(" + sign.representation() + ", " + taint.representation() + ")");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ExtendedSignTaintLattice other)) {
			return false;
		}
		return Objects.equals(this.sign, other.sign)
			&& Objects.equals(this.taint, other.taint);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sign, taint);
	}
}
