package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class DivByZeroTest {

    @Test
    public void testDivByZeroDefinite() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/divbyzero/divbyzero.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        conf.workdir = "outputs/" + "divbyzero";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(
                defaultHeapDomain(),
                new Interval(),
                defaultTypeDomain()
        );
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);

        System.out.println("Analysis completed for " + "inputs/divbyzero/divbyzero.imp");
        System.out.println("Check outputs in: outputs/divbyzero/" + "divbyzero");
    }
}
