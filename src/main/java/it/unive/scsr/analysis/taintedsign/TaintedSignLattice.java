package it.unive.scsr.analysis.taintedsign;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.combination.CartesianCombination;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class TaintedSignLattice
    extends CartesianCombination<TaintedSignLattice, ExtendedSignLattice, TaintThreeLevelsLattice>
//    implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintedSignLattice>
{
    public TaintedSignLattice() {
        super(
            ExtendedSignLattice.TOP,
            TaintThreeLevelsLattice.Top
        );
    }

    public TaintedSignLattice(ExtendedSignLattice extendedSignLattice, TaintThreeLevelsLattice taintThreeLevelsLattice) {
        super(extendedSignLattice, taintThreeLevelsLattice);
    }

    @Override
    public TaintedSignLattice mk(ExtendedSignLattice extendedSignLattice, TaintThreeLevelsLattice taintThreeLevelsLattice) {
        return new TaintedSignLattice(extendedSignLattice, taintThreeLevelsLattice);
    }

    @Override
    public StructuredRepresentation representation() {
        return new StringRepresentation(
            "("
            + first.representation().toString()
            + ", "
            + second.representation().toString()
            + ")"
        );
    }

//    @Override
//    public TaintedSignLattice tainted() {
//        return mk(this.first, TaintThreeLevelsLattice.Taint);
//    }
//
//    @Override
//    public TaintedSignLattice clean() {
//        return mk(this.first, TaintThreeLevelsLattice.Clean);
//    }
//
//    @Override
//    public TaintedSignLattice or(TaintedSignLattice other) throws SemanticException {
//        return mk(this.first.lub(other.first), this.second.or(other.second));
//    }
//
//    @Override
//    public boolean isAlwaysTainted() {
//        return this.second.isAlwaysTainted();
//    }
//
//    @Override
//    public boolean isPossiblyTainted() {
//        return this.second.isPossiblyTainted();
//    }
}
