package it.unive.scsr.analysis.sign.extended;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {

    @Override
    public ExtendedSignLattice top() {
        return ExtendedSignLattice.TOP;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return ExtendedSignLattice.BOTTOM;
    }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return null;
    }

    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return null;

    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return null;

    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return null;

    }

    @Override
    public ValueEnvironment<ExtendedSignLattice> assumeBinaryExpression(ValueEnvironment<ExtendedSignLattice> environment, BinaryExpression expression, ProgramPoint src, ProgramPoint dest, SemanticOracle oracle) throws SemanticException {
        return null;

    }
}
