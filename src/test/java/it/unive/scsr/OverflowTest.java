package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class OverflowTest {
    @Test
    public void testOverflow() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/overflow/overflow.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        conf.workdir = "outputs/" + "overflow";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(
                defaultHeapDomain(),
                new Interval(),
                defaultTypeDomain()
        );
        // MIN_VALUE = -2147483648, MAX_VALUE = 2147483647
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);

        System.out.println("Analysis completed for " + "inputs/overflow/overflow.imp");
        System.out.println("Check outputs in: outputs/" + "overflow");
    }
}
