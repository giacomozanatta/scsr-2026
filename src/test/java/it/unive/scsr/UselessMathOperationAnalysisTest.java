package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.UselessMathOpChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class UselessMathOperationAnalysisTest {

	@Test
	public void testUselessMathOperationAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/useless_op.imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/useless_op";

		// we specify the visual format of the analysis results
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
//		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		// added checker to the analysis
		conf.semanticChecks.add(new UselessMathOpChecker<>());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);

	}
}
