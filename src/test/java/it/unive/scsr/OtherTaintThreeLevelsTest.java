package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsCheckerSolution;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsSolution;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

public class TaintThreeLevelsShellTest {

    String[] nameSource = {
            "source1",      // From 904329_TaintThreeLevels_Checker, 913849_threetaint3
            "user_input",   // From 913849_threetaint
            "GetRequest",   // From 876957-taint, 881299_taint, 894004_taint
            "getUserInput"  // From 894069_taint
    };

    String[] nameSanitizers = {
            "sanitizer1",      // From 904329_TaintThreeLevels_Checker, 913849_threetaint3
            "sanitized_value", // From 913849_threetaint
            "sanitizeInput"    // From 876957-taint, 894069_taint
    };

    String[] nameSinks = {
            "sink1",      // From 904329_TaintThreeLevels_Checker, 913849_threetaint3
            "evaluate",   // From 913849_threetaint
            "runQueryDB", // From 876957-taint, 881299_taint, 894004_taint
            "sendEmail"   // From 894069_taint
    };

    @Test
    public void testTaintAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/others/tainthreelevels/other.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/others";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevelsSolution(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.TAINTED_ANNOTATION);
            else if (isSanitizer(name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.CLEAN_ANNOTATION);
            else if (isSink(name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
        }

        // added checker to the analysis
        conf.semanticChecks.add(new TaintThreeLevelsCheckerSolution<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    private boolean isSource(String name) {
        for (String src : nameSource)
            if (src.equals(name))
                return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for (String sanitizer : nameSanitizers)
            if (sanitizer.equals(name))
                return true;
        return false;
    }

    private boolean isSink(String name) {
        for (String sink : nameSinks)
            if (sink.equals(name))
                return true;
        return false;
    }
}


