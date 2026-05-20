package it.unive.scsr.analysis.nullability;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;

public class Nullability implements BaseNonRelationalValueDomain<NullabilityLattice> {

    @Override
    public NullabilityLattice top() { return NullabilityLattice.TOP; }

    @Override
    public NullabilityLattice bottom() { return NullabilityLattice.BOTTOM; }

    @Override
    public NullabilityLattice evalConstant(Constant constant, ProgramPoint pp,
                                           SemanticOracle oracle) throws SemanticException {
        // null literal → Null
        // any other constant (int, string, bool) → NonNull
        if (constant.getValue() == null)
            return NullabilityLattice.NULL;
        return NullabilityLattice.NON_NULL;
    }

    @Override
    public NullabilityLattice evalIdentifier(Identifier id,
                                             ValueEnvironment<NullabilityLattice> environment,
                                             ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        NullabilityLattice val = environment.getState(id);
        // if not tracked yet, assume NonNull (conservative for non-pointer types)
        return val.isBottom() ? NullabilityLattice.NON_NULL : val;
    }

    @Override
    public NullabilityLattice evalUnaryExpression(UnaryExpression expression,
                                                  NullabilityLattice arg, ProgramPoint pp,
                                                  SemanticOracle oracle) throws SemanticException {
        // unary ops (negation etc.) produce non-null numeric results
        return NullabilityLattice.NON_NULL;
    }

    @Override
    public NullabilityLattice evalBinaryExpression(BinaryExpression expression,
                                                   NullabilityLattice left, NullabilityLattice right,
                                                   ProgramPoint pp, SemanticOracle oracle) throws SemanticException {

        // if either operand is definitely null → this operation would
        // throw NullPointerException at runtime → warn but return Top
        if (left.isAlwaysNull() || right.isAlwaysNull())
            return NullabilityLattice.TOP;  // MaybeNull — undefined behavior

        // if either operand is possibly null → result is uncertain
        if (left.isPossiblyNull() || right.isPossiblyNull())
            return NullabilityLattice.TOP;  // MaybeNull

        // both are NonNull → result is NonNull
        return NullabilityLattice.NON_NULL;
    }

    @Override
    public NullabilityLattice evalPushAny(PushAny pushAny, ProgramPoint pp,
                                          SemanticOracle oracle) throws SemanticException {
        // unknown value → could be anything
        return NullabilityLattice.TOP;
    }

    @Override
    public NullabilityLattice fixedVariable(Identifier id, ProgramPoint pp,
                                            SemanticOracle oracle) throws SemanticException {
        return NullabilityLattice.NON_NULL;
    }
}