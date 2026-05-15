package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;

public class SignTaint implements BaseNonRelationalValueDomain<SignTaintLattice> {

    @Override
    public SignTaintLattice top() {
        // TODO
        return null;
    }

    @Override
    public SignTaintLattice bottom() {
        // TODO
        return null;
    }

    @Override
    public SignTaintLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        // TODO
        return null;
    }

    @Override
    public SignTaintLattice evalUnaryExpression(UnaryExpression expression, SignTaintLattice arg,
            ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        // TODO
        return null;
    }

    @Override
    public SignTaintLattice evalBinaryExpression(BinaryExpression expression, SignTaintLattice left,
            SignTaintLattice right, ProgramPoint pp, SemanticOracle oracle) throws SemanticException {
        // TODO
        return null;
    }

}
