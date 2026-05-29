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
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static it.unive.lisa.DefaultConfiguration.*;

public class OtherStudentProgramsTest {

    private static final String DIR = "inputs/other/";

    // =========================================================================
    // Helpers
    // =========================================================================

    private void runDivZero(String file, String outdir) throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile(DIR + file);
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/" + outdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    private void runOverflow(String file, String outdir) throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile(DIR + file);
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/" + outdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    private void runStrings(String file, String outdir) throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile(DIR + file);

        LiSAConfiguration confHttp = new DefaultConfiguration();
        confHttp.workdir = "outputs/other/" + outdir + "/http";
        confHttp.outputs.add(new HtmlResults<>(true));
        confHttp.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        confHttp.interproceduralAnalysis = new ContextBasedAnalysis<>();
        confHttp.semanticChecks.add(new HTTPStringChecker<>());
        confHttp.outputs.add(new JSONReportDumper());
        new LiSA(confHttp).run(program);

        LiSAConfiguration confDot = new DefaultConfiguration();
        confDot.workdir = "outputs/other/" + outdir + "/dotcom";
        confDot.outputs.add(new HtmlResults<>(true));
        confDot.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        confDot.interproceduralAnalysis = new ContextBasedAnalysis<>();
        confDot.semanticChecks.add(new DotComStringChecker<>());
        confDot.outputs.add(new JSONReportDumper());
        new LiSA(confDot).run(program);
    }

    private void runTaint(String file, String outdir,
            List<String> sources, List<String> sanitizers, List<String> sinks)
            throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile(DIR + file);
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/" + outdir;
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (sources.contains(name))    cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if (sanitizers.contains(name)) cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if (sinks.contains(name)) cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
        }
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    // =========================================================================
    // Division-by-zero — 7 other student programs
    // =========================================================================

    @Test
    public void testDivZeroPentagon1() throws ParsingException, AnalysisException {
        runDivZero("895227_897270_div_by_zero_pentagon_1.imp", "dz-pentagon1");
    }

    @Test
    public void testDivZeroPentagon2() throws ParsingException, AnalysisException {
        runDivZero("895227_897270_div_by_zero_pentagon_2.imp", "dz-pentagon2");
    }

    @Test
    public void testDivZeroPentagon3() throws ParsingException, AnalysisException {
        runDivZero("895227_897270_div_by_zero_pentagon_3.imp", "dz-pentagon3");
    }

    @Test
    public void testDivZeroRateLimiter() throws ParsingException, AnalysisException {
        runDivZero("896827-895879-divbyzero.imp", "dz-ratelimiter");
    }

    @Test
    public void testDivZeroGeneric() throws ParsingException, AnalysisException {
        runDivZero("divbyzero.imp", "dz-generic");
    }

    @Test
    public void testDivZeroPhysics() throws ParsingException, AnalysisException {
        runDivZero("881299_divbyzero.imp", "dz-physics");
    }

    @Test
    public void testDivZeroSensor() throws ParsingException, AnalysisException {
        runDivZero("894069_div_by_zero.imp", "dz-sensor");
    }

    // =========================================================================
    // Overflow — 7 other student programs
    // =========================================================================

    @Test
    public void testOverflowSensorProcessor() throws ParsingException, AnalysisException {
        runOverflow("872966_Overflow.imp", "ov-sensorprocessor");
    }

    @Test
    public void testOverflow876957() throws ParsingException, AnalysisException {
        runOverflow("876957-overflow.imp", "ov-876957");
    }

    @Test
    public void testOverflowBank881299() throws ParsingException, AnalysisException {
        runOverflow("881299_overunderflow.imp", "ov-bank881299");
    }

    @Test
    public void testOverflow894069() throws ParsingException, AnalysisException {
        runOverflow("894069_overflow.imp", "ov-894069");
    }

    @Test
    public void testOverflow894579() throws ParsingException, AnalysisException {
        runOverflow("894579_896954_overunderflow_1-2.imp", "ov-894579");
    }

    @Test
    public void testOverflowBankTemp() throws ParsingException, AnalysisException {
        runOverflow("896827-895879-overflow.imp", "ov-banktemp");
    }

    @Test
    public void testOverflow903942() throws ParsingException, AnalysisException {
        runOverflow("903942_overflow.imp", "ov-903942");
    }

    // =========================================================================
    // Strings (HTTP/DotCom) — 7 other student programs
    // =========================================================================

    @Test
    public void testStringsWebhook() throws ParsingException, AnalysisException {
        runStrings("895227_897270_http_string_3.imp", "str-webhook");
    }

    @Test
    public void testStringsEmailSystem() throws ParsingException, AnalysisException {
        runStrings("895227_897270_dot_com_string_2.imp", "str-email");
    }

    @Test
    public void testStringsDotCom3() throws ParsingException, AnalysisException {
        runStrings("895227_897270_dot_com_string_3.imp", "str-dotcom3");
    }

    @Test
    public void testStrings872966() throws ParsingException, AnalysisException {
        runStrings("872966_Strings.imp", "str-872966");
    }

    @Test
    public void testStrings881299() throws ParsingException, AnalysisException {
        runStrings("881299_presuffix.imp", "str-881299");
    }

    @Test
    public void testStrings894069() throws ParsingException, AnalysisException {
        runStrings("894069_prefix_suffix.imp", "str-894069");
    }

    @Test
    public void testStrings894579() throws ParsingException, AnalysisException {
        runStrings("894579_896954_prefixsuffix_1.imp", "str-894579");
    }

    // =========================================================================
    // Taint (three levels) — 7 other student programs
    // =========================================================================

    @Test
    public void testTaint872966() throws ParsingException, AnalysisException {
        runTaint("872966_TaintThreeLevels.imp", "taint-872966",
                Arrays.asList("getExternalRequest", "getInternalToken"),
                Arrays.asList("basicSanitize", "advancedEncrypt"),
                Arrays.asList("db_execute", "log_to_public_file"));
    }

    @Test
    public void testTaint876957() throws ParsingException, AnalysisException {
        runTaint("876957-taint.imp", "taint-876957",
                Arrays.asList("GetRequest", "getUserInput"),
                Arrays.asList("sanitizeInput", "escapeHtml"),
                Arrays.asList("runQueryDB", "renderHtml"));
    }

    @Test
    public void testTaint881299() throws ParsingException, AnalysisException {
        runTaint("881299_taint.imp", "taint-881299",
                Arrays.asList("GetRequest", "GetRequestUnsafe"),
                Arrays.asList("sanitize"),
                Arrays.asList("runQueryDB", "writeSession", "logAudit"));
    }

    @Test
    public void testTaint894069() throws ParsingException, AnalysisException {
        runTaint("894069_taint.imp", "taint-894069",
                Arrays.asList("getUserInput", "GetRequest"),
                Arrays.asList("sanitizeInput"),
                Arrays.asList("runQueryDB", "renderHtml", "sendEmail"));
    }

    @Test
    public void testTaint894579() throws ParsingException, AnalysisException {
        runTaint("894579_896954_taintthreelevel_1-2.imp", "taint-894579",
                Arrays.asList("sourceSerializedObject"),
                Arrays.asList("sanitizeParam"),
                Arrays.asList("readFile", "execute"));
    }

    @Test
    public void testTaint1003406_1() throws ParsingException, AnalysisException {
        runTaint("1003406_three_taint_1.imp", "taint-1003406-1",
                Arrays.asList("getExternalRequest", "getUserInput"),
                Arrays.asList("sanitizeParam", "basicSanitize"),
                Arrays.asList("executeSystemCommand"));
    }

    @Test
    public void testTaint1003406_2() throws ParsingException, AnalysisException {
        runTaint("1003406_three_taint_2.imp", "taint-1003406-2",
                Arrays.asList("GetRequest"),
                Arrays.asList("sanitizeInput"),
                Arrays.asList("logAudit"));
    }
}
