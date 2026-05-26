package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.uninitialized.Uninitialized;
import it.unive.scsr.checkers.UninitializedVariableChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

public class UninitializedVariableTest {

    @Test
    public void testUninitializedAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/uninitialized/uninitialized.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/uninitialized";

        conf.outputs.add(new HtmlResults(true));

        conf.analysis = simpleDomain(
                new PointBasedHeap(),
                new Uninitialized(),
                defaultTypeDomain()
        );

        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        conf.semanticChecks.add(new UninitializedVariableChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}