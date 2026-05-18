package it.unive.scsr.analysis;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.combination.CartesianCombination;

public class SignXThreeTaint extends CartesianCombination
{

    public SignXThreeTaint(Lattice first, Lattice second) {
        super(first, second);
    }

    @Override
    public CartesianCombination mk(Lattice first, Lattice second) {
        return new SignXThreeTaint(first, second);
    }
}

    

