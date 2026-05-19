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
import it.unive.scsr.analysis.cartesian.ExtendedSignTaint;
import it.unive.scsr.analysis.cartesian.ExtendedSignTaintChecker;
import it.unive.scsr.analysis.cartesian.ExtendedSignTaintLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ExtendedSignTaintAnalysisTest {
    String[] sourceNames = {"source", "GetRequest", "source1"};
    String[] nameSanitizers = {"sanitizer1"};
    String[] sinkNames = {"sink", "db_query", "system_exec", "sink1"};

    @Test
    public void testExtendedSignTaintAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/project/cartesian/extendedSignTaint.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/extendedSignTaint";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new ExtendedSignTaint(), defaultTypeDomain());

        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        for(CFG cfg : program.getAllCFGs()){
            String name = cfg.getDescriptor().getName();

            if(isSource(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if(isSanitizer(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if(isSink(name)){
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
            }
        }

        // we add checkers
        conf.semanticChecks.add(new ExtendedSignTaintChecker<>());
        // we add the report.json file
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);
        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    private boolean isSource(String name){
        for(String s : sourceNames){
            if(s.equals(name))
                return true;
        }
        return false;
    }

    private boolean isSanitizer(String name){
        for (String s : nameSanitizers){
            if(s.equals(name))
                return true;
        }
        return false;
    }
    private boolean isSink(String name){
        for (String s : sinkNames){
            if(s.equals(name))
                return true;
        }
        return false;
    }
}
