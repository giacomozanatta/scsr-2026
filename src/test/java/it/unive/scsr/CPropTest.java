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

//Work done by Elia Stevanato 895598 & Francesco Pasqualato 897778
public class CPropTest {
    @Test
    public void testCProp() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/cprop.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        conf.workdir = "outputs/c-pro";


        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new CProp(), defaultTypeDomain());

        LiSA lisa = new LiSA(conf);

        lisa.run(program);
    }
}
