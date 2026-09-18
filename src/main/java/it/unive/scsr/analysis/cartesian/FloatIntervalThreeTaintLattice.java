package it.unive.scsr.analysis.cartesian;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;
import it.unive.scsr.analysis.floatInterval.FloatIntervalLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.Objects;

public class FloatIntervalThreeTaintLattice implements BaseLattice<FloatIntervalThreeTaintLattice>,
        it.unive.lisa.lattices.informationFlow.TaintLattice<FloatIntervalThreeTaintLattice> {

    @Override
    public FloatIntervalThreeTaintLattice tainted() {
        return new FloatIntervalThreeTaintLattice(this.interval, this.taint.tainted());
    }

    @Override
    public FloatIntervalThreeTaintLattice clean() {
        return new FloatIntervalThreeTaintLattice(this.interval, this.taint.clean());
    }

    @Override
    public FloatIntervalThreeTaintLattice or(FloatIntervalThreeTaintLattice other) throws SemanticException {
        return new FloatIntervalThreeTaintLattice(this.interval.lub(other.interval), this.taint.or(other.taint));
    }

    @Override
    public boolean isAlwaysTainted() {
        return this.taint.isAlwaysTainted();
    }

    @Override
    public boolean isPossiblyTainted() {
        return this.taint.isPossiblyTainted();
    }

    private final FloatIntervalLattice interval;
    private final TaintThreeLevelsLattice taint;

    public static FloatIntervalThreeTaintLattice TOP =
            new FloatIntervalThreeTaintLattice(FloatIntervalLattice.TOP, TaintThreeLevelsLattice.Top);
    public static FloatIntervalThreeTaintLattice BOTTOM =
            new FloatIntervalThreeTaintLattice(FloatIntervalLattice.BOTTOM, TaintThreeLevelsLattice.Bottom);

    public FloatIntervalThreeTaintLattice(FloatIntervalLattice interval, TaintThreeLevelsLattice taint) {
        this.interval = interval;
        this.taint = taint;
    }

    public FloatIntervalLattice getInterval() { return interval; }
    public TaintThreeLevelsLattice getTaint() { return taint; }

    @Override public FloatIntervalThreeTaintLattice top() { return TOP; }
    @Override public FloatIntervalThreeTaintLattice bottom() { return BOTTOM; }

    @Override
    public FloatIntervalThreeTaintLattice lubAux(FloatIntervalThreeTaintLattice other) throws SemanticException {
        return new FloatIntervalThreeTaintLattice(this.interval.lub(other.interval), this.taint.lub(other.taint));
    }

    @Override
    public FloatIntervalThreeTaintLattice glbAux(FloatIntervalThreeTaintLattice other) throws SemanticException {
        return new FloatIntervalThreeTaintLattice(this.interval.glb(other.interval), this.taint.glb(other.taint));
    }

    @Override
    public boolean lessOrEqualAux(FloatIntervalThreeTaintLattice other) throws SemanticException {
        return this.interval.lessOrEqual(other.interval) && this.taint.lessOrEqual(other.taint);
    }

    @Override
    public FloatIntervalThreeTaintLattice wideningAux(FloatIntervalThreeTaintLattice other) throws SemanticException {
        return new FloatIntervalThreeTaintLattice(
                this.interval.widening(other.interval),
                this.taint.widening(other.taint)
        );
    }

    @Override
    public StructuredRepresentation representation() {
        if (isBottom()) return Lattice.bottomRepresentation();
        if (isTop()) return Lattice.topRepresentation();
        return new StringRepresentation("[" + interval.representation() + "," + taint.representation() + "]");
    }

    @Override
    public int hashCode() { return Objects.hash(interval, taint); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        FloatIntervalThreeTaintLattice other = (FloatIntervalThreeTaintLattice) obj;
        return this.interval.equals(other.interval) && this.taint.equals(other.taint);
    }
}