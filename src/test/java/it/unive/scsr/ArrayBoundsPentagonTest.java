package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.ArrayBoundsPentagonChecker;

public class ArrayBoundsPentagonTest {

    @Test
    public void testArrayBounds() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/peer-programs/taintthreelevels/array.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/array-bounds-pentagon";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        conf.semanticChecks.add(new ArrayBoundsPentagonChecker<>());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}
