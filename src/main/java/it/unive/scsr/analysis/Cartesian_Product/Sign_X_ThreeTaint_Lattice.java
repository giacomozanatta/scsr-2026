package it.unive.scsr.analysis.Cartesian_Product;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class Sign_X_ThreeTaint_Lattice implements BaseLattice<Sign_X_ThreeTaint_Lattice>{

    private final SignLattice signLattice;
    private final TaintThreeLevelsLattice taintLattice;

    public static Sign_X_ThreeTaint_Lattice TOP = new Sign_X_ThreeTaint_Lattice(SignLattice.TOP, TaintThreeLevelsLattice.Top);
    public static Sign_X_ThreeTaint_Lattice BOTTOM = new Sign_X_ThreeTaint_Lattice(SignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);

    public Sign_X_ThreeTaint_Lattice(SignLattice e1, TaintThreeLevelsLattice e2) 
    {
		this.signLattice = e1;
        this.taintLattice = e2;
	}
    
    @Override
    public Sign_X_ThreeTaint_Lattice top() {
        return Sign_X_ThreeTaint_Lattice.TOP;
    }

    @Override
    public Sign_X_ThreeTaint_Lattice bottom() {
        return Sign_X_ThreeTaint_Lattice.BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() 
    {
        return new StringRepresentation("[" + signLattice.representation() + "," + taintLattice.representation() + "]");
    }

    @Override
    public Sign_X_ThreeTaint_Lattice lubAux(Sign_X_ThreeTaint_Lattice other) throws SemanticException {
        
        return new Sign_X_ThreeTaint_Lattice(signLattice.lub(other.signLattice), taintLattice.lub(other.taintLattice));
    }

    @Override
    public boolean lessOrEqualAux(Sign_X_ThreeTaint_Lattice other) throws SemanticException {
        return signLattice.lessOrEqual(other.signLattice) && taintLattice.lessOrEqual(other.taintLattice);
    }

    public Satisfiability eq(Sign_X_ThreeTaint_Lattice other) 
	{
		if (this.isBottom() || other.isBottom())
        return Satisfiability.BOTTOM;

        else if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;

        return signLattice.eq(other.getSign());
	}

    public Satisfiability gt(Sign_X_ThreeTaint_Lattice other)
    {
        if (this.isBottom() || other.isBottom())
        return Satisfiability.BOTTOM;

        else if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;

        return signLattice.gt(other.getSign());
    }
    
    @Override
	public int hashCode() {
		return Objects.hash(signLattice,taintLattice);
	}

	@Override
	public boolean equals(Object obj) 
    {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
        
		Sign_X_ThreeTaint_Lattice other = (Sign_X_ThreeTaint_Lattice) obj;
		return (signLattice.equals(other.signLattice)) && (taintLattice.equals(other.taintLattice));
	}

    public SignLattice getSign()
    {
        return this.signLattice;
    }

    public TaintThreeLevelsLattice getTaint()
    {
        return this.taintLattice;
    }
}

