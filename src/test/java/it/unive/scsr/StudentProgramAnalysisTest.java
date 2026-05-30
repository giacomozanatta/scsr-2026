package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;

public class StudentProgramAnalysisTest {

	private static final String[] OVERFLOW_PROGRAMS = {
			"inputs/student_programs/overflow/872966_Overflow.imp",
			"inputs/student_programs/overflow/876957-overflow.imp",
			"inputs/student_programs/overflow/overflow_01.imp",
			"inputs/student_programs/overflow/overflow_02.imp",
			"inputs/student_programs/overflow/overflow_03.imp",
			"inputs/student_programs/overflow/overflow_04.imp",
			"inputs/student_programs/overflow/overflow_05.imp",
			"inputs/student_programs/overflow/overflow_06.imp",
			"inputs/student_programs/overflow/overflow_07.imp",
			"inputs/student_programs/overflow/overflow_08.imp",
	};

	private static final String[] DIV_BY_ZERO_PROGRAMS = {
			"inputs/student_programs/divbyzero/894579_896954_divisionbyzero_1-2.imp",
			"inputs/student_programs/divbyzero/895227_897270_div_by_zero_interval_1.imp",
			"inputs/student_programs/divbyzero/divbyzero_01.imp",
			"inputs/student_programs/divbyzero/divbyzero_02.imp",
			"inputs/student_programs/divbyzero/divbyzero_03.imp",
			"inputs/student_programs/divbyzero/divbyzero_04.imp",
			"inputs/student_programs/divbyzero/divbyzero_05.imp",
			"inputs/student_programs/divbyzero/divbyzero_06.imp",
			"inputs/student_programs/divbyzero/divbyzero_07.imp",
			"inputs/student_programs/divbyzero/divbyzero_08.imp",
	};

	private static final String[] PREFIX_PROGRAMS = {
			"inputs/student_programs/prefixsuffix/894579_896954_prefixsuffix_1.imp",
			"inputs/student_programs/prefixsuffix/894579_896954_prefixsuffix_2.imp",
			"inputs/student_programs/prefixsuffix/prefix_01.imp",
			"inputs/student_programs/prefixsuffix/prefix_02.imp",
			"inputs/student_programs/prefixsuffix/prefix_03.imp",
			"inputs/student_programs/prefixsuffix/prefix_04.imp",
			"inputs/student_programs/prefixsuffix/prefix_05.imp",
			"inputs/student_programs/prefixsuffix/prefix_06.imp",
			"inputs/student_programs/prefixsuffix/prefix_07.imp",
			"inputs/student_programs/prefixsuffix/prefix_08.imp",
	};

	private static final String[] TAINT_PROGRAMS = {
			"inputs/student_programs/taint/895227_897270_taint_three_level_1.imp",
			"inputs/student_programs/taint/895227_897270_taint_three_level_2.imp",
			"inputs/student_programs/taint/895227_897270_taint_three_level_3.imp",
			"inputs/student_programs/taint/taint_01.imp",
			"inputs/student_programs/taint/taint_02.imp",
			"inputs/student_programs/taint/taint_03.imp",
			"inputs/student_programs/taint/taint_04.imp",
			"inputs/student_programs/taint/taint_05.imp",
			"inputs/student_programs/taint/taint_06.imp",
			"inputs/student_programs/taint/taint_07.imp",
			"inputs/student_programs/taint/taint_08.imp",
	};

	@Test
	public void testStudentOverflowPrograms() throws ParsingException, AnalysisException {
		for (int i = 0; i < OVERFLOW_PROGRAMS.length; i++)
			runOverflowAnalysis(OVERFLOW_PROGRAMS[i], outputName("student-overflow", i, OVERFLOW_PROGRAMS[i]));
	}

	@Test
	public void testStudentDivByZeroPrograms() throws ParsingException, AnalysisException {
		for (int i = 0; i < DIV_BY_ZERO_PROGRAMS.length; i++)
			runDivByZeroAnalysis(DIV_BY_ZERO_PROGRAMS[i], outputName("student-divbyzero", i, DIV_BY_ZERO_PROGRAMS[i]));
	}

	@Test
	public void testStudentPrefixSuffixPrograms() throws ParsingException, AnalysisException {
		for (int i = 0; i < PREFIX_PROGRAMS.length; i++)
			runPrefixAnalysis(PREFIX_PROGRAMS[i], outputName("student-prefix", i, PREFIX_PROGRAMS[i]));
	}

	@Test
	public void testStudentTaintPrograms() throws ParsingException, AnalysisException {
		for (int i = 0; i < TAINT_PROGRAMS.length; i++)
			runTaintAnalysis(TAINT_PROGRAMS[i], outputName("student-taint", i, TAINT_PROGRAMS[i]));
	}

	private static String outputName(String prefix, int index, String file) {
		String base = file.substring(file.lastIndexOf('/') + 1).replace(".imp", "");
		return prefix + "-" + index + "-" + base;
	}

	private void runOverflowAnalysis(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/student-analysis/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new Interval(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());

		new LiSA(conf).run(program);
	}

	private void runDivByZeroAnalysis(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/student-analysis/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new Interval(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());

		new LiSA(conf).run(program);
	}

	private void runPrefixAnalysis(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/student-analysis/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new Prefix(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new HTTPStringChecker<>());
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());

		new LiSA(conf).run(program);
	}

	private void runTaintAnalysis(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);
		annotateTaintMethods(program);

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/student-analysis/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());

		new LiSA(conf).run(program);
	}

	private void annotateTaintMethods(Program program) {
		for (it.unive.lisa.program.cfg.CFG cfg : program.getAllCFGs()) {
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
		return lower.contains("source") || lower.contains("input") || lower.contains("read")
				|| lower.startsWith("get");
	}

	private boolean isSanitizer(String name) {
		return name.toLowerCase().contains("sanitize");
	}

	private boolean isSink(String name) {
		String lower = name.toLowerCase();
		return lower.contains("sink") || lower.contains("execute") || lower.contains("write")
				|| lower.contains("output") || lower.equals("process");
	}
}