package it.unive.scsr.analysis.signtaint.extsigntaint;

import it.unive.lisa.analysis.combination.CartesianCombination;
import it.unive.scsr.analysis.sign.extsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

/**
 * Abstract domain that is the cartesian product of the Extended Sign domain and the Three-Levels Taint domain.
 * Extends CartesianCombination, which handles all lattice operations and creates two attributes for the two domains.
 *
 * By default, CartesianCombination delegates lattice operations to both lattice components.
 * Example : lubAux, inherited from CartesianCombination, will call lubAux
 * on the first component (ExtendedSignLattice) and on the second (TaintThreeLevelsLattice).
 */
public class ExtendedSignTaintLattice extends CartesianCombination<ExtendedSignTaintLattice, ExtendedSignLattice, TaintThreeLevelsLattice> {

    /**
     * Builds a new product of two lattices.
     *
     * @param first  the first lattice
     * @param second the second lattice
     */
    public ExtendedSignTaintLattice(ExtendedSignLattice first, TaintThreeLevelsLattice second) {
        super(first, second);
    }

    @Override
    public ExtendedSignTaintLattice mk(ExtendedSignLattice first, TaintThreeLevelsLattice second) {
        return new ExtendedSignTaintLattice(first, second);
    }
}
