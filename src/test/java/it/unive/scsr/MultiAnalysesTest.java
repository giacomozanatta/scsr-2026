package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.compare.ResultComparer;
import it.unive.lisa.outputs.json.JsonReport;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.checkers.*;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static it.unive.lisa.DefaultConfiguration.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MultiAnalysesTest {

	private final String INPUT_FILE = "overflow32";

	@Test
	public void testStringPrefixAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/strings/prefix";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		// added checker to the analysis
		conf.semanticChecks.add(new HTTPStringChecker<>());

		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}

	@Test
	public void testStringSuffixAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/strings/suffix";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		// added checker to the analysis
		conf.semanticChecks.add(new DotComStringChecker<>());
		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}


	@Test
	public void testDivByZeroIntervalAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/divide_by_zero";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());

		// added checker to the analysis
		conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}

	@Test
	public void testDivByZeroPentagonAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/divide_by_zero_PENT";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());

		// added checker to the analysis
		conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);


		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}

	@Test
	public void testOverflowInterval8bitsAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/overflow/8bit";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());

		// added checker to the analysis
		conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE)); // checks overflow for integer 8 bits

		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}

	@Test
	public void testOverflowInterval16bitsAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/overflow/16bit";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		// added checker to the analysis
		conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE)); // checks overflow for integer 16 bits

		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}

	@Test
	public void testOverflowInterval32bitsAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/overflow/32bit";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		// added checker to the analysis
		conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE)); // checks overflow for integer 32bits

		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}

	String[] nameSource = {"source", "source1", "getUserInput", "GetRequest", "getExternalRequest", "getInternalToken", "GetRequestUnsafe", "source_int", "source_arr", "sourceSerializedObject", "readSecretFile", "user_input", "input"};
	String[] nameSanitizers = {"sanitizer", "sanitizer1", "sanitizeInput", "escapeHtml", "basicSanitize", "advancedEncrypt", "strip1", "strip2", "sanitize", "sanitizeParam", "sanitized_value", "fix_pass"};
	String[] nameSinks = {"dbQuery", "storeAsFile", "sink1", "runQueryDB", "renderHtml", "sendEmail", "db_execute", "log_to_public_file", "system", "runQueryDB", "writeSession", "logAudit", "sink", "deserializePathJob", "writeToDatabase", "sendToExternalServer", "executeSystemCommand", "evaluate", "read_pass", "readFile"};


	@Test
	public void testThreeTaintAnalysis() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/SCSR2026Programs/" + INPUT_FILE + ".imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/SCSR2026Programs/" + INPUT_FILE + "/taint";

		// we specify the visual format of the analysis results
		//conf.outputs.add(new HtmlInputs(true));
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
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

		// added checker to the analysis
		conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
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
