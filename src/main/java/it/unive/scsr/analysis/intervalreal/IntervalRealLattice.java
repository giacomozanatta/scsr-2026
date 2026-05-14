package it.unive.scsr.analysis.intervalreal;

import java.util.Objects;
import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;


/**
 * IntervalRealLattice Class
 * 
 * An interval lattice for real numbers, where each element is an interval [l, u] with l and u being MathNumbers.
 * The lattice supports the standard operations of lub, glb, widening, and comparison.
 * The top element is represented by the interval [-∞, +∞], and the bottom element is represented by an interval with null bounds.  
 * 
 * @author Gianmaria Pizzo 872966
 */
public class IntervalRealLattice implements BaseLattice<IntervalRealLattice>, Comparable<IntervalRealLattice> {

}