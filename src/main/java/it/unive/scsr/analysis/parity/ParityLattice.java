package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    private int currentState;

    public static final ParityLattice TOP = new ParityLattice(0);
    public static final ParityLattice ODD = new ParityLattice(1);
    public static final ParityLattice EVEN = new ParityLattice(2);
    public static final ParityLattice BOTTOM = new ParityLattice(3);

    public ParityLattice(int currentState) {
        this.currentState = currentState;
    }

    @Override
	public ParityLattice top() {
		return TOP;
	}

	@Override
	public ParityLattice bottom() {
		return BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		if (this.currentState == 0) {
            return Lattice.topRepresentation();
        } else if (this.currentState == 1) {
            return new StringRepresentation("ODD");
        } else if (this.currentState == 2) {
            return new StringRepresentation("EVEN");
        }
        //else if (this.currentState == 3)
        return Lattice.bottomRepresentation();
    }

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		if(other.currentState == this.currentState) return this;
        else return top();
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
        return other.currentState == this.currentState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentState);
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
        return currentState == other.currentState;
    }

}