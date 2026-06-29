package it.unive.scsr;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;

public class TaintThreeLevelsAnalysisTest {

  String[] nameSource = {
      "readHttpRequestParam",
      "readCookieValue",
      "readUploadedFileName",
      "readAdminConsoleInput"
  };

  String[] nameSanitizers = {
      "sanitizeSqlIdentifier",
      "escapeHtml",
      "validateRedirectPath",
      "maskForAudit"
  };

  String[] nameSinks = {
      "executeSqlQuery",
      "renderHtmlPage",
      "redirectToUrl",
      "writeAuditLog",
      "sendNotification"
  };

  @Test
  public void testTaintAnalysis() throws ParsingException, AnalysisException {
    Program program = IMPFrontend.processFile("inputs/exam/personal/taint-three-levels.imp");

    LiSAConfiguration conf = new DefaultConfiguration();
    conf.workdir = "outputs/exam/personal/taintThreeLevels";

    conf.outputs.add(new HtmlResults<>(true));
    conf.analysis = simpleDomain(defaultHeapDomain(), new TaintThreeLevels(), defaultTypeDomain());

    for (CFG cfg : program.getAllCFGs()) {
      String name = cfg.getDescriptor().getName();
      if (isSource(name))
        cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
      else if (isSanitizer(name))
        cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
      else if (isSink(name))
        cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
    }

    conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
    conf.outputs.add(new JSONReportDumper());

    LiSA lisa = new LiSA(conf);
    lisa.run(program);
  }

  private boolean isSource(String name) {
    for (String src : nameSource)
      if (src.equals(name))
        return true;
    return false;
  }

  private boolean isSanitizer(String name) {
    for (String sanit : nameSanitizers)
      if (sanit.equals(name))
        return true;
    return false;
  }

  private boolean isSink(String name) {
    for (String sink : nameSinks)
      if (sink.equals(name))
        return true;
    return false;
  }
}
