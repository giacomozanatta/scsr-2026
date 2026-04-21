package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.informationFlow.BaseTaint;

public class TaintThreeLevels extends BaseTaint<TaintThreeLevelsLattice> {
    @Override
    public TaintThreeLevelsLattice top() { return TaintThreeLevelsLattice.Top; }
    @Override
    public TaintThreeLevelsLattice bottom() { return TaintThreeLevelsLattice.Bottom; }
    @Override
    protected TaintThreeLevelsLattice tainted() { return TaintThreeLevelsLattice.Taint; }
    @Override
    protected TaintThreeLevelsLattice clean() { return TaintThreeLevelsLattice.Clean; }
}