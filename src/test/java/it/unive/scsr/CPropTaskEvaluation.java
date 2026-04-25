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
public class CPropTaskEvaluation {

    @Test
    public void testConstantPropagation() throws ParsingException, AnalysisException {
        // We parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/cprop.imp");

        // We build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/cprop";

        // We specify where we want files to be generated
        conf.outputs.add(new HtmlResults<>(true));

        // we specify the analysis that we want to execute in this case is Constant Propagation
        conf.analysis = simpleDomain(defaultHeapDomain(), new CProp(), defaultTypeDomain());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // We tell LiSA to analyze the program
        lisa.run(program);
    }
}