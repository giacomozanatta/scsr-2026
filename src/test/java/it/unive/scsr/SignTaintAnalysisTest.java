package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.signtaint.SignTaint;
import it.unive.scsr.analysis.signtaint.SignTaintChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class SignTaintAnalysisTest {

    @Test
    public void testSignTaintAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/sign-taint.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/sign-taint";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new SignTaint(), defaultTypeDomain());
        conf.outputs.add(new JSONReportDumper());

        for (CFG cfg : program.getAllCFGs()) {
            if ("sink".equals(cfg.getDescriptor().getName()))
                cfg.getDescriptor().addAnnotation(SignTaint.SINK_ANNOTATION);
        }

        conf.semanticChecks.add(new SignTaintChecker<>());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}
