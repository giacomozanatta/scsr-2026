package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.JSONResults;
import it.unive.lisa.outputs.compare.ResultComparer;
import it.unive.lisa.outputs.json.JsonReport;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.CProp;
import it.unive.scsr.analysis.CPropSolution;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static it.unive.lisa.DefaultConfiguration.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CPropTaskEvaluation {
    @Test
    public void testCP() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/cp-eval.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        conf.workdir = "outputs/cp-eval";

        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONResults<>());
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(defaultHeapDomain(), new CPropSolution(), defaultTypeDomain());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);

        Path expectedPath = Paths.get("expected", "cp-eval");
        Path actualPath = Paths.get("outputs", "cp-eval");

        File expFile = Paths.get(expectedPath.toString(), "report.json").toFile();
        File actFile = Paths.get(actualPath.toString(), "report.json").toFile();
        try {
            JsonReport expected = JsonReport.read(new FileReader(expFile));
            JsonReport actual = JsonReport.read(new FileReader(actFile));
            assertTrue("Results are different",
                    new ResultComparer().compare(expected, actual, expectedPath.toFile(), actualPath.toFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            fail("Unable to find report file");
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail("Unable to compare reports");
        }
    }
}
