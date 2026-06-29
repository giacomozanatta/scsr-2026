package it.unive.scsr.exam;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import it.unive.scsr.checkers.OverflowPentagonChecker;

public class OverflowTest {
  @Test
  public void testIntervalOverflow() throws ParsingException, AnalysisException {
    String inputPath = "inputs/exam/personal/overflow.imp";
    String outputPath = "outputs/exam/personal/overflowInterval";

    Program program = IMPFrontend.processFile(inputPath);

    LiSAConfiguration conf = new DefaultConfiguration();

    conf.workdir = outputPath;
    conf.outputs.add(new HtmlResults<>(true));
    conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
    conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
    conf.outputs.add(new JSONReportDumper());

    LiSA lisa = new LiSA(conf);
    lisa.run(program);

    System.out.println("Analysis completed for " + inputPath);
    System.out.println("Check outputs in: " + outputPath);
  }

  @Test
  public void testPentagonOverlflow() throws ParsingException, AnalysisException {
    String inputPath = "inputs/exam/personal/overflow.imp";
    String outputPath = "outputs/exam/personal/divisionByZeroPentagon";

    Program program = IMPFrontend.processFile(inputPath);

    LiSAConfiguration conf = new DefaultConfiguration();

    conf.workdir = outputPath;
    conf.outputs.add(new HtmlResults<>(true));
    conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
    conf.semanticChecks.add(new OverflowPentagonChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
    conf.outputs.add(new JSONReportDumper());

    LiSA lisa = new LiSA(conf);
    lisa.run(program);

    System.out.println("Analysis completed for " + inputPath);
    System.out.println("Check outputs in: " + outputPath);
  }

  @Test
  public void testOthersOverflow() throws ParsingException, AnalysisException {
    Program program = IMPFrontend.processFile("inputs/others/overflow/overflow.imp");

    LiSAConfiguration conf = new DefaultConfiguration();

    conf.workdir = "outputs/others/" + "overflow";
    conf.outputs.add(new HtmlResults<>(true));
    conf.analysis = simpleDomain(
        defaultHeapDomain(),
        new Interval(),
        defaultTypeDomain());
    // MIN_VALUE = -2147483648, MAX_VALUE = 2147483647
    conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
    conf.outputs.add(new JSONReportDumper());

    LiSA lisa = new LiSA(conf);
    lisa.run(program);

    System.out.println("Analysis completed for " + "inputs/others/overflow/overflow.imp");
    System.out.println("Check outputs in: outputs/others/" + "overflow");
  }
}