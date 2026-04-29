package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Lattice of  taint with three levels
 *	 Top
 * 	/	\
 * C	 T
 *  \	/
 *  BOTTOM
 *
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

    private final Integer elem;

    public static final TaintThreeLevelsLattice TOP = new TaintThreeLevelsLattice(3);
    public static final TaintThreeLevelsLattice CLEAN = new TaintThreeLevelsLattice(2);
    public static final TaintThreeLevelsLattice TAINT = new TaintThreeLevelsLattice(1);
    public static final TaintThreeLevelsLattice BOTTOM = new TaintThreeLevelsLattice(0);

    private TaintThreeLevelsLattice(int i){
        this.elem = i;
    }

    @Override
    public int hashCode() {
        return this.elem.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj==null || this.getClass()!=obj.getClass())
            return false;
        TaintThreeLevelsLattice casted = (TaintThreeLevelsLattice) obj;
        return (this.elem.equals(casted.elem));
    }

    private boolean isXAux(TaintThreeLevelsLattice TTLL){
        return (this==TTLL || this.equals(TTLL));

    }

    @Override
    public boolean isTop(){
        return this.isXAux(TaintThreeLevelsLattice.TOP);
    }

    @Override
    public boolean isBottom(){
        return this.isXAux(TaintThreeLevelsLattice.BOTTOM);
    }

    public boolean isTaint(){
        return this.isXAux(TaintThreeLevelsLattice.TAINT);
    }

    public boolean isClean(){
        return this.isXAux(TaintThreeLevelsLattice.CLEAN);
    }

    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        // TODO
        return TaintThreeLevelsLattice.TOP;
    }

    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        // TODO
        return false;
    }

    @Override
    public TaintThreeLevelsLattice top() {
        // TODO
        return TaintThreeLevelsLattice.TOP;
    }

    @Override
    public TaintThreeLevelsLattice bottom() {
        // TODO
        return TaintThreeLevelsLattice.BOTTOM;
    }

    @Override
    public StructuredRepresentation representation() {
        // TODO
        if (this.isBottom())
            return Lattice.bottomRepresentation();
        else if (this.isTop())
            return Lattice.topRepresentation();
        else return (this.isTaint()) ? new StringRepresentation("T") : new StringRepresentation("C");
    }

    @Override
    public TaintThreeLevelsLattice tainted() {
        // TODO
        return TaintThreeLevelsLattice.TAINT;
    }

    @Override
    public TaintThreeLevelsLattice clean() {
        // TODO
        return TaintThreeLevelsLattice.CLEAN;
    }

    @Override
    public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
        // TODO
        if (this.isBottom() || other.isBottom())
            return TaintThreeLevelsLattice.BOTTOM;
        else if (this.isTop() || other.isTop())
            return TaintThreeLevelsLattice.TOP;
        else if (this.isTaint() && other.isTaint())
            return TaintThreeLevelsLattice.TAINT;
        else if (this.isClean() && other.isClean())
            return TaintThreeLevelsLattice.CLEAN;
        else if ((this.isTaint() && other.isClean()) && (this.isClean() && other.isTaint()))
            return TaintThreeLevelsLattice.TOP;

        return TaintThreeLevelsLattice.TOP;
    }

    @Override
    public boolean isAlwaysTainted() {
        // TODO
        return this.isTaint();
    }

    @Override
    public boolean isPossiblyTainted() {
        // TODO
        return (this.isTaint() || this.isTop());
    }

}