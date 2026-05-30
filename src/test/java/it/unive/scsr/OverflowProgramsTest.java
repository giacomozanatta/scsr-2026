package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.OverflowIntervalChecker;

public class OverflowProgramsTest {

    private static final String[] OWN = {
            "inputs/overflow/overflow-addition.imp",
            "inputs/overflow/overflow-subtraction.imp",
            "inputs/overflow/overflow-multiplication.imp",
    };

    @Test
    public void testOwnOverflowPrograms() throws ParsingException, AnalysisException {
        for (String file : OWN) {
            Program program = IMPFrontend.processFile(file);

            LiSAConfiguration conf = new DefaultConfiguration();
            String base = file.substring(file.lastIndexOf('/') + 1).replace(".imp", "");
            conf.workdir = "outputs/overflow/" + base;
            conf.analysis = simpleDomain(new PointBasedHeap(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());

            new LiSA(conf).run(program);
        }
    }
}