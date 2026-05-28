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
import it.unive.scsr.analysis.cartesian.Cartesian;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.checkers.CartesianChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

public class CartesianAnalysisTest {

    String[] nameSource = {"taint"};
    String[] nameSanitizers = {"sanitize"};
    String[] nameSinks = {"sink1"};

    @Test
    public void testCartesianAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/cartesian/cartesian.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/cartesian";

        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(new PointBasedHeap(), new Cartesian(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new CartesianChecker<>());

        // Annotate CFGs for the taint component
        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.TAINTED_ANNOTATION);
            else if (isSanitizer(name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.CLEAN_ANNOTATION);
            else if (isSink(name))
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
        }

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    private boolean isSource(String name) {
        for (String src : nameSource)
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