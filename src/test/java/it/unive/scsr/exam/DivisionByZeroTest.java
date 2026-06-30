package it.unive.scsr.exam;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;

public class DivisionByZeroTest {
  @Test
  public void testIntervalDivByZero() throws ParsingException, AnalysisException {
    String inputPath = "inputs/exam/other/divisionbyzero.imp";
    String outputPath = "outputs/exam/other/divisionByZeroInterval";

    Program program = IMPFrontend.processFile(inputPath);

    LiSAConfiguration conf = new DefaultConfiguration();

    conf.workdir = outputPath;
    conf.outputs.add(new HtmlResults<>(true));
    conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
    conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
    conf.outputs.add(new JSONReportDumper());

    LiSA lisa = new LiSA(conf);
    lisa.run(program);

    System.out.println("Analysis completed for " + inputPath);
    System.out.println("Check outputs in: " + outputPath);
  }

  // @Test
  // public void testPentagonDivByZero() throws ParsingException,
  // AnalysisException {
  // String inputPath = "inputs/exam/other/divisionbyzero.imp";
  // String outputPath = "outputs/exam/other/divisionByZeroPentagon";

  // Program program = IMPFrontend.processFile(inputPath);

  // LiSAConfiguration conf = new DefaultConfiguration();

  // conf.workdir = outputPath;
  // conf.outputs.add(new HtmlResults<>(true));
  // conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(),
  // defaultTypeDomain());
  // conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
  // conf.outputs.add(new JSONReportDumper());

  // LiSA lisa = new LiSA(conf);
  // lisa.run(program);

  // System.out.println("Analysis completed for " + inputPath);
  // System.out.println("Check outputs in: " + outputPath);
  // }
}