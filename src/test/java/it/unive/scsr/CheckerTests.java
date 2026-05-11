package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.scsr.analysis.interval.Interval;
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
        // we parse the program to get the CFG representation of the code in it
        //  Program program = IMPFrontend.processFile("inputs/barbtest.intervalpaolo.imp");
        Program program = IMPFrontend.processFile("inputs/checkertests/overunderflow.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        // conf.workdir = "outputs/barbintervalpaolo";
        conf.workdir = "outputs/checkers/overunderflow";

        // we specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalFloat(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(0, 10));
        ///// conf.semanticChecks.add(new OverflowPentagonChecker<>());
        //conf.analysis = DefaultConfiguration.defaultAbstractDomain();
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    @Test
    public void testDivisionByZero() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        //  Program program = IMPFrontend.processFile("inputs/barbtest.intervalpaolo.imp");
        Program program = IMPFrontend.processFile("inputs/checkertests/divisionbyzero.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        // conf.workdir = "outputs/barbintervalpaolo";
        conf.workdir = "outputs/checkers/divisionbyzero";

        // we specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalFloat(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
        //conf.analysis = DefaultConfiguration.defaultAbstractDomain();
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    @Test
    public void prefixSuffixCheck() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        //  Program program = IMPFrontend.processFile("inputs/barbtest.intervalpaolo.imp");
        Program program = IMPFrontend.processFile("inputs/checkertests/prefixsuffix.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        // conf.workdir = "outputs/barbintervalpaolo";
        conf.workdir = "outputs/checkers/prefixsuffix";

        // we specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new TaintThreeLevels(), defaultTypeDomain());
        conf.semanticChecks.add(new DotComStringChecker<>());
        conf.semanticChecks.add(new HTTPStringChecker<>());
        //conf.analysis = DefaultConfiguration.defaultAbstractDomain();
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    @Test
    public void testTaintAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checkertests/taintthreelevel.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checkers/taintthreelevel";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
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

        // added checker to the analysis
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());
        ;

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    String[] nameSource = {"source1", "source", "GetRequest"};
    String[] nameSanitizers = {"sanitizer1"};
    String[] nameSinks = {"sink1", "sink", "runQueryDB"};

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