package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.PushAny;

/*
 * Lattice of taint with three levels
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
		return TaintThreeLevelsLattice.TOP;
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return TaintThreeLevelsLattice.BOTTOM;
	}

	@Override
	protected TaintThreeLevelsLattice tainted() {
		return TaintThreeLevelsLattice.TAINT;
	}

	@Override
	protected TaintThreeLevelsLattice clean() {
		return TaintThreeLevelsLattice.CLEAN;
	}

	@Override
	public TaintThreeLevelsLattice evalPushAny(PushAny pushAny, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		// qualora LiSA non riesca a risalire al valore restituito da una funzione,
		// forzo la restituzione del valore TAINT, così da dare un "falso positivo",
		// indicando che il valore non è sanitizzato
		return tainted();
	}

}
