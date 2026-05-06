package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;

/*
 * Lattice of  taint with three levels
 *	 Top 
 * 	/	\
 * C	 T	 
 *  \	/
 *  BOTTOM
 * 
 */
//Work done by Elia Stevanato 895598 & Francesco Pasqualato 897778

public class TaintThreeLevels extends BaseTaint<TaintThreeLevelsLattice>{

	
	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);
	@Override
	public TaintThreeLevelsLattice top() {
		return TaintThreeLevelsLattice.Top;
	}
	@Override
	public TaintThreeLevelsLattice bottom() {
		return TaintThreeLevelsLattice.Bottom;
	}
	@Override
	protected TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.Taint;
	}
	@Override
	protected TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.Clean;

	}

	
}
