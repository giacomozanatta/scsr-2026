package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.extendedsign.signtaint.ExtendedSignTaint;

import it.unive.scsr.checkers.ExtendedSignTaintSinkChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ExtendedSignTaintAnalysisTest {

    // Funzioni del programma extendedsign_taint.imp
    String[] nameSources    = {"source1"};
    String[] nameSanitizers = {"sanitizer1"};
    String[] nameSinks      = {"sink1"};

    @Test
    public void testExtendedSignTaint() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/extendedsign_taint.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/extendedsign-taint";
        conf.outputs.add(new HtmlResults<>(true));

        conf.analysis = simpleDomain(new PointBasedHeap(), new ExtendedSignTaint(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            } else if (isSanitizer(name)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            } else if (isSink(name)) {
                cfg.getDescriptor().addAnnotation(ExtendedSignTaint.SINK_ANNOTATION);
            }
        }

        conf.semanticChecks.add(new ExtendedSignTaintSinkChecker<>());

        conf.outputs.add(new JSONReportDumper());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    private boolean isSource(String name) {
        for (String s : nameSources)    if (s.equals(name)) return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for (String s : nameSanitizers) if (s.equals(name)) return true;
        return false;
    }

    private boolean isSink(String name) {
        for (String s : nameSinks)      if (s.equals(name)) return true;
        return false;
    }
}
