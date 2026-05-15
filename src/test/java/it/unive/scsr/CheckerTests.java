package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.scsr.analysis.intervalfloat.FloatInterval;
import it.unive.scsr.analysis.intervalfloat.IntervalFloat;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;

import it.unive.scsr.checkers.*;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class CheckerTests {
    @Test
    public void testOverUnder() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/checkertests/overunderflow_1-2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/checkers/overunderflow";
        conf.outputs.add(new HtmlResults<>(true));

        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(0, 10));
        conf.outputs.add(new JSONReportDumper());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void testDivisionByZero() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/checkertests/divisionbyzero_1-2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/checkers/divisionbyzero";
        conf.outputs.add(new HtmlResults<>(true));

        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalFloat(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.semanticChecks.add(new DivByZeroPentagonChecker<>());

        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void prefixSuffixCheck() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/checkertests/prefixsuffix_1.imp"); // TODO: update as needed to check the others
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/checkers/prefixsuffix";
        conf.outputs.add(new HtmlResults<>(true));

        // conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        // conf.semanticChecks.add(new DotComStringChecker<>());
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void testTaintAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/checkertests/taintthreelevel_1-2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/checkers/taintthreelevel";
        conf.outputs.add(new HtmlResults<>(true));

        conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
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

    String[] nameSource = {"source1", "source", "GetRequest", "sourceSerializedObject"};
    String[] nameSanitizers = {"sanitizer1", "sanitizeJob", "sanitizeParam"};
    String[] nameSinks = {"sink1", "sink", "runQueryDB", "deserializePathJob"};

    private boolean isSource(String name) {
        for (String src : nameSource)
            if (src.equals(name))
                return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for (String sanit : nameSanitizers)
            if (sanit.equals(name))
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