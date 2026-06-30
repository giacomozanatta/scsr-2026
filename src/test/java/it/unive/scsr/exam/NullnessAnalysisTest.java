package it.unive.scsr.exam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.ReturnTopPolicy;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.nullness.NullDereferenceChecker;
import it.unive.scsr.analysis.nullness.Nullness;

public class NullnessAnalysisTest {

  @Test
  public void testNullDereferenceAnalysis() throws ParsingException, AnalysisException, IOException {
    String inputPath = "inputs/exam/personal/nullness.imp";
    String outputPath = "outputs/exam/personal/nullness";

    Program program = IMPFrontend.processFile(inputPath);

    LiSAConfiguration conf = new DefaultConfiguration();

    conf.workdir = outputPath;
    conf.analysis = simpleDomain(defaultHeapDomain(), new Nullness(), defaultTypeDomain());
    conf.openCallPolicy = ReturnTopPolicy.INSTANCE;
    conf.semanticChecks.add(new NullDereferenceChecker<>());
    conf.outputs.add(new HtmlResults<>(true));
    conf.outputs.add(new JSONReportDumper());

    LiSA lisa = new LiSA(conf);
    lisa.run(program);

    String report = Files.readString(Paths.get(outputPath, "report.json"));
    String warnings = report.substring(0, report.indexOf("\"notices\""));

    assertEquals(0, warnings.split(Pattern.quote("[NPE]"), -1).length - 1);
    assertEquals(5, warnings.split(Pattern.quote("[POSSIBLE_NPE]"), -1).length - 1);

    String[][] expectedWarnings = {
        { "handleAuthenticatedRequest", "session" },
        { "handleAuthenticatedRequest", "auditLogger" },
        { "processAdminReport", "reportDb" },
        { "refreshSessionAfterLogout", "session" },
        { "cleanupExpiredConnections", "db" },
    };

    for (String[] expected : expectedWarnings)
      assertTrue("Missing possible null warning in " + expected[0],
          Pattern.compile("\"message\"\\s*:\\s*\"[^\"]*" + Pattern.quote(expected[0]) + "[^\"]*"
              + Pattern.quote("[POSSIBLE_NPE] '" + expected[1] + "'")).matcher(warnings).find());

    assertTrue("Safe checkout flow should not warn",
        !Pattern.compile("\"message\"\\s*:\\s*\"[^\"]*processOrderCheckout").matcher(warnings).find());

    System.out.println("Analysis completed for " + inputPath);
    System.out.println("Check outputs in: " + outputPath);
  }
}
