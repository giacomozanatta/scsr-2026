package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.scsr.analysis.CProp;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlInputs;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class CPropTest {

    @Test
    public void testCProp() throws ParsingException, AnalysisException {
        // generazione del grafico CFG
        Program program = IMPFrontend.processFile("inputs/cprop.imp");

        // generazione della configurazione
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/cprop";
        conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new CProp(), defaultTypeDomain());

        // esecuzione dell'analisi
        new LiSA(conf).run(program);
    }
}
