package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.CPropSetElem;

import java.util.Objects;

//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    public static final ParityLattice TOP = new ParityLattice(0);
    public static final ParityLattice BOTTOM = new ParityLattice(1);
    public static final ParityLattice EVEN = new ParityLattice(2);
    public static final ParityLattice ODD = new ParityLattice(3);

    private final int id;
    public ParityLattice(int id) {
        this.id = id;
    }

	@Override
	public ParityLattice top() {
		// TODO: homework
		return ParityLattice.TOP;
	}

	@Override
	public ParityLattice bottom() {
		// TODO: homework
		return ParityLattice.BOTTOM;
	}

	@Override
	public StructuredRepresentation representation() {
		// TODO: homework
		if(this == TOP) {
            return Lattice.topRepresentation();
        }
        if(this == BOTTOM) {
            return Lattice.bottomRepresentation();
        }
        if(this == EVEN) {
            return new StringRepresentation("EVEN");
        }
        if(this == ODD) {
            return new StringRepresentation("ODD");
        }
        return null;
	}

	@Override
	public ParityLattice lubAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {
		// TODO: homework
		return false;
	}

	// TODO add missing components
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(
            Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParityLattice other = (ParityLattice) obj;
        return id == other.id;
    }
}