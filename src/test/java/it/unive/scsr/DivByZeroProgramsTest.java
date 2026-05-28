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
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;

public class DivByZeroProgramsTest {

    private static final String[] OWN = {
            "inputs/divbyzero/divbyzero-direct.imp",
            "inputs/divbyzero/divbyzero-conditional.imp",
            "inputs/divbyzero/divbyzero-loop.imp",
    };

    @Test
    public void testOwnDivByZeroPrograms() throws ParsingException, AnalysisException {
        for (String file : OWN) {
            Program program = IMPFrontend.processFile(file);

            LiSAConfiguration conf = new DefaultConfiguration();
            String base = file.substring(file.lastIndexOf('/') + 1).replace(".imp", "");
            conf.workdir = "outputs/divbyzero/" + base;
            conf.analysis = simpleDomain(new PointBasedHeap(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
            conf.outputs.add(new JSONReportDumper());

            new LiSA(conf).run(program);
        }
    }
}