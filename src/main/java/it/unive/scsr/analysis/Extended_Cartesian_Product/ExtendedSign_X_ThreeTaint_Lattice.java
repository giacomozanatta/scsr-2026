package it.unive.scsr.analysis.Extended_Cartesian_Product;

import java.util.Objects;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.Extended_Sign.Extended_SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class ExtendedSign_X_ThreeTaint_Lattice implements BaseLattice<ExtendedSign_X_ThreeTaint_Lattice>{

    private final Extended_SignLattice signLattice;
    private final TaintThreeLevelsLattice taintLattice;

    public static ExtendedSign_X_ThreeTaint_Lattice TOP = new ExtendedSign_X_ThreeTaint_Lattice(Extended_SignLattice.TOP, TaintThreeLevelsLattice.Top);
    public static ExtendedSign_X_ThreeTaint_Lattice BOTTOM = new ExtendedSign_X_ThreeTaint_Lattice(Extended_SignLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);

    public ExtendedSign_X_ThreeTaint_Lattice(Extended_SignLattice e1, TaintThreeLevelsLattice e2) 
    {
		this.signLattice = e1;
        this.taintLattice = e2;
	}
    
    @Override
    public ExtendedSign_X_ThreeTaint_Lattice top() {
        return ExtendedSign_X_ThreeTaint_Lattice.TOP;
    }

    @Override
    public ExtendedSign_X_ThreeTaint_Lattice bottom() {
        return ExtendedSign_X_ThreeTaint_Lattice.BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() 
    {
        return new StringRepresentation("[" + signLattice.representation() + "," + taintLattice.representation() + "]");
    }

    @Override
    public ExtendedSign_X_ThreeTaint_Lattice lubAux(ExtendedSign_X_ThreeTaint_Lattice other) throws SemanticException {
        
        return new ExtendedSign_X_ThreeTaint_Lattice(signLattice.lub(other.signLattice), taintLattice.lub(other.taintLattice));
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSign_X_ThreeTaint_Lattice other) throws SemanticException {
        return signLattice.lessOrEqual(other.signLattice) && taintLattice.lessOrEqual(other.taintLattice);
    }

    public Satisfiability eq(ExtendedSign_X_ThreeTaint_Lattice other) 
	{
		if (this.isBottom() || other.isBottom())
        return Satisfiability.BOTTOM;

        else if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;

        return signLattice.eq(other.getSign());
	}

    public Satisfiability gt(ExtendedSign_X_ThreeTaint_Lattice other)
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
        
		ExtendedSign_X_ThreeTaint_Lattice other = (ExtendedSign_X_ThreeTaint_Lattice) obj;
		return (signLattice.equals(other.signLattice)) && (taintLattice.equals(other.taintLattice));
	}

    public Extended_SignLattice getSign()
    {
        return this.signLattice;
    }

    public TaintThreeLevelsLattice getTaint()
    {
        return this.taintLattice;
    }
}
