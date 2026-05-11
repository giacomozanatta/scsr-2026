package it.unive.scsr.analysis.extendedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

public class ExtendedSign implements BaseNonRelationalValueDomain<ExtendedSignLattice> {
    @Override
    public ExtendedSignLattice evalUnaryExpression(UnaryExpression expression, ExtendedSignLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return BaseNonRelationalValueDomain.super.evalUnaryExpression(expression, arg, pp, oracle);
    }

    @Override
    public ExtendedSignLattice evalBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return BaseNonRelationalValueDomain.super.evalBinaryExpression(expression, left, right, pp, oracle);
    }

    @Override
    public ExtendedSignLattice evalTernaryExpression(TernaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice middle, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return BaseNonRelationalValueDomain.super.evalTernaryExpression(expression, left, middle, right, pp, oracle);
    }

    @Override
    public ExtendedSignLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return BaseNonRelationalValueDomain.super.evalConstant(constant, pp, oracle);
    }

    @Override
    public ExtendedSignLattice evalIdentifier(Identifier id, ValueEnvironment<ExtendedSignLattice> environment, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return BaseNonRelationalValueDomain.super.evalIdentifier(id, environment, pp, oracle);
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return BaseNonRelationalValueDomain.super.satisfiesBinaryExpression(expression, left, right, pp, oracle);
    }

    @Override
    public Satisfiability satisfiesTernaryExpression(TernaryExpression expression, ExtendedSignLattice left, ExtendedSignLattice middle, ExtendedSignLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return BaseNonRelationalValueDomain.super.satisfiesTernaryExpression(expression, left, middle, right, pp, oracle);
    }

    @Override
    public ExtendedSignLattice top() {
        return null;
    }

    @Override
    public ExtendedSignLattice bottom() {
        return null;
    }
}
