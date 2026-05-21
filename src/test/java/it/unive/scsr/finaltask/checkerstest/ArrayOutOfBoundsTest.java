package it.unive.scsr.finaltask.checkerstest;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.BaseHeapDomain;
import it.unive.lisa.analysis.heap.pointbased.FieldSensitivePointBasedHeap;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.ArrayOutOfBoundsChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ArrayOutOfBoundsTest {
    @Test
    public void testArrayOutOfBoundsChecker() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/arrays.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/arrays";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(new FieldSensitivePointBasedHeap(), new Pentagon(), defaultTypeDomain());

        // added checker to the analysis
        conf.semanticChecks.add(new ArrayOutOfBoundsChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }
}
