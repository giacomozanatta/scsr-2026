package it.unive.scsr.analysis.extendedsign.signtaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.ModuloOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.RemainderOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.scsr.analysis.extendedsign.ExtendedSignUtils;
import it.unive.scsr.analysis.extendedsign.base.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

/**
 * ExtendedSign x ThreeTaint.
 *
 * Per il componente segno, viene usato {@link ExtendedSignUtils}, servizio condiviso con ExtendedSign per evitare codice duplicato.
 * Per il componente taint, viene usato il metodo "or" {@link TaintThreeLevelsLattice}
 */
public class ExtendedSignThreeTaint extends BaseTaint<ExtendedSignThreeTaintLattice> {

	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

	@Override
	public ExtendedSignThreeTaintLattice top() {
		return ExtendedSignThreeTaintLattice.TOP;
	}

	@Override
	public ExtendedSignThreeTaintLattice bottom() {
		return ExtendedSignThreeTaintLattice.BOTTOM;
	}

	@Override
	protected ExtendedSignThreeTaintLattice tainted() {
		return ExtendedSignThreeTaintLattice.TAINTED_ELEM;
	}

	@Override
	protected ExtendedSignThreeTaintLattice clean() {
		return ExtendedSignThreeTaintLattice.CLEAN_ELEM;
	}

	@Override
	public ExtendedSignThreeTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return new ExtendedSignThreeTaintLattice(ExtendedSignUtils.evalConstant(constant), TaintThreeLevelsLattice.CLEAN);
	}

	@Override
	public ExtendedSignThreeTaintLattice evalPushAny(PushAny pushAny, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return tainted();
	}

	@Override
	public ExtendedSignThreeTaintLattice evalUnaryExpression(UnaryExpression expression,
	                                                         ExtendedSignThreeTaintLattice arg, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		ExtendedSignLattice signResult;
		if (expression.getOperator() == NumericNegation.INSTANCE) {
			signResult = ExtendedSignUtils.negate(arg.sign);
		} else {
			signResult = ExtendedSignLattice.TOP;
		}
		return new ExtendedSignThreeTaintLattice(signResult, arg.taint);
	}

	@Override
	public ExtendedSignThreeTaintLattice evalBinaryExpression(BinaryExpression expression,
	                                                          ExtendedSignThreeTaintLattice left, ExtendedSignThreeTaintLattice right,
	                                                          ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		ExtendedSignLattice signResult;
		if (expression.getOperator() instanceof AdditionOperator) {
			signResult = ExtendedSignUtils.add(left.sign, right.sign);
		} else if (expression.getOperator() instanceof SubtractionOperator) {
			signResult = ExtendedSignUtils.add(left.sign, ExtendedSignUtils.negate(right.sign));
		} else if (expression.getOperator() instanceof MultiplicationOperator) {
			signResult = ExtendedSignUtils.multiply(left.sign, right.sign);
		} else if (expression.getOperator() instanceof DivisionOperator) {
			signResult = ExtendedSignUtils.divide(left.sign, right.sign);
		} else if (expression.getOperator() instanceof ModuloOperator) {
			signResult = ExtendedSignUtils.modulo(left.sign, right.sign);
		} else if (expression.getOperator() instanceof RemainderOperator) {
			signResult = ExtendedSignUtils.remainder(left.sign, right.sign);
		} else {
			signResult = ExtendedSignLattice.TOP;
		}

		TaintThreeLevelsLattice taintResult;
		try {
			taintResult = left.taint.or(right.taint);
		} catch (Exception e) {
			taintResult = TaintThreeLevelsLattice.TOP;
		}

		return new ExtendedSignThreeTaintLattice(signResult, taintResult);
	}
}
