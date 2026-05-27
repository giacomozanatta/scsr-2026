package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.scsr.analysis.CProp; // <-- Changed to import your CProp class!
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class CPropTest {

    @Test
    public void testConstantPropagation() throws ParsingException, AnalysisException {
        // 1. Tell LiSA to parse your cprop.imp file
        Program program = IMPFrontend.processFile("inputs/cprop.imp");

        // 2. Build the configuration
        LiSAConfiguration conf = new DefaultConfiguration();

        // 3. Specify where we want files to be generated (changed to a cprop folder)
        conf.workdir = "outputs/cprop";

        // 4. Specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));

        // 5. THIS IS THE MAGIC LINE: Pass your new CProp() domain into the analysis!
        conf.analysis = simpleDomain(defaultHeapDomain(), new CProp(), defaultTypeDomain());

        // 6. Instantiate LiSA and run it
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}