package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.Taint;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

public class StudentProgramTest {

    // Base path for student programs
    private static final String BASE_INPUT  =
            "inputs/student-programs/SCSR2026Programs/SCSR2026Programs/";
    private static final String BASE_OUTPUT =
            "outputs/student-programs/";

    // Source / sanitizer / sink names used by taint test harness
    private static final String[] SOURCES    = {"source1", "GetRequest", "getExternalRequest", "getInternalToken", "GetRequestUnsafe", "getUserInput"};
    private static final String[] SANITIZERS = {"sanitizer1", "basicSanitize", "advancedEncrypt", "sanitizeParam"};
    private static final String[] SINKS      = {"sink1", "runQueryDB", "db_execute", "log_to_public_file", "writeSession", "logAudit", "executeSystemCommand"};

    // ----------------------------------------------------------------
    // 1. 872966_Overflow.imp  ->  OverflowIntervalChecker (8/16/32-bit)
    // ----------------------------------------------------------------
    @Test
    public void analyze_872966_Overflow() throws ParsingException, AnalysisException {
        String name = "872966_Overflow";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");

        runOverflow(program, BASE_OUTPUT + name + "/overflow-8bit",
                Byte.MIN_VALUE, Byte.MAX_VALUE);
        runOverflow(program, BASE_OUTPUT + name + "/overflow-16bit",
                Short.MIN_VALUE, Short.MAX_VALUE);
        runOverflow(program, BASE_OUTPUT + name + "/overflow-32bit",
                Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    // ----------------------------------------------------------------
    // 2. 872966_TaintThreeLevels.imp  ->  TaintThreeLevelsChecker
    // ----------------------------------------------------------------
    @Test
    public void analyze_872966_TaintThreeLevels() throws ParsingException, AnalysisException {
        String name = "872966_TaintThreeLevels";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");
        runTaint(program, BASE_OUTPUT + name);
    }

    // ----------------------------------------------------------------
    // 3. 876957-div_by_zero.imp  ->  DivByZeroIntervalChecker
    // ----------------------------------------------------------------
    @Test
    public void analyze_876957_divByZero() throws ParsingException, AnalysisException {
        String name = "876957-div_by_zero";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");
        runDivZeroInterval(program, BASE_OUTPUT + name + "/divzero-interval");
    }

    // ----------------------------------------------------------------
    // 4. 876957-strings.imp  ->  HTTPStringChecker + DotComStringChecker
    // ----------------------------------------------------------------
    @Test
    public void analyze_876957_strings() throws ParsingException, AnalysisException {
        String name = "876957-strings";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");
        runHTTP(program,   BASE_OUTPUT + name + "/prefix");
        runDotCom(program, BASE_OUTPUT + name + "/suffix");
    }

    // ----------------------------------------------------------------
    // 5. 881299_overunderflow.imp  ->  OverflowIntervalChecker (8/16/32-bit)
    // ----------------------------------------------------------------
    @Test
    public void analyze_881299_overunderflow() throws ParsingException, AnalysisException {
        String name = "881299_overunderflow";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");

        runOverflow(program, BASE_OUTPUT + name + "/overflow-8bit",
                Byte.MIN_VALUE, Byte.MAX_VALUE);
        runOverflow(program, BASE_OUTPUT + name + "/overflow-16bit",
                Short.MIN_VALUE, Short.MAX_VALUE);
        runOverflow(program, BASE_OUTPUT + name + "/overflow-32bit",
                Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    // ----------------------------------------------------------------
    // 6. 881299_taint.imp  ->  TaintThreeLevelsChecker
    // ----------------------------------------------------------------
    @Test
    public void analyze_881299_taint() throws ParsingException, AnalysisException {
        String name = "881299_taint";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");
        runTaint(program, BASE_OUTPUT + name);
    }

    // ----------------------------------------------------------------
    // 7. 894069_overflow.imp  ->  OverflowIntervalChecker (8/16/32-bit)
    // ----------------------------------------------------------------
    @Test
    public void analyze_894069_overflow() throws ParsingException, AnalysisException {
        String name = "894069_overflow";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");

        runOverflow(program, BASE_OUTPUT + name + "/overflow-8bit",
                Byte.MIN_VALUE, Byte.MAX_VALUE);
        runOverflow(program, BASE_OUTPUT + name + "/overflow-16bit",
                Short.MIN_VALUE, Short.MAX_VALUE);
        runOverflow(program, BASE_OUTPUT + name + "/overflow-32bit",
                Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    // ----------------------------------------------------------------
    // 8. 894069_prefix_suffix.imp  ->  HTTPStringChecker + DotComStringChecker
    // ----------------------------------------------------------------
    @Test
    public void analyze_894069_prefixSuffix() throws ParsingException, AnalysisException {
        String name = "894069_prefix_suffix";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");
        runHTTP(program,   BASE_OUTPUT + name + "/prefix");
        runDotCom(program, BASE_OUTPUT + name + "/suffix");
    }
    // ----------------------------------------------------------------
    // 9. 1003406_three_taint_1.imp  ->  TaintThreeLevelsChecker
    // ----------------------------------------------------------------
    @Test
    public void analyze_1003406_threeTaint1() throws ParsingException, AnalysisException {
        String name = "1003406_three_taint_1";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");
        runTaint(program, BASE_OUTPUT + name);
    }

    // ----------------------------------------------------------------
    // 10. 903942_div_by_zero.imp  ->  DivByZeroIntervalChecker
    // ----------------------------------------------------------------
    @Test
    public void analyze_903942_divByZero() throws ParsingException, AnalysisException {
        String name = "903942_div_by_zero";
        Program program = IMPFrontend.processFile(BASE_INPUT + name + ".imp");
        runDivZeroInterval(program, BASE_OUTPUT + name + "/divzero-interval");
    }

    // ================================================================
    // Shared runner methods
    // ================================================================

    private void runOverflow(Program program, String workdir, int min, int max)
            throws AnalysisException {
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = workdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalChecker<>(min, max));
        new LiSA(conf).run(program);
    }

    private void runDivZeroInterval(Program program, String workdir)
            throws AnalysisException {
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = workdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        new LiSA(conf).run(program);
    }

    private void runTaint(Program program, String workdir)
            throws AnalysisException {
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = workdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        for (CFG cfg : program.getAllCFGs()) {
            String n = cfg.getDescriptor().getName();
            if (isSource(n))
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if (isSanitizer(n))
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if (isSink(n))
                cfg.getDescriptor().addAnnotation(Taint.SINK_ANNOTATION);
        }
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        new LiSA(conf).run(program);
    }

    private void runHTTP(Program program, String workdir)
            throws AnalysisException {
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = workdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        new LiSA(conf).run(program);
    }

    private void runDotCom(Program program, String workdir)
            throws AnalysisException {
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = workdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DotComStringChecker<>());
        new LiSA(conf).run(program);
    }

    // ================================================================
    // Annotation helpers
    // ================================================================

    private boolean isSource(String name) {
        for (String s : SOURCES)    if (s.equals(name)) return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for (String s : SANITIZERS) if (s.equals(name)) return true;
        return false;
    }

    private boolean isSink(String name) {
        for (String s : SINKS)      if (s.equals(name)) return true;
        return false;
    }
}
