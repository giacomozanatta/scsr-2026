package it.unive.scsr.analysis.parity;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import java.util.Objects;

/**
 *          T
 *      /      \
 *    EVEN     ODD
 *     \        /
 *       BOTTOM
 */
//Homework
public class ParityLattice implements BaseLattice<ParityLattice> {

    private final int element;

    public static ParityLattice TOP = new ParityLattice(0);

    public static ParityLattice EVEN = new ParityLattice(1);
    public static ParityLattice ODD = new ParityLattice(2);

    public static ParityLattice BOTTOM = new ParityLattice(3);

    public ParityLattice(int e){
        this.element = e;
    }

    @Override
    public ParityLattice top(){
        return ParityLattice.TOP;
    }

    @Override
    public ParityLattice bottom() {
        return ParityLattice.BOTTOM;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ParityLattice objCasted = (ParityLattice) obj;
        return (this.element == objCasted.element);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.element);
    }

    @Override
    public StructuredRepresentation representation() {
        if(this == ParityLattice.BOTTOM)
            return Lattice.bottomRepresentation();
        else if (this == ParityLattice.TOP)
            return Lattice.topRepresentation();
        else if (this == ParityLattice.EVEN)
            return new StringRepresentation("EVEN");

        return new StringRepresentation("ODD");
    }

    @Override
    public ParityLattice lubAux(ParityLattice other) throws SemanticException {
        return ParityLattice.TOP;
    }

    @Override
    public boolean lessOrEqualAux(ParityLattice other) throws SemanticException {

        // // Controls already done by lessOrEqual
        /*if (this.equals(other) || this.equals(ParityLattice.BOTTOM))
            return true;
        else if (this.equals(ParityLattice.TOP))
            return false;
        */
        return false;
    }

    //TODO add missing components
}
