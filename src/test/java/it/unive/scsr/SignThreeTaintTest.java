package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.*;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.sign_three_taint.SignThreeTaint;
import it.unive.scsr.analysis.sign_three_taint.SignThreeTaintChecker;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;

import org.junit.Test;

public class SignThreeTaintTest {
    private static final String[] SOURCES = {"source1"};
    private static final String[] SANITIZERS = {"sanitizer1"};
    private static final String[] SINKS = {"sink1"};

    @Test
    public void testSignThreeTaintAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/sign_three_taint/sign_three_taint.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/sign_three_taint";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(defaultHeapDomain(), new SignThreeTaint(), defaultTypeDomain());

        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (contains(SOURCES, name))
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if (contains(SANITIZERS, name))
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if (contains(SINKS, name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
        }

        conf.semanticChecks.add(new SignThreeTaintChecker<>());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    private static boolean contains(String[] names, String name) {
        for (String n : names)
            if (n.equals(name)) return true;
        return false;
    }
}
