package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.interval.FloatInterval;
import it.unive.scsr.checkers.SquareRootChecker;
import org.junit.Test;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import static it.unive.lisa.DefaultConfiguration.*;
import it.unive.lisa.analysis.numeric.Interval;

public class DivZeroTest {

    @Test
    public void testDivZeroAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/myprograms/894069_div_by_zero.imp");

       // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/MYDIVZEROANALYSISSTATICALLLLLLL";

        // we specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));

        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }
}
