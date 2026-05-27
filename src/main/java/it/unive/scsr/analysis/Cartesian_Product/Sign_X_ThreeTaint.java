package it.unive.scsr.analysis.Cartesian_Product;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.scsr.analysis.sign.Sign;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class Sign_X_ThreeTaint implements BaseNonRelationalValueDomain<Sign_X_ThreeTaint_Lattice>
{
    private final Sign s = new Sign();

    @Override
    public Sign_X_ThreeTaint_Lattice top() {
        return Sign_X_ThreeTaint_Lattice.TOP;
    }

    @Override
    public Sign_X_ThreeTaint_Lattice bottom() {
        return Sign_X_ThreeTaint_Lattice.BOTTOM;
    }

    @Override
	public Sign_X_ThreeTaint_Lattice
	evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
			throws SemanticException {
		
        SignLattice sign = s.evalConstant(constant, pp, oracle);
			
		return new Sign_X_ThreeTaint_Lattice(sign, TaintThreeLevelsLattice.Clean);
	}

    @Override
	public Sign_X_ThreeTaint_Lattice evalUnaryExpression(UnaryExpression expression, Sign_X_ThreeTaint_Lattice arg, ProgramPoint pp,
			SemanticOracle oracle) throws SemanticException 
    {
	SignLattice signResult = s.evalUnaryExpression(expression, arg.getSign(), pp, oracle);
    
    TaintThreeLevelsLattice taintResult = arg.getTaint();
    
    return new Sign_X_ThreeTaint_Lattice(signResult, taintResult);
	}

    @Override
	public Sign_X_ThreeTaint_Lattice evalBinaryExpression(BinaryExpression expression, Sign_X_ThreeTaint_Lattice left, Sign_X_ThreeTaint_Lattice right,
			ProgramPoint pp, SemanticOracle oracle) throws SemanticException 
    {

        SignLattice sign = s.evalBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);

        TaintThreeLevelsLattice taint = left.getTaint().lub(right.getTaint());

		return new Sign_X_ThreeTaint_Lattice(sign, taint);
	}

    @Override
	public Satisfiability satisfiesBinaryExpression(
			BinaryExpression expression,
			Sign_X_ThreeTaint_Lattice left,
			Sign_X_ThreeTaint_Lattice right,
			ProgramPoint pp,
			SemanticOracle oracle) 
    {
        return s.satisfiesBinaryExpression(expression, left.getSign(), right.getSign(), pp, oracle);
	}

    @Override
	public ValueEnvironment<Sign_X_ThreeTaint_Lattice> assumeBinaryExpression(
			ValueEnvironment<Sign_X_ThreeTaint_Lattice> environment,
			BinaryExpression expression,
			ProgramPoint src,
			ProgramPoint dest,
			SemanticOracle oracle)
			throws SemanticException 
    {
        return environment;
	}
}

    

