package it.unive.scsr.analysis.sign_three_taint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonNe;
import it.unive.scsr.analysis.sign.Sign;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

/**
 * Cartesian product domain: Sign x TaintThreeLevels.
 *
 * Sign component: abstract sign of the value.
 * Taint component: information-flow label (CLEAN / TAINT / TOP / BOTTOM).
 *
 * Semantics:
 *   constants -> (sign(c),  CLEAN)
 *   identifiers  -> env lookup
 *   source annotation -> (TOP, TAINT)
 *   unary ops -> (sign(op,s), t) taint propagates unchanged
 *   binary ops -> (sign(op,s1,s2), t1 ∨ t2) taint propagates if any operand is tainted
 */
public class SignThreeTaint implements BaseNonRelationalValueDomain<SignThreeTaintLattice> {
    private final Sign signDomain = new Sign();

    @Override
    public SignThreeTaintLattice top() { 
        return SignThreeTaintLattice.TOP;
    }

    @Override
    public SignThreeTaintLattice bottom() { 
        return SignThreeTaintLattice.BOTTOM;
    }

    @Override
    public SignThreeTaintLattice evalConstant(
        Constant constant, ProgramPoint pp, SemanticOracle oracle
    ) throws SemanticException {
        SignLattice s = signDomain.evalConstant(constant, pp, oracle);

        return new SignThreeTaintLattice(s, TaintThreeLevelsLattice.CLEAN);
    }

    @Override
    public SignThreeTaintLattice evalIdentifier(
        Identifier id, ValueEnvironment<SignThreeTaintLattice> env, ProgramPoint pp, SemanticOracle oracle
    ) throws SemanticException {

        // Source: anything flowing through this identifier is tainted
        if (id.getAnnotations().contains(BaseTaint.TAINTED_MATCHER))
            return new SignThreeTaintLattice(SignLattice.TOP, TaintThreeLevelsLattice.TAINT);

        // Sanitizer: taint is removed, sign is preserved from the environment
        if (id.getAnnotations().contains(BaseTaint.CLEAN_MATCHER)) {
            SignThreeTaintLattice current = env.getState(id);
            return new SignThreeTaintLattice(current.getSign(), TaintThreeLevelsLattice.CLEAN);
        }

        return env.getState(id);
    }

    @Override
    public SignThreeTaintLattice evalUnaryExpression(
        UnaryExpression expression, SignThreeTaintLattice arg, ProgramPoint pp, SemanticOracle oracle
    ) throws SemanticException {
        if (arg.isBottom()) {
            return bottom();
        }

        SignLattice s = signDomain.evalUnaryExpression(expression, arg.getSign(), pp, oracle);

        // Taint flows through: if operand is tainted, result is tainted
        return new SignThreeTaintLattice(s, arg.getTaint());
    }

    @Override
    public SignThreeTaintLattice evalBinaryExpression(
        BinaryExpression expression, SignThreeTaintLattice left, SignThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle
    ) throws SemanticException {

        if (left.isBottom() || right.isBottom()) {
            return bottom();
        }

        SignLattice s = signDomain.evalBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);

        // Taint propagates: if either operand is tainted, result is tainted
        TaintThreeLevelsLattice t = left.getTaint().or(right.getTaint());

        return new SignThreeTaintLattice(s, t);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(
        BinaryExpression expression, SignThreeTaintLattice left, SignThreeTaintLattice right, ProgramPoint pp, SemanticOracle oracle
    ) {
        // Satisfiability — uses only the sign component
        return signDomain.satisfiesBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);
    }

    // Assumption — refines the sign component, taint is unchanged

    @Override
    public ValueEnvironment<SignThreeTaintLattice> assumeBinaryExpression(
        ValueEnvironment<SignThreeTaintLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle
    ) throws SemanticException {
        Satisfiability sat = satisfies(environment, expression, src, oracle);

        if (sat == Satisfiability.NOT_SATISFIED){
            return environment.bottom();
        }

        if (sat == Satisfiability.SATISFIED){
            return environment;
        }

        BinaryOperator operator = expression.getOperator();
        ValueExpression left = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();
        Identifier id;
        SignThreeTaintLattice evalPair;
        boolean rightIsExpr;

        if (left instanceof Identifier) {
            evalPair = eval(environment, right, src, oracle);
            id = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            evalPair = eval(environment, left, src, oracle);
            id = (Identifier) right;
            rightIsExpr = false;
        } else {
            return environment;
        }

        SignThreeTaintLattice starting = environment.getState(id);

        if (evalPair.isBottom() || starting.isBottom()){
            return environment.bottom();
        }

        SignLattice signEval = evalPair.getSign();
        SignLattice signStarting = starting.getSign();
        SignLattice signUpdate = null;

        if (operator == ComparisonEq.INSTANCE) {
            signUpdate = signStarting.glb(signEval);
        } else {
            SignLattice[] all = { 
                SignLattice.NEG,
                SignLattice.ZERO, 
                SignLattice.POS
            };

            if (operator == ComparisonGe.INSTANCE) {
                for (SignLattice s : all)
                    if ((rightIsExpr ? s.gt(signEval).or(s.eq(signEval)): signEval.gt(s).or(signEval.eq(s))).mightBeTrue())
                        signUpdate = signUpdate == null ? signStarting.glb(s) : signUpdate.lub(signStarting.glb(s));
            } else if (operator == ComparisonGt.INSTANCE) {
                for (SignLattice s : all)
                    if ((rightIsExpr ? s.gt(signEval) : signEval.gt(s)).mightBeTrue())
                        signUpdate = signUpdate == null ? signStarting.glb(s) : signUpdate.lub(signStarting.glb(s));
            } else if (operator == ComparisonLe.INSTANCE) {
                for (SignLattice s : all)
                    if ((rightIsExpr ? s.gt(signEval) : signEval.gt(s)).mightBeFalse())
                        signUpdate = signUpdate == null ? signStarting.glb(s) : signUpdate.lub(signStarting.glb(s));
            } else if (operator == ComparisonLt.INSTANCE) {
                for (SignLattice s : all)
                    if ((rightIsExpr ? s.gt(signEval).or(s.eq(signEval)) : signEval.gt(s).or(signEval.eq(s))).mightBeFalse())
                        signUpdate = signUpdate == null ? signStarting.glb(s) : signUpdate.lub(signStarting.glb(s));
            } else if (operator == ComparisonNe.INSTANCE) {
                for (SignLattice s : all)
                    if (s.eq(signEval).mightBeFalse()) 
                        signUpdate = signUpdate == null ? signStarting.glb(s) : signUpdate.lub(signStarting.glb(s));
            }
        }

        if (signUpdate == null){
            return environment;
        }

        if (signUpdate.isBottom()){
            return environment.bottom();
        }

        // Taint component is unchanged by sign refinement
        SignThreeTaintLattice updated = new SignThreeTaintLattice(signUpdate, starting.getTaint());

        return environment.putState(id, updated);
    }
}
