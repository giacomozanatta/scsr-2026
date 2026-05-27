package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.scsr.analysis.parity.Parity;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ParityTest {

    @Test
    public void testParityAnalysis() throws ParsingException, AnalysisException {
        // parse the parity test program
        Program program = IMPFrontend.processFile("inputs/parity.imp");

        // build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // specify where output files will be generated
        conf.workdir = "outputs/parity";

        // specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));

        // plug in the Parity domain as the value domain
        conf.analysis = simpleDomain(defaultHeapDomain(), new Parity(), defaultTypeDomain());

        // instantiate LiSA and run the analysis
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}
