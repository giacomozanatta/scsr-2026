package it.unive.scsr.analysis.sign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.combination.LatticeProduct;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
//TASK REQUEST: to implement two domains in LiSA :Sign x ThreeTaint (Cartesian Product)

//in this class we dont have to apply a reduction because the two domains are independent
//so i decided to use LatticeProduct to combine them

public class SignXTaint implements BaseNonRelationalValueDomain<LatticeProduct<SignLattice, TaintThreeLevelsLattice>> {

        private final Sign sign = new Sign();
        private final TaintThreeLevels taint = new TaintThreeLevels();

        @Override
        public LatticeProduct<SignLattice, TaintThreeLevelsLattice> top() {
                return new LatticeProduct<>(sign.top(), taint.top());
        }

        @Override
        public LatticeProduct<SignLattice, TaintThreeLevelsLattice> bottom() {
                return new LatticeProduct<>(sign.bottom(), taint.bottom());
        }

        @Override
        public LatticeProduct<SignLattice, TaintThreeLevelsLattice> fixedVariable(
                        Identifier id,
                        ProgramPoint pp,
                        SemanticOracle oracle) throws SemanticException {
                Annotations annots = id.getAnnotations();
                if (annots.contains(BaseTaint.TAINTED_MATCHER))
                        return new LatticeProduct<>(sign.top(), TaintThreeLevelsLattice.TAINT);
                if (annots.contains(BaseTaint.CLEAN_MATCHER))
                        return new LatticeProduct<>(sign.top(), TaintThreeLevelsLattice.CLEAN);
                return bottom();
        }

        @Override
        public LatticeProduct<SignLattice, TaintThreeLevelsLattice> evalIdentifier(
                        Identifier id,
                        ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>> environment,
                        ProgramPoint pp,
                        SemanticOracle oracle) throws SemanticException {
                LatticeProduct<SignLattice, TaintThreeLevelsLattice> def = fixedVariable(id, pp, oracle);
                if (!def.isBottom())
                        return def;
                return BaseNonRelationalValueDomain.super.evalIdentifier(id, environment, pp, oracle);
        }

        @Override
        public LatticeProduct<SignLattice, TaintThreeLevelsLattice> evalConstant(Constant constant, ProgramPoint pp,
                        SemanticOracle oracle) throws SemanticException {
                return new LatticeProduct<>(
                                sign.evalConstant(constant, pp, oracle),
                                taint.evalConstant(constant, pp, oracle));
        }

        @Override
        public LatticeProduct<SignLattice, TaintThreeLevelsLattice> evalUnaryExpression(
                        UnaryExpression expression,
                        LatticeProduct<SignLattice, TaintThreeLevelsLattice> arg,
                        ProgramPoint pp,
                        SemanticOracle oracle) throws SemanticException {
                return new LatticeProduct<>(
                                sign.evalUnaryExpression(expression, arg.first, pp, oracle),
                                taint.evalUnaryExpression(expression, arg.second, pp, oracle));
        }

        @Override
        public LatticeProduct<SignLattice, TaintThreeLevelsLattice> evalBinaryExpression(
                        BinaryExpression expression,
                        LatticeProduct<SignLattice, TaintThreeLevelsLattice> left,
                        LatticeProduct<SignLattice, TaintThreeLevelsLattice> right,
                        ProgramPoint pp,
                        SemanticOracle oracle) throws SemanticException {
                return new LatticeProduct<>(
                                sign.evalBinaryExpression(expression, left.first, right.first, pp, oracle),
                                taint.evalBinaryExpression(expression, left.second, right.second, pp, oracle));
        }

       
        // taint is allways unknown so it doesnt affect the satisfiability

        public Satisfiability satisfiesBinaryExpression(
                        BinaryExpression expression,
                        LatticeProduct<SignLattice, TaintThreeLevelsLattice> left,
                        LatticeProduct<SignLattice, TaintThreeLevelsLattice> right,
                        ProgramPoint pp,
                        SemanticOracle oracle) {
                return sign.satisfiesBinaryExpression(expression, left.first, right.first, pp, oracle);
        }

        // taint is allways unknown so it doesnt affect the assume
        public ValueEnvironment<SignLattice> assumeBinaryExpression(
                        ValueEnvironment<SignLattice> environment,
                        BinaryExpression expression,
                        ProgramPoint src,
                        ProgramPoint dest,
                        SemanticOracle oracle)
                        throws SemanticException {
                return sign.assumeBinaryExpression(environment, expression, src, dest, oracle);
        }

}
