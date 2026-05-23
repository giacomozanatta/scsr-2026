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
import it.unive.scsr.checkers.AssignmentNotUsedChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class AssignmentNotUsedTest {

    @Test
    public void testAssignmentNotUsed() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/assignment-not-used.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/assignment-not-used";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), defaultValueDomain(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new AssignmentNotUsedChecker<>());
        conf.outputs.add(new JSONReportDumper());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}
