package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.sign.Sign;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;

public class Cartesian
        implements BaseNonRelationalValueDomain<CartesianLattice> {
    @Override
    public CartesianLattice top() {
        return CartesianLattice.TOP;
    }

    @Override
    public CartesianLattice bottom() {
        return CartesianLattice.BOTTOM;
    }

    @Override
    public CartesianLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new CartesianLattice((new Sign()).evalConstant(constant, pp, oracle), (new TaintThreeLevels().evalConstant(constant, pp, oracle)));
    }

    @Override
    public CartesianLattice evalUnaryExpression(UnaryExpression expression, CartesianLattice arg, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new CartesianLattice((new Sign()).evalUnaryExpression(expression, arg.signLattice(), pp, oracle), (new TaintThreeLevels()).evalUnaryExpression(expression, arg.taintThreeLevelsLattice(), pp, oracle));
    }

    @Override
    public CartesianLattice evalBinaryExpression(BinaryExpression expression, CartesianLattice left, CartesianLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        return new CartesianLattice((new Sign()).evalBinaryExpression(expression, left.signLattice(), right.signLattice(), pp, oracle), (new TaintThreeLevels()).evalBinaryExpression(expression, left.taintThreeLevelsLattice(), right.taintThreeLevelsLattice(), pp, oracle));
    }
}
