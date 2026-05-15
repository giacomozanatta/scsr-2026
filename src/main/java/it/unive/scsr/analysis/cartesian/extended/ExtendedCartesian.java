package it.unive.scsr.analysis.cartesian.extended;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.extendedsign.ExtendedSign;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;


public class ExtendedCartesian
        implements BaseNonRelationalValueDomain<ExtendedCartesianLattice> {
    @Override
    public ExtendedCartesianLattice top() {
        return ExtendedCartesianLattice.TOP;
    }

    @Override
    public ExtendedCartesianLattice bottom() {
        return ExtendedCartesianLattice.BOTTOM;
    }

    @Override
    public ExtendedCartesianLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new ExtendedCartesianLattice((new ExtendedSign()).evalConstant(constant, pp, oracle), (new TaintThreeLevels().evalConstant(constant, pp, oracle)));
    }

    @Override
    public ExtendedCartesianLattice evalUnaryExpression(UnaryExpression expression, ExtendedCartesianLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new ExtendedCartesianLattice((new ExtendedSign()).evalUnaryExpression(expression, arg.signLattice(), pp, oracle), (new TaintThreeLevels()).evalUnaryExpression(expression, arg.taintThreeLevelsLattice(), pp, oracle));
    }

    @Override
    public ExtendedCartesianLattice evalBinaryExpression(BinaryExpression expression, ExtendedCartesianLattice left, ExtendedCartesianLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new ExtendedCartesianLattice((new ExtendedSign()).evalBinaryExpression(expression, left.signLattice(), right.signLattice(), pp, oracle), (new TaintThreeLevels()).evalBinaryExpression(expression, left.taintThreeLevelsLattice(), right.taintThreeLevelsLattice(), pp, oracle));
    }
}
