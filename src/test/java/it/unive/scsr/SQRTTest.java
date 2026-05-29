package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.interval.FloatInterval;
import it.unive.scsr.checkers.SquareRootChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class SQRTTest {
    @Test
    public void testSqrtAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/sqrt.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/definitive_sqrt";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(defaultHeapDomain(), new FloatInterval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new SquareRootChecker<>());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}