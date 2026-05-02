package it.unive.scsr.analysis.taint;

import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;

/*
 * Lattice of Taint Domain 
 * 
 * T
 * |
 * C
 * |
 * BOTTOM
 * 
 */
public class Taint extends BaseTaint<TaintLattice>{

	
	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);
	
	
	@Override
	public TaintLattice top() {
		return TaintLattice.Taint;
	}

	@Override
	public TaintLattice bottom() {
		return TaintLattice.Bottom;
	}

	@Override
	protected TaintLattice tainted() {
		return TaintLattice.Taint;
	}

	@Override
	protected TaintLattice clean() {
		return TaintLattice.Clean;
	}

}