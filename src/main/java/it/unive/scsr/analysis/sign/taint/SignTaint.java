package it.unive.scsr.analysis.sign.taint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.scsr.analysis.sign.extended.ExtendedSign;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignTaint implements BaseNonRelationalValueDomain<SignTaintLattice> {

	private final ExtendedSign sign = new ExtendedSign();
	private final TaintThreeLevels taint = new TaintThreeLevels();

	@Override
	public SignTaintLattice top() {
		return SignTaintLattice.TOP;
	}

	@Override
	public SignTaintLattice bottom() {
		return SignTaintLattice.BOTTOM;
	}

	@Override
	public SignTaintLattice fixedVariable(Identifier id, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return new SignTaintLattice(sign.fixedVariable(id, pp, oracle), taint.fixedVariable(id, pp, oracle));
	}

	@Override
	public SignTaintLattice evalIdentifier(Identifier id, ValueEnvironment<SignTaintLattice> environment,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		SignTaintLattice value = environment.getState(id);
		SignTaintLattice fixed = fixedVariable(id, pp, oracle);
		if (!fixed.getSign().isBottom() || !fixed.getTaint().isBottom()) {
			ExtendedSignLattice signValue = fixed.getSign().isBottom() ? value.getSign() : fixed.getSign();
			TaintThreeLevelsLattice taintValue = fixed.getTaint().isBottom() ? value.getTaint() : fixed.getTaint();
			return new SignTaintLattice(signValue, taintValue);
		}
		return value;
	}

	@Override
	public SignTaintLattice evalPushAny(PushAny pushAny, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return new SignTaintLattice(sign.evalPushAny(pushAny, pp, oracle), taint.evalPushAny(pushAny, pp, oracle));
	}

	@Override
	public SignTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return new SignTaintLattice(sign.evalConstant(constant, pp, oracle), taint.evalConstant(constant, pp, oracle));
	}

	@Override
	public SignTaintLattice evalUnaryExpression(UnaryExpression expression, SignTaintLattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException {
		return new SignTaintLattice(sign.evalUnaryExpression(expression, arg.getSign(), pp, oracle),
				taint.evalUnaryExpression(expression, arg.getTaint(), pp, oracle));
	}

	@Override
	public SignTaintLattice evalBinaryExpression(BinaryExpression expression, SignTaintLattice left,
			SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return new SignTaintLattice(sign.evalBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle),
				taint.evalBinaryExpression(expression, left.getTaint(), right.getTaint(), pp, oracle));
	}

	@Override
	public SignTaintLattice evalTernaryExpression(TernaryExpression expression, SignTaintLattice left,
			SignTaintLattice middle, SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		return new SignTaintLattice(ExtendedSignLattice.TOP,
				taint.evalTernaryExpression(expression, left.getTaint(), middle.getTaint(), right.getTaint(), pp, oracle));
	}

	@Override
	public SignTaintLattice evalTypeCast(BinaryExpression cast, SignTaintLattice left, SignTaintLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return left;
	}

	@Override
	public SignTaintLattice evalTypeConv(BinaryExpression conv, SignTaintLattice left, SignTaintLattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return left;
	}

	@Override
	public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, SignTaintLattice left,
			SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
		return sign.satisfiesBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);
	}

	@Override
	public ValueEnvironment<SignTaintLattice> assume(ValueEnvironment<SignTaintLattice> environment,
			ValueExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
		return environment;
	}
}
