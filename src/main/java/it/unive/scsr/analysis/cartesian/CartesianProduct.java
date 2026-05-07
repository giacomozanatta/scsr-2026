package it.unive.scsr.analysis.cartesian;

import it.unive.scsr.analysis.sign.SignLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public record CartesianProduct(SignLattice signLattice, TaintThreeLevelsLattice taintThreeLevelsLattice) {
}
