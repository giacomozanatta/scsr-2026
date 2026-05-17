package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.interval.floats.FloatInterval;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class IntervalFloatAnalysisTest {

    @Test
    public void testFloatIntervalAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/intervals-float.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/float-interval";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new FloatInterval(), defaultTypeDomain());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

}
