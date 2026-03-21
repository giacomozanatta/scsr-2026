package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.ConstantValuePropagation;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.lattices.ConstantValue;
import it.unive.lisa.outputs.HtmlInputs;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class HelloWorldTest {
    @Test
    public void testHelloWorld() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/hello-world.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/hello-world";

        // we specify the visual format of the analysis results
        conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));

        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(),new ConstantValuePropagation(),defaultTypeDomain());
        //conf.analysis = DefaultConfiguration.defaultAbstractDomain();

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }
}
