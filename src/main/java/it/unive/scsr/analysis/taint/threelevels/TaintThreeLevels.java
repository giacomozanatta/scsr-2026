package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.informationFlow.BaseTaint;

// This class connects our custom 3-level lattice to the main analyzer
public class TaintThreeLevels extends BaseTaint<TaintThreeLevelsLattice> {
    
    // Returns the highest state (Unknown / Maybe Tainted)
    @Override
    public TaintThreeLevelsLattice top() { return TaintThreeLevelsLattice.Top; }
    
    // Returns the lowest state (Unreachable code)
    @Override
    public TaintThreeLevelsLattice bottom() { return TaintThreeLevelsLattice.Bottom; }
    
    // Returns the completely dirty state
    @Override
    protected TaintThreeLevelsLattice tainted() { return TaintThreeLevelsLattice.Taint; }
    
    // Returns the completely safe state
    @Override
    protected TaintThreeLevelsLattice clean() { return TaintThreeLevelsLattice.Clean; }
}