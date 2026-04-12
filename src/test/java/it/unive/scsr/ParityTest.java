package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlInputs;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.JSONResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.parity.Parity;
import org.junit.Test;
import static it.unive.lisa.DefaultConfiguration.*;

public class ParityTest {

    @Test
    public void testParity() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/parity.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/parity";
        conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONResults<>());
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Parity(), defaultTypeDomain());

        new LiSA(conf).run(program);
    }
}
