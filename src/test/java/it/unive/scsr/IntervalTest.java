package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.interval.Interval;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class IntervalTest {
    @Test
    public void testInterval() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/intervals.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/intervals-test";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}

