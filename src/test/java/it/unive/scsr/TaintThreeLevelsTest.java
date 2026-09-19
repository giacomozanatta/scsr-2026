package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.combined.SignTaint;
import it.unive.scsr.analysis.taint.Taint;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class TaintThreeLevelsTest {

    // Arrays for defining sources, sanitizers, and sinks
    private final String[] nameSource = {"source1", "GetRequest"};
    private final String[] nameSanitizers = {"sanitizer1"};
    private final String[] nameSinks = {"sink1", "runQueryDB"};

    @Test
    public void testTaintThreeLevels() throws ParsingException, AnalysisException {
        // Parse the input file containing the vulnerable code example
        Program program = IMPFrontend.processFile("inputs/taint.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/taint-three-levels";
        
        // Add HTML reports for visualization
        conf.outputs.add(new HtmlResults<>(true));

        // IMPORTANT: Annotate functions before running the analysis!
        setupAnnotations(program);

        // 1. ADD YOUR CHECKER (the one that looks for Tainted values in Sinks)
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

        // 2. SPECIFY YOUR COMBINED DOMAIN SignTaint
        conf.analysis = simpleDomain(
            defaultHeapDomain(), 
            new SignTaint(), 
            defaultTypeDomain()
        );

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    // Method that attaches TAINTED, CLEAN, and SINK annotations to functions
    private void setupAnnotations(Program program) {
        for(CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if(isMatch(name, nameSource)) 
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if(isMatch(name, nameSanitizers)) 
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if(isMatch(name, nameSinks)) 
                cfg.getDescriptor().addAnnotation(Taint.SINK_ANNOTATION);
        }
    }

    private boolean isMatch(String name, String[] list) {
        for(String s : list) if(s.equals(name)) return true;
        return false;
    }
}