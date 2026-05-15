package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.sign.extended.ExtendedSign;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ExtendedSignAnalysisTest {

    @Test
    public void testExtendedSignAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/signs.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/extended-sign";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new ExtendedSign(), defaultTypeDomain());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

}
