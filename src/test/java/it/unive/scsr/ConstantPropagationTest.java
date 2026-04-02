package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.CProp;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

/**
 * @author Alan Dal Col 895879
 * @author Mattia Acquilesi 896827
 */
public class ConstantPropagationTest {

    @Test
    public void testConstantPropagationAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/constant-propagation.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        conf.workdir = "outputs/constant-propagation";

        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new CProp(), defaultTypeDomain());
        LiSA lisa = new LiSA(conf);

        lisa.run(program);
    }
}
