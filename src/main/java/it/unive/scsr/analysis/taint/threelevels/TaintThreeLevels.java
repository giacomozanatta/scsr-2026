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
public class TaintThreeLevels extends BaseTaint<TaintThreeLevelsLattice>{

	
	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);
	@Override
	public TaintThreeLevelsLattice top() {
		// TODO
		return null;
	}
	@Override
	public TaintThreeLevelsLattice bottom() {
		// TODO 
		return null;
	}
	@Override
	protected TaintThreeLevelsLattice tainted() {
		// TODO
		return null;
	}
	@Override
	protected TaintThreeLevelsLattice clean() {
		// TODO
		return null;
	}

	
}
