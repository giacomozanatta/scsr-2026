package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.parity.Parity;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ParityTest {

    @Test
    public void testParity() throws ParsingException, AnalysisException {
        // Parse the IMP program to get its CFG representation
        Program program = IMPFrontend.processFile("inputs/parity-eval.imp");

        // Build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // Specify where output files should be generated
        conf.workdir = "outputs/parity";

        // Specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));

        // Specify the Parity domain as our analysis
        conf.analysis = simpleDomain(defaultHeapDomain(), new Parity(), defaultTypeDomain());

        // Instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // Run the analysis
        lisa.run(program);
    }
}
