package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.scsr.analysis.sign.Sign;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignTaint implements BaseNonRelationalValueDomain<SignTaintLattice> {

    private final Sign sign = new Sign();
    private final TaintThreeLevels taint = new TaintThreeLevels();

    @Override
    public SignTaintLattice top() {
        return new SignTaintLattice(sign.top(), taint.top());
    }

    @Override
    public SignTaintLattice bottom() {
        return new SignTaintLattice(sign.bottom(), taint.bottom());
    }

    @Override
    public SignTaintLattice evalConstant(Constant c, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new SignTaintLattice(
                sign.evalConstant(c, pp, oracle),
                taint.evalConstant(c, pp, oracle)
        );
    }

    // evalIdentifier must be overridden because BaseTaint.evalIdentifier checks
    // id.getAnnotations() for @Tainted/@Clean and returns TAINT/CLEAN accordingly.
    // The default BaseNonRelationalDomain.evalIdentifier just does environment.getState(id),
    // so taint annotations on return-value identifiers (propagated from function descriptors
    // by LiSA) would be silently ignored, keeping taint always CLEAN.
    @Override
    public SignTaintLattice evalIdentifier(
            Identifier id,
            ValueEnvironment<SignTaintLattice> environment,
            ProgramPoint pp,
            SemanticOracle oracle) throws SemanticException {

        // Ask the taint sub-domain whether this identifier is annotated.
        // taint.fixedVariable replicates BaseTaint.defaultApprox: it checks
        // id.getAnnotations() and returns TAINT, CLEAN, or bottom() if absent.
        TaintThreeLevelsLattice taintAnnotation = taint.fixedVariable(id, pp, oracle);

        if (!taintAnnotation.isBottom()) {
            // Annotation found: override the taint component, keep the sign from the env.
            // This preserves sign precision (e.g. source1() returning -99 stays NEG)
            // while correctly marking the value as tainted/clean.
            SignTaintLattice envValue = environment.getState(id);
            if (envValue.isBottom()) return bottom();
            return new SignTaintLattice(envValue.first, taintAnnotation);
        }

        // No taint annotation: standard environment lookup.
        return environment.getState(id);
    }

    @Override
    public SignTaintLattice evalUnaryExpression(
            UnaryExpression expr, SignTaintLattice arg,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new SignTaintLattice(
                sign.evalUnaryExpression(expr, arg.first, pp, oracle),
                taint.evalUnaryExpression(expr, arg.second, pp, oracle)
        );
    }

    @Override
    public SignTaintLattice evalBinaryExpression(
            BinaryExpression expr,
            SignTaintLattice left, SignTaintLattice right,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new SignTaintLattice(
                sign.evalBinaryExpression(expr, left.first, right.first, pp, oracle),
                taint.evalBinaryExpression(expr, left.second, right.second, pp, oracle)
        );
    }

    // Satisfiability and assume: only the sign component informs comparison guards.
    // The taint component is preserved unchanged.

    @Override
    public Satisfiability satisfiesBinaryExpression(
            BinaryExpression expr,
            SignTaintLattice left, SignTaintLattice right,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return sign.satisfiesBinaryExpression(expr, left.first, right.first, pp, oracle);
    }

    @Override
    public ValueEnvironment<SignTaintLattice> assumeBinaryExpression(
            ValueEnvironment<SignTaintLattice> env,
            BinaryExpression expression,
            ProgramPoint src, ProgramPoint dest,
            SemanticOracle oracle) throws SemanticException {

        Satisfiability sat = satisfies(env, expression, src, oracle);
        if (sat == Satisfiability.NOT_SATISFIED)
            return env.bottom();
        if (sat == Satisfiability.SATISFIED)
            return env;

        BinaryOperator operator = expression.getOperator();
        ValueExpression left  = (ValueExpression) expression.getLeft();
        ValueExpression right = (ValueExpression) expression.getRight();

        Identifier id;
        SignTaintLattice evalProduct;
        boolean rightIsExpr;

        if (left instanceof Identifier) {
            evalProduct = eval(env, right, src, oracle);
            id = (Identifier) left;
            rightIsExpr = true;
        } else if (right instanceof Identifier) {
            evalProduct = eval(env, left, src, oracle);
            id = (Identifier) right;
            rightIsExpr = false;
        } else
            return env;

        SignTaintLattice starting = env.getState(id);
        if (evalProduct.isBottom() || starting.isBottom())
            return env.bottom();

        // Extract the sign component of the evaluated expression and the current state.
        // The taint component is not refined by comparison guards.
        SignLattice evalSign     = evalProduct.first;
        SignLattice startingSign = starting.first;
        TaintThreeLevelsLattice startingTaint = starting.second;

        SignLattice signUpdate = null;

        if (operator == ComparisonEq.INSTANCE) {
            signUpdate = startingSign.glb(evalSign);
        } else {
            // For each atom a in {NEG, ZERO, POS}: if a could satisfy the constraint
            // against evalSign, keep the part of startingSign that overlaps with a.
            // The final update is the lub of all such filtered parts.
            SignLattice[] atoms = { SignLattice.NEG, SignLattice.ZERO, SignLattice.POS};

            if (operator == ComparisonGe.INSTANCE) {
                if (rightIsExpr) {
                    for (SignLattice a : atoms)
                        if (a.gt(evalSign).or(a.eq(evalSign)).mightBeTrue())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                } else {
                    for (SignLattice a : atoms)
                        if (evalSign.gt(a).or(evalSign.eq(a)).mightBeTrue())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                }
            } else if (operator == ComparisonLe.INSTANCE) {
                if (rightIsExpr) {
                    for (SignLattice a : atoms)
                        if (a.gt(evalSign).mightBeFalse())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                } else {
                    for (SignLattice a : atoms)
                        if (evalSign.gt(a).mightBeFalse())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                }
            } else if (operator == ComparisonLt.INSTANCE) {
                if (rightIsExpr) {
                    for (SignLattice a : atoms)
                        if (a.gt(evalSign).or(a.eq(evalSign)).mightBeFalse())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                } else {
                    for (SignLattice a : atoms)
                        if (evalSign.gt(a).or(evalSign.eq(a)).mightBeFalse())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                }
            } else if (operator == ComparisonGt.INSTANCE) {
                if (rightIsExpr) {
                    for (SignLattice a : atoms)
                        if (a.gt(evalSign).mightBeTrue())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                } else {
                    for (SignLattice a : atoms)
                        if (evalSign.gt(a).mightBeTrue())
                            signUpdate = signUpdate == null ? startingSign.glb(a) : signUpdate.lub(startingSign.glb(a));
                }
            }
        }

        if (signUpdate == null)
            return env;
        if (signUpdate.isBottom())
            return env.bottom();
        return env.putState(id, new SignTaintLattice(signUpdate, startingTaint));
    }
}