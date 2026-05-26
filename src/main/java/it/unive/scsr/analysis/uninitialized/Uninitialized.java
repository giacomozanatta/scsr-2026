package it.unive.scsr.analysis.uninitialized;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;

public class Uninitialized implements BaseNonRelationalValueDomain<UninitializedLattice> {

    @Override
    public UninitializedLattice top() {
        return UninitializedLattice.TOP;
    }

    @Override
    public UninitializedLattice bottom() {
        return UninitializedLattice.BOTTOM;
    }

    @Override
    public UninitializedLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        // This is a workaround since we cannot use def z; without assigning it an actual value.
        if (constant.getValue() instanceof Integer value && value == -1) {
            return UninitializedLattice.UNINITIALIZED;
        }
        return UninitializedLattice.INITIALIZED;
    }

    @Override
    public UninitializedLattice evalUnaryExpression(UnaryExpression expression, UninitializedLattice arg,
                                                    ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return arg;
    }

    @Override
    public UninitializedLattice evalBinaryExpression(BinaryExpression expression, UninitializedLattice left,
                                                     UninitializedLattice right, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        if (left.isTop() || right.isTop())
            return UninitializedLattice.TOP;
        if (left.isBottom() || right.isBottom())
            return UninitializedLattice.BOTTOM;

        return left.lub(right);
    }
}