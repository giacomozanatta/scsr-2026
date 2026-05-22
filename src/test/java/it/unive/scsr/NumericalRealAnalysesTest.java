package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.scsr.analysis.interval_real.IntervalReal;
import it.unive.scsr.checkers.DivByZeroIntervalRealChecker;
import it.unive.scsr.checkers.OverflowIntervalRealChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

/**
 * NumericalRealAnalysesTest
 *
 * @brief Test suite for semantic checkers over the {@link IntervalReal} abstract domain,
 *  applied to {@code inputs/realintervals.imp}.
 *
 * @note We test
 *  - {@link DivByZeroIntervalRealChecker} which detects division-by-zero at division nodes.
 *  - {@link OverflowIntervalRealChecker} which detects overflow/underflow for 32 and 64-bit values.
 *
 * @author Gianmaria Pizzo 872966
 */
public class NumericalRealAnalysesTest {

    /**
     * Runs {@link DivByZeroIntervalRealChecker} over {@code interval_real.imp}.
     *
     * @note We expect 1 definite warning at line 45 ({@code def div_1 = pos_a / zero}),
     *  where {@code zero} is the constant {@code 0.0} — abstract value {@code [0.0, 0.0]}.
     */
    @Test
    public void testDivByZeroIntervalRealAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/interval_real/interval_real.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/interval_real/div-by-zero";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalReal(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DivByZeroIntervalRealChecker<>());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    /**
     * Runs {@link OverflowIntervalRealChecker} with Float32 bounds over {@code realintervals.imp}.
     *
     * @note The range is {@code [-Float.MAX_VALUE, Float.MAX_VALUE]}, which is approximately {@code [-3.4e38, 3.4e38]}.
     * @note We expect 12 warnings (expressions whose abstract value is ±∞ after loop widening)
     * 
     */
    @Test
    public void testOverflowIntervalReal32bitsAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/interval_real/interval_real.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/interval_real/overflow_float32";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalReal(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalRealChecker<>(-Float.MAX_VALUE, Float.MAX_VALUE));

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    /**
     * Runs {@link OverflowIntervalRealChecker} with Float64 bounds over {@code realintervals.imp}.
     *
     * @note The range is {@code [-Double.MAX_VALUE, Double.MAX_VALUE]}, which is approximately {@code [-1.8e308, 1.8e308]}.
     * @note We expect 12 warnings (same set as the Float32 test, because all overflowing abstract).
     */
    @Test
    public void testOverflowIntervalReal64bitsAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/interval_real/interval_real.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/interval_real/overflow_float64";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalReal(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalRealChecker<>(-Double.MAX_VALUE, Double.MAX_VALUE));

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}
