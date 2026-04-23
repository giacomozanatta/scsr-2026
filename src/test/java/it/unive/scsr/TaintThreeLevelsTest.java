package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class TaintThreeLevelsTest {

    // Names of source, sanitizer and sink functions defined in taintthreelevels.imp
    String[] nameSources    = {"source1"};
    String[] nameSanitizers = {"sanitizer1"};
    String[] nameSinks      = {"sink1"};

    @Test
    public void testTaintThreeLevels() throws ParsingException, AnalysisException {
        // Parse the input program to obtain its CFG representation
        Program program = IMPFrontend.processFile("inputs/taintthreelevels.imp");

        // Build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // Specify the output directory for generated files
        conf.workdir = "outputs/taint-three-levels";

        // Specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));

        // Specify the abstract domain to use (three-level taint)
        conf.analysis = simpleDomain(defaultHeapDomain(), new TaintThreeLevels(), defaultTypeDomain());

        // Annotate CFGs as sources, sanitizers or sinks based on their name
        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name))
                // Mark as tainted: values returned by this function are tainted
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if (isSanitizer(name))
                // Mark as clean: values returned by this function are sanitized
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if (isSink(name))
                // Mark as sink: tainted values reaching this function trigger a warning
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
        }

        // Add the checker that emits DEFINITE and POSSIBLE warnings at sinks
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

        // Add a JSON report dumper to inspect warnings after the analysis
        conf.outputs.add(new JSONReportDumper());

        // Instantiate LiSA and run the analysis
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    // Returns true if the given function name is a source
    private boolean isSource(String name) {
        for (String src : nameSources)
            if (src.equals(name)) return true;
        return false;
    }

    // Returns true if the given function name is a sanitizer
    private boolean isSanitizer(String name) {
        for (String san : nameSanitizers)
            if (san.equals(name)) return true;
        return false;
    }

    // Returns true if the given function name is a sink
    private boolean isSink(String name) {
        for (String sink : nameSinks)
            if (sink.equals(name)) return true;
        return false;
    }
}