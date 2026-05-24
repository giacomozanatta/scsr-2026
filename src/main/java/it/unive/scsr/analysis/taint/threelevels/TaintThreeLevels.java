package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;

/*
 * Lattice of  taint with three levels
 *	 Top
 * 	/	\
 * C	 T
 *  \	/
 *  BOTTOM
 *
 */
public class TaintThreeLevels extends BaseTaint<TaintThreeLevelsLattice> {

	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

	@Override
	public TaintThreeLevelsLattice top() {
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return TaintThreeLevelsLattice.BOTTOM;
	}

	// Changed visibility to public just to make it easily callable from SignTaint class
	@Override
	public TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.TAINT;
	}

	// Changed visibility to public just to make it easily callable from SignTaint class
	@Override
	public TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.CLEAN;
	}

	// Changed visibility to public just to make it easily callable from SignTaint class
	@Override
	public TaintThreeLevelsLattice defaultApprox(
	Identifier id,
	ProgramPoint pp,
	SemanticOracle oracle)
			throws SemanticException {
		return super.defaultApprox(id, pp, oracle);
	}
}
