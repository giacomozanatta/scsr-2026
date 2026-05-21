package it.unive.scsr.analysis.signtaint;

import it.unive.lisa.analysis.combination.CartesianCombination;
import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

/**
 * Abstract domain that is the cartesian product of the Sign domain and the Three-Levels Taint domain.
 * Extends CartesianCombination, which handles all lattice operations, by default delegated to both lattice components
 */
public class SignTaintLattice extends CartesianCombination<SignTaintLattice, SignLattice, TaintThreeLevelsLattice> {

    /**
     * Builds a new product of two lattices.
     *
     * @param first  the first lattice
     * @param second the second lattice
     */
    public SignTaintLattice(SignLattice first, TaintThreeLevelsLattice second) {
        super(first, second);
    }

    @Override
    public SignTaintLattice mk(SignLattice first, TaintThreeLevelsLattice second) {
        return new SignTaintLattice(first, second);
    }
}
