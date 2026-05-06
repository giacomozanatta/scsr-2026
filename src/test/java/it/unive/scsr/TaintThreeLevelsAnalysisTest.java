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

public class TaintThreeLevelsAnalysisTest {

    String[] nameSources    = {"source1"};
    String[] nameSanitizers = {"sanitizer1"};
    String[] nameSinks      = {"sink1"};

    @Test
    public void testTaintThreeLevels() throws ParsingException, AnalysisException {
        // Parse the input program to obtain its CFG representation
        Program program = IMPFrontend.processFile("inputs/taintthreelevels.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        conf.workdir = "outputs/taint-three-levels";

        conf.outputs.add(new HtmlResults<>(true));

        conf.analysis = simpleDomain(defaultHeapDomain(), new TaintThreeLevels(), defaultTypeDomain());

        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if (isSanitizer(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if (isSink(name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
        }

        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

        conf.outputs.add(new JSONReportDumper());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    private boolean isSource(String name) {
        for (String src : nameSources)
            if (src.equals(name)) return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for (String san : nameSanitizers)
            if (san.equals(name)) return true;
        return false;
    }

    private boolean isSink(String name) {
        for (String sink : nameSinks)
            if (sink.equals(name)) return true;
        return false;
    }
}