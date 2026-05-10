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
public class ExtendedSignTaint extends BaseTaint<ExtendedSignTaintLattice> {

	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

	@Override
	public ExtendedSignTaintLattice top() {
		return ExtendedSignTaintLattice.TOP;
	}

	@Override
	public ExtendedSignTaintLattice bottom() {
		return ExtendedSignTaintLattice.BOTTOM;
	}

	@Override
	protected ExtendedSignTaintLattice tainted() {
		return ExtendedSignTaintLattice.TAINTED_ELEM;
	}

	@Override
	protected ExtendedSignTaintLattice clean() {
		return ExtendedSignTaintLattice.CLEAN_ELEM;
	}

	@Override
	public ExtendedSignTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return new ExtendedSignTaintLattice(ExtendedSignUtils.evalConstant(constant), TaintThreeLevelsLattice.CLEAN);
	}

	@Override
	public ExtendedSignTaintLattice evalPushAny(PushAny pushAny, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return tainted();
	}

	@Override
	public ExtendedSignTaintLattice evalUnaryExpression(UnaryExpression expression,
			ExtendedSignTaintLattice arg, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		ExtendedSignLattice signResult;
		if (expression.getOperator() == NumericNegation.INSTANCE) {
			signResult = ExtendedSignUtils.negate(arg.sign);
		} else {
			signResult = ExtendedSignLattice.TOP;
		}
		return new ExtendedSignTaintLattice(signResult, arg.taint);
	}

	@Override
	public ExtendedSignTaintLattice evalBinaryExpression(BinaryExpression expression,
			ExtendedSignTaintLattice left, ExtendedSignTaintLattice right,
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

		return new ExtendedSignTaintLattice(signResult, taintResult);
	}
}
