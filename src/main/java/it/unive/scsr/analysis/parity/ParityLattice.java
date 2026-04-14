package it.unive.scsr.analysis.parity;

import com.ibm.icu.impl.number.parse.ParsingUtils;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.sign.SignLattice;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    private int element;

    public static ParityLattice TOP =  new ParityLattice(0);
    public static ParityLattice BOTTOM = new ParityLattice(1);
    public static ParityLattice EVEN = new ParityLattice(2);
    public static ParityLattice ODD = new ParityLattice(3);

    public ParityLattice(int element) {
        this.element = element;
    }

	@Override
	public ParityLattice top() { return ParityLattice.TOP; }

    @Override
    public ParityLattice bottom() { return ParityLattice.BOTTOM; }

	@Override
	public StructuredRepresentation representation() {
		if(this == ParityLattice.BOTTOM)
            return Lattice.bottomRepresentation();
        else if(this == ParityLattice.TOP)
            return Lattice.topRepresentation();
        else if(this == ParityLattice.EVEN)
            return new StringRepresentation("EVEN");
        else return new StringRepresentation("ODD");
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		return ParityLattice.TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		return this.equals(other);
	}

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParityLattice other = (ParityLattice) obj;
        return element == other.element;
    }

    public Satisfiability eq(ParityLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        else if (this.isTop() || other.isTop())
            return Satisfiability.UNKNOWN;
        else if (!this.equals(other))
            return Satisfiability.NOT_SATISFIED;
        else
            return Satisfiability.UNKNOWN;
    }

    public Satisfiability gt(ParityLattice other) {
        if (this.isBottom() || other.isBottom())
            return Satisfiability.BOTTOM;
        return Satisfiability.UNKNOWN;
    }
}