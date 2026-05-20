package it.unive.scsr.analysis.extendedsignXthreetaint;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.combination.LatticeProduct;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

// Notice the explicit "implements Lattice<ExtendedSignXThreeTaintLattice>"
// to satisfy LiSA's type boundaries.
public class ExtendedSignXThreeTaintLattice {}
        /*
        extends LatticeProduct<ExtendedSignLattice, TaintThreeLevelsLattice>
        implements Lattice<ExtendedSignXThreeTaintLattice> {

    public ExtendedSignXThreeTaintLattice(ExtendedSignLattice first, TaintThreeLevelsLattice second) {
        super(first, second);
    }

    @Override
    public boolean isBottom() {
        // If either domain hits bottom, the entire pair represents an unreachable state
        return first.isBottom() || second.isBottom();
    }

    @Override
    public boolean isTop() {
        return first.isTop() && second.isTop();
    }

    @Override
    public ExtendedSignXThreeTaint unknownValue(Identifier id) {
        return BaseNonRelationalValueDomain.super.unknownValue(id);
    }
    @Override
    public ExtendedSignXThreeTaintLattice lubAux(ExtendedSignXThreeTaintLattice other) throws SemanticException {
        return new ExtendedSignXThreeTaintLattice(first.lub(other.first), second.lub(other.second));
    }

    @Override
    public ExtendedSignXThreeTaintLattice glbAux(ExtendedSignXThreeTaintLattice other) throws SemanticException {
        return new ExtendedSignXThreeTaintLattice(first.glb(other.first), second.glb(other.second));
    }

    @Override
    public ExtendedSignXThreeTaintLattice wideningAux(ExtendedSignXThreeTaintLattice other) throws SemanticException {
        return new ExtendedSignXThreeTaintLattice(first.widening(other.first), second.widening(other.second));
    }

    @Override
    public boolean lessOrEqualAux(ExtendedSignXThreeTaintLattice other) throws SemanticException {
        return first.lessOrEqual(other.first) && second.lessOrEqual(other.second);
    }

    @Override
    public ExtendedSignXThreeTaintLattice top() {
        return new ExtendedSignXThreeTaintLattice(first.top(), second.top());
    }

    @Override
    public ExtendedSignXThreeTaintLattice bottom() {
        return new ExtendedSignXThreeTaintLattice(first.bottom(), second.bottom());
    }
}

 */