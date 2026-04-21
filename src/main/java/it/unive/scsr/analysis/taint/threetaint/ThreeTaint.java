package it.unive.scsr.analysis.taint.threetaint;

import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;

/*
 * Lattice of Three taint
 *	 Top 
 * 	/	\
 * C	 T	 
 *  \	/
 *  BOTTOM
 * 
 */
public class ThreeTaint extends BaseTaint<ThreeTaintLattice>{

	
	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);
	@Override
	public ThreeTaintLattice top() {
		// TODO
		return null;
	}
	@Override
	public ThreeTaintLattice bottom() {
		// TODO 
		return null;
	}
	@Override
	protected ThreeTaintLattice tainted() {
		// TODO
		return null;
	}
	@Override
	protected ThreeTaintLattice clean() {
		// TODO
		return null;
	}

	
}
