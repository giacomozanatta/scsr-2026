package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.JSONResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.parity.ParitySolution; 
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ParityTaskEvaluation {

    @Test
    public void testParity() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/parity.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/parity-eval";

        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONResults<>());
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(
                defaultHeapDomain(), 
                new ParitySolution(), 
                defaultTypeDomain()
        );

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
        
        System.out.println("Analysis finished! Check outputs/parity-eval for results.");
    }
}