package it.unive.scsr.analysis.floatinterval;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.scsr.analysis.interval.Interval;
import it.unive.scsr.analysis.interval.IntervalLattice;

/**
 * Estensione di Interval, supportando anche i numeri reali.
 * Eseguo l'ovverride di evalConstant per supportare anche i reali, tramite MathNumber;
 * tutte le altre funzioni sono già corrette su Interval, in quanto usano MathNumber
 */
public class FloatInterval extends Interval {

    @Override
    public IntervalLattice evalConstant(Constant constant, ProgramPoint pp, SemanticOracle oracle)
            throws SemanticException {
        Object val = constant.getValue();

        if (val instanceof Integer) {
            MathNumber mn = new MathNumber((double) (Integer) val);
            return new IntervalLattice(mn, mn);
        }
        if (val instanceof Float) {
            MathNumber mn = new MathNumber((double) (Float) val);
            return new IntervalLattice(mn, mn);
        }
        if (val instanceof Double) {
            MathNumber mn = new MathNumber((Double) val);
            return new IntervalLattice(mn, mn);
        }
        if (val instanceof Long) {
            MathNumber mn = new MathNumber((double) (Long) val);
            return new IntervalLattice(mn, mn);
        }

        return IntervalLattice.TOP;
    }
}
