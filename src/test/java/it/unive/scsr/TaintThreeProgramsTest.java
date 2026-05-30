package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

import org.junit.Test;

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

public class TaintThreeProgramsTest {

	@Test
	public void testTaintThreeSimple() throws ParsingException, AnalysisException {
		run("inputs/taint/taint-three-simple.imp", "taint-three-simple");
	}

	@Test
	public void testTaintThreePropagation() throws ParsingException, AnalysisException {
		run("inputs/taint/taint-three-propagation.imp", "taint-three-propagation");
	}

	@Test
	public void testTaintThreeSanitization() throws ParsingException, AnalysisException {
		run("inputs/taint/taint-three-sanitization.imp", "taint-three-sanitization");
	}

	private void run(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);
		annotate(program);

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/taint-three-programs/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());

		new LiSA(conf).run(program);
	}

	private void annotate(Program program) {
		for (CFG cfg : program.getAllCFGs()) {
			String name = cfg.getDescriptor().getName();
			if (isSource(name))
				cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
			else if (isSanitizer(name))
				cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
			else if (isSink(name))
				cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
		}
	}

	private boolean isSource(String name) {
		String lower = name.toLowerCase();
		return lower.startsWith("get") || lower.startsWith("read");
	}

	private boolean isSanitizer(String name) {
		String lower = name.toLowerCase();
		return lower.contains("sanitize") || lower.contains("sanit");
	}

	private boolean isSink(String name) {
		String lower = name.toLowerCase();
		return lower.contains("sink") || lower.contains("execute") || lower.contains("write")
				|| lower.equals("usevalue") || lower.equals("process");
	}
}
