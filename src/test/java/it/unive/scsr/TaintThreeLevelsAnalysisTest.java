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
import it.unive.scsr.analysis.taint.Taint;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

/**
 * @author Mattia Acquilesi 896827
 * @author Alan Dal Col 895879
 */
public class TaintThreeLevelsAnalysisTest {

    String[] nameSource = {"source1", "GetRequest"};
    String[] nameSanitizers = {"sanitizer1"};
    String[] nameSinks = {"sink1", "runQueryDB"};


    @Test
    public void testTaintThreeLevelsAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/taint.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/threeTaint";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        for(CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if(isSource(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if(isSanitizer(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if(isSink(name))
                cfg.getDescriptor().addAnnotation(Taint.SINK_ANNOTATION);
        }

        // added checker to the analysis
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder 
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    private boolean isSource(String name) {
        for(String src : nameSource)
            if(src.equals(name))
                return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for(String sanit : nameSanitizers)
            if(sanit.equals(name))
                return true;
        return false;
    }

    private boolean isSink(String name) {
        for(String sink : nameSinks)
            if(sink.equals(name))
                return true;
        return false;
    }
}
