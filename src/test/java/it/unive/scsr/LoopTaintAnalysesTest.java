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

import it.unive.scsr.analysis.cartesian.FloatIntervalThreeTaint;
import it.unive.scsr.checkers.UnboundedTaintedLoopChecker;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class LoopTaintAnalysesTest {

    String[] nameSource = {"source1", "GetRequest", "readInput"};
    String[] nameSanitizers = {"sanitizer1", "cleanData"};

    @Test
    public void testUnboundedTaintedLoopAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/loop_taint_tests.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/loop_taint";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(new PointBasedHeap(), new FloatIntervalThreeTaint(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            } else if (isSanitizer(name)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            }
        }

        conf.semanticChecks.add(new UnboundedTaintedLoopChecker<>());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    private boolean isSource(String name) {
        for (String src : nameSource) if (src.equals(name)) return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for (String s : nameSanitizers) if (s.equals(name)) return true;
        return false;
    }
}