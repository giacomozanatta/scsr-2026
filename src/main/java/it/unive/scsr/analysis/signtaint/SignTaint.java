package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.scsr.analysis.sign.extended.ExtendedSign;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignTaint implements BaseNonRelationalValueDomain<SignTaintLattice> {

	private final ExtendedSign extendedSignDomain = new ExtendedSign();
	private final TaintThreeLevels taintsDomain = new TaintThreeLevels();

	private static final Logger LOG = LogManager.getLogger(SignTaint.class);

	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

	@Override
	public SignTaintLattice top() {
		return SignTaintLattice.TOP;
	}

	@Override
	public SignTaintLattice bottom() {
		return SignTaintLattice.BOTTOM;
	}

	@Override
	public SignTaintLattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return new SignTaintLattice(
				extendedSignDomain.evalConstant(constant, pp, oracle),
				taintsDomain.evalConstant(constant, pp, oracle)
		);
	}

	@Override
	public SignTaintLattice evalUnaryExpression(UnaryExpression expression, SignTaintLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return new SignTaintLattice(
				extendedSignDomain.evalUnaryExpression(expression, arg.signLattice, pp, oracle),
				taintsDomain.evalUnaryExpression(expression, arg.taintLattice, pp, oracle)
		);
	}

	@Override
	public SignTaintLattice evalBinaryExpression(BinaryExpression expression, SignTaintLattice left, SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return new SignTaintLattice(
				extendedSignDomain.evalBinaryExpression(expression, left.signLattice,
						right.signLattice, pp, oracle),
				taintsDomain.evalBinaryExpression(expression, left.taintLattice,
						right.taintLattice, pp, oracle)
		);
	}

	// Stuff required for Taint Analysis to work properly

	@Override
	public SignTaintLattice evalIdentifier(Identifier id, ValueEnvironment<SignTaintLattice> environment, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

		SignTaintLattice state = environment.getState(id);

		TaintThreeLevelsLattice def = state.taintLattice;
		// Simplified copy of BaseTaint.defaultApprox
		// Basically propagate taint information, if present
		Annotations annots = id.getAnnotations();
		if (annots.contains(TaintThreeLevels.TAINTED_MATCHER))
			def = TaintThreeLevelsLattice.TAINT;
		else if (annots.contains(TaintThreeLevels.CLEAN_MATCHER))
			def = TaintThreeLevelsLattice.CLEAN;

		return new SignTaintLattice(
				state.signLattice,
				def
		);
	}

	@Override
	public SignTaintLattice evalPushAny(PushAny pushAny, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return new SignTaintLattice(
				extendedSignDomain.evalPushAny(pushAny, pp, oracle),
				taintsDomain.evalPushAny(pushAny, pp, oracle)
		);
	}
}
