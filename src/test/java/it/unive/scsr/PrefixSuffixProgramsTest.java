package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;

public class PrefixSuffixProgramsTest {

	@Test
	public void testPrefixHttp() throws ParsingException, AnalysisException {
		runPrefix("inputs/prefixsuffix/prefix-http.imp", "prefix-http");
	}

	@Test
	public void testPrefixConditional() throws ParsingException, AnalysisException {
		runPrefix("inputs/prefixsuffix/prefix-conditional.imp", "prefix-conditional");
	}

	@Test
	public void testPrefixLoop() throws ParsingException, AnalysisException {
		runPrefix("inputs/prefixsuffix/prefix-loop.imp", "prefix-loop");
	}

	@Test
	public void testSuffixDotCom() throws ParsingException, AnalysisException {
		runSuffix("inputs/prefixsuffix/suffix-dotcom.imp", "suffix-dotcom");
	}

	@Test
	public void testSuffixConditional() throws ParsingException, AnalysisException {
		runSuffix("inputs/prefixsuffix/suffix-conditional.imp", "suffix-conditional");
	}

	@Test
	public void testSuffixLoop() throws ParsingException, AnalysisException {
		runSuffix("inputs/prefixsuffix/suffix-loop.imp", "suffix-loop");
	}

	private void runPrefix(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);
		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/prefixsuffix/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new Prefix(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new HTTPStringChecker<>());
		conf.outputs.add(new JSONReportDumper());
		new LiSA(conf).run(program);
	}

	private void runSuffix(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);
		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/prefixsuffix/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new Suffix(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new DotComStringChecker<>());
		conf.outputs.add(new JSONReportDumper());
		new LiSA(conf).run(program);
	}
}