package it.unive.scsr;


import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.scsr.analysis.nullability.Nullability;
import it.unive.scsr.analysis.nullability.NullabilityChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class NullabilityTest {
    @Test
    public void testNullabilityAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/nullability.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/nullability";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Nullability(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new NullabilityChecker<>());
        new LiSA(conf).run(program);
    }
}
