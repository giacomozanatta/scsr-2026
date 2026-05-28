package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.JSONResults;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.fixpoints.backward.BackwardDescendingNarrowingFixpoint;
import it.unive.lisa.program.cfg.fixpoints.forward.ForwardDescendingNarrowingFixpoint;
import it.unive.scsr.analysis.floatInterval.IntervalFloat;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class FloatIntervalTest {
    @Test
    public void floatIntervalAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/float-interval.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();


        // we specify where we want files to be generated
        conf.workdir = "outputs/float-interval";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        //conf.outputs.add(new HtmlResults<>(true));
        //conf.outputs.add(new JSONResults<>());
        conf.outputs.add(new JSONReportDumper());
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new IntervalFloat(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        // added checker to the analysis

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);

    }
}
