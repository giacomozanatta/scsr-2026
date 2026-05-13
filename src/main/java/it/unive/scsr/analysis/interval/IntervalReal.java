package it.unive.scsr.analysis.interval;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.util.numeric.MathNumber;
/*

TASK REQUEST: To extend interval domain in LiSA to support real numbers (double/float)


since this is an extended class i don't need to implement the other methods.
evalUnaryExpression,evalBinaryExpression ecc..., they use MathNumber to handle calculations
so they can handle both integers and real numbers and i don't need to override them.
the only method i need to override is evalConstant to handle float and double cases
*/

/*
in the pdf is mentioned to "re-think the widening operator"
but since in the lattice we are using MathNumber we can use the same widening operator as before
since MathNumber  can represent integers and real numbers and supports comparisons natively,
 so the same widening logic can be applied.

NOTE: MathNumber has float32 precision so 7.9 -> 7.900000095367432

*/

public class IntervalReal extends Interval{

    @Override

    public IntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        //i can use MathNumber to represent real numbers
        if (constant.getValue() instanceof Double) {
            Double n = (Double) constant.getValue();
            MathNumber m = new MathNumber(n);
            return new IntervalLattice(m, m);
        }
        if (constant.getValue() instanceof Float){
            Float n = (Float) constant.getValue();
            MathNumber m = new MathNumber(n);
            return new IntervalLattice(m,m);
        }
        //i use the super method to handle integers and TOP cases
        return super.evalConstant(constant,pp,oracle);
    }
}
