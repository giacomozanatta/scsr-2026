package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.informationFlow.BaseTaint;

public class TaintThreeLevelsSolution extends BaseTaint<TaintThreeLevelsLatticeSolution> {

@Override
public TaintThreeLevelsLatticeSolution top() {
return TaintThreeLevelsLatticeSolution.TOP;
}

@Override
public TaintThreeLevelsLatticeSolution bottom() {
return TaintThreeLevelsLatticeSolution.BOTTOM;
}

@Override
protected TaintThreeLevelsLatticeSolution tainted() {
return TaintThreeLevelsLatticeSolution.TAINTED;
}

@Override
protected TaintThreeLevelsLatticeSolution clean() {
return TaintThreeLevelsLatticeSolution.CLEAN;
}

}