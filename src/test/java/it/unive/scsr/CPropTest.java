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

public class CPropTest {

    @Test
    public void testConstantPropagation() throws ParsingException, AnalysisException {
        // Parse cprop.imp
        Program program = IMPFrontend.processFile("inputs/cprop.imp");

        // Configure lisa analyzer
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/cprop";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new CProp(), defaultTypeDomain());

        // Run
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}