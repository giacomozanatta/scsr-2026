package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.cartesian.SignThreeTaint;
import it.unive.scsr.analysis.cartesian.SignThreeTaintChecker;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class SignThreeTaintAnalysisTest {
    private final String[] sources = {"source1", "GetRequest", "readInput"};
    private final String[] sanitizers = {"sanitizer1", "cleanData"};
    private final String[] sinks = {"sink1", "runQueryDB", "printSensitive"};

    @Test
    public void testSignThreeTaint() throws ParsingException, AnalysisException {

        Program program = IMPFrontend.processFile("inputs/signthreetaint.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        conf.workdir = "outputs/signthreetaint";

        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(new PointBasedHeap(), new SignThreeTaint(), defaultTypeDomain());

        conf.interproceduralAnalysis =
                new ContextBasedAnalysis<>();

        // Annotazione Source / Sanitizer / Sink
        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);

            } else if (isSanitizer(name)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);

            } else if (isSink(name)) {
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
            }
        }

        conf.semanticChecks.add(
                new SignThreeTaintChecker<>()
        );

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    private boolean isSource(String name) {
        for (String source : sources)
            if (source.equals(name))
                return true;

        return false;
    }

    private boolean isSanitizer(String name) {
        for (String sanitizer : sanitizers)
            if (sanitizer.equals(name))
                return true;

        return false;
    }

    private boolean isSink(String name) {
        for (String sink : sinks)
            if (sink.equals(name))
                return true;

        return false;
    }

}
