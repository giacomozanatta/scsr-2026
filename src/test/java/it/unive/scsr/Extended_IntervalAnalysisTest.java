package it.unive.scsr;

import static org.junit.Assert.fail;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.scsr.analysis.Extended_Interval.Extended_Interval;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.JSONResults;
import it.unive.lisa.outputs.compare.ResultComparer;
import it.unive.lisa.outputs.json.JsonReport;
import it.unive.lisa.program.Program;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Extended_IntervalAnalysisTest {

    @Test
    public void testIntervalAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        
        
        Program program = IMPFrontend.processFile("inputs/intervals.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/extended_intervals";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONResults<>());
        conf.outputs.add(new JSONReportDumper());
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Extended_Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        // added checker to the analysis
        //conf.semanticChecks.add(new OverflowIntervalChecker<>(-128, 127)); //interval range added for testing the overflow
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
        
        //Path expectedPath = Paths.get("expected", "intervals-eval");
        Path actualPath = Paths.get("outputs", "extended_intervals");

        //File expFile = Paths.get(expectedPath.toString(), "report.json").toFile();
        File actFile = Paths.get(actualPath.toString(), "report.json").toFile();
        try {
            //JsonReport expected = JsonReport.read(new FileReader(expFile));
            JsonReport actual = JsonReport.read(new FileReader(actFile));
            //assertTrue("Results are different",
                    //new ResultComparer().compare(expected, actual, expectedPath.toFile(), actualPath.toFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
            fail("Unable to find report file");
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail("Unable to compare reports");
        }
    }
    
}
