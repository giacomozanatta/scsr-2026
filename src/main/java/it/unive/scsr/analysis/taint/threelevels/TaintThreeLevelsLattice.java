package it.unive.scsr.analysis.taint.threelevels;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.util.representation.StringRepresentation;
import it.unive.lisa.util.representation.StructuredRepresentation;

/*
 * Решетка Taint с тремя уровнями (Diamond Lattice):
 * TOP (возможно грязный)
 * /   \
 * CLEAN   TAINTED
 * \   /
 * BOTTOM
 */
public class TaintThreeLevelsLattice implements it.unive.lisa.lattices.informationFlow.TaintLattice<TaintThreeLevelsLattice> {

    private enum State {
        BOTTOM, CLEAN, TAINTED, TOP
    }

    private final State state;

    // Константы для быстрого доступа
    public static final TaintThreeLevelsLattice Bottom = new TaintThreeLevelsLattice(State.BOTTOM);
    public static final TaintThreeLevelsLattice Clean = new TaintThreeLevelsLattice(State.CLEAN);
    public static final TaintThreeLevelsLattice Taint = new TaintThreeLevelsLattice(State.TAINTED);
    public static final TaintThreeLevelsLattice Top = new TaintThreeLevelsLattice(State.TOP);

    private TaintThreeLevelsLattice(State state) {
        this.state = state;
    }

    @Override
    public TaintThreeLevelsLattice top() {
        return Top;
    }

    @Override
    public TaintThreeLevelsLattice bottom() {
        return Bottom;
    }

    @Override
    public TaintThreeLevelsLattice lubAux(TaintThreeLevelsLattice other) throws SemanticException {
        if (this == other || other == Bottom) return this;
        if (this == Bottom) return other;
        if (this == Top || other == Top) return Top;
        
        // Если один Clean, а другой Tainted (или наоборот) -> получаем TOP
        return Top;
    }

    @Override
    public boolean lessOrEqualAux(TaintThreeLevelsLattice other) throws SemanticException {
        if (this == other || other == Top || this == Bottom) return true;
        return false;
    }

    @Override
    public StructuredRepresentation representation() {
        if (this == Bottom) return Lattice.bottomRepresentation();
        if (this == Top) return Lattice.topRepresentation();
        return new StringRepresentation(this == Taint ? "T" : "C");
    }

    @Override
    public TaintThreeLevelsLattice tainted() {
        return Taint;
    }

    @Override
    public TaintThreeLevelsLattice clean() {
        return Clean;
    }

    @Override
    public TaintThreeLevelsLattice or(TaintThreeLevelsLattice other) throws SemanticException {
        // В информационных потоках 'or' обычно ведет себя как lub (объединение)
        return lubAux(other);
    }

    @Override
    public boolean isAlwaysTainted() {
        return this == Taint;
    }

    @Override
    public boolean isPossiblyTainted() {
        // Грязный либо точно (Taint), либо возможно (Top)
        return this == Taint || this == Top;
    }

    // Важно переопределить для корректной работы LiSA
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return state == ((TaintThreeLevelsLattice) obj).state;
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }
}