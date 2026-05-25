package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;

/**
 * Taint analysis domain with three levels.
 *
 * BaseTaint handles the propagation rules automatically —
 * we only need to tell it which lattice elements represent
 * tainted, clean, top, and bottom.
 *
 * Source functions produce TAINTED values.
 * Sanitizer functions produce CLEAN values.
 * Arithmetic between TAINTED and CLEAN produces TOP.
 */
public class TaintThreeLevels extends BaseTaint<TaintThreeLevelsLattice> {

	public static final Annotation SINK_ANNOTATION =
			new Annotation("lisa.taint.Sink");

	public static final AnnotationMatcher SINK_MATCHER =
			new BasicAnnotationMatcher(SINK_ANNOTATION);

	@Override
	public TaintThreeLevelsLattice top() {
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return TaintThreeLevelsLattice.BOTTOM;
	}

	@Override
	protected TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.TAINTED;
	}

	@Override
	protected TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.CLEAN;
	}
}