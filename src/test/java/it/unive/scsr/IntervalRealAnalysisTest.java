package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlInputs;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.JSONResults;
// import it.unive.lisa.outputs.compare.ResultComparer;
// import it.unive.lisa.outputs.json.JsonReport;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.interval_real.IntervalReal;

// import static org.junit.Assert.assertTrue;
// import static org.junit.Assert.fail;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileReader;
// import java.io.IOException;
// import java.nio.file.Path;
// import java.nio.file.Paths;

public class IntervalRealAnalysisTest {

    @Test
    public void testIntervalRealAnalysis() throws ParsingException, AnalysisException {
        // Parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/interval_real/interval_real.imp");

        // Build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // Specify where we want files to be generated
        conf.workdir = "outputs/interval_real/analysis";

        // Specify the visual format of the analysis results
        conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONResults<>());
        conf.outputs.add(new JSONReportDumper());

        // Specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalReal(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);

    }
}
