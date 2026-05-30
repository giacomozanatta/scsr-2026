package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.PushAny;



/**
 * @author Mattia Acquilesi 896827
 * @author Alan Dal Col 895879
 */
public class TaintThreeLevels extends BaseTaint<TaintThreeLevelsLattice> {

	public static final Annotation SINK_ANNOTATION =new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER =new BasicAnnotationMatcher(SINK_ANNOTATION);


	@Override
	public TaintThreeLevelsLattice top() {
		return new TaintThreeLevelsLattice().top();
	}

	@Override
	public TaintThreeLevelsLattice bottom() {
		return new TaintThreeLevelsLattice().bottom();
	}

	@Override
	protected TaintThreeLevelsLattice tainted() {
		return new TaintThreeLevelsLattice().tainted();
	}

	@Override
	protected TaintThreeLevelsLattice clean() {
		return new TaintThreeLevelsLattice().clean();
	}

	@Override
	public TaintThreeLevelsLattice evalPushAny(
			PushAny pushAny,
			ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		return tainted();
	}
}
