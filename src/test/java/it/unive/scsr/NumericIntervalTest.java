package it.unive.scsr;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.JSONResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.interval.extended.NumericInterval;

public class NumericIntervalTest {

  @Test
  public void testNumericIntervalAnalysis() throws ParsingException, AnalysisException {
    Program program = IMPFrontend.processFile("inputs/exam/numericInterval/base.imp");

    LiSAConfiguration conf = new DefaultConfiguration();

    conf.workdir = "outputs/numericInterval";

    conf.outputs.add(new HtmlResults<>(true));
    conf.outputs.add(new JSONResults<>());
    conf.outputs.add(new JSONReportDumper());

    conf.analysis = simpleDomain(
        defaultHeapDomain(),
        new NumericInterval(),
        defaultTypeDomain());

    LiSA lisa = new LiSA(conf);

    lisa.run(program);
  }
}