package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class TaintThreeLevelsAnalysisTest {

	String[] nameSource     = {"getRequestParam", "readRawInput", "readUserCommand"};
	String[] nameSanitizers = {"escapeHtml", "sanitize"};
	String[] nameSinks      = {"runQueryDB", "renderPage", "executeCommand"};

	@Test
	public void testTaintThreeLevelsUserLogin() throws ParsingException, AnalysisException {
		runAnalysis("inputs/taint-userLogin.imp", "outputs/taint-threelevels-userLogin");
	}

	@Test
	public void testTaintThreeLevelsCommentForm() throws ParsingException, AnalysisException {
		runAnalysis("inputs/taint-commentForm.imp", "outputs/taint-threelevels-commentForm");
	}

	@Test
	public void testTaintThreeLevelsShellExec() throws ParsingException, AnalysisException {
		runAnalysis("inputs/taint-shellExec.imp", "outputs/taint-threelevels-shellExec");
	}

	private void runAnalysis(String inputPath, String workdir) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(inputPath);

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = workdir;
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());
		conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

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

		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}

	private boolean isSource(String name) {
		for (String src : nameSource)
			if (src.equals(name)) return true;
		return false;
	}

	private boolean isSanitizer(String name) {
		for (String sanit : nameSanitizers)
			if (sanit.equals(name)) return true;
		return false;
	}

	private boolean isSink(String name) {
		for (String sink : nameSinks)
			if (sink.equals(name)) return true;
		return false;
	}
}
