package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

import java.io.File;
import java.util.function.Consumer;

import org.junit.Test;

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
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class PeerAnalysisTest {

    private static final String PEER_DIR = "inputs/SCSR2026Programs/";
    private static final String OUT_DIR = "outputs/peer-analysis/";

    private static final String[] DIVBYZERO_FILES = {
            "876957-div_by_zero.imp",
            "880119-890558-divbyzero.imp",
            "881299_divbyzero.imp",
            "891184_divide_by_zero.imp",
            "894579_896954_divisionbyzero_1-2.imp",
            "895227_897270_div_by_zero_interval_1.imp",
            "896827-895879-divbyzero.imp",
            "903942_div_by_zero.imp",
            "904329_divbyzero_1.imp",
            "913849_div_by_zero.imp",
    };

    private static final String[] OVERFLOW_FILES = {
            "872966_Overflow.imp",
            "876957-overflow.imp",
            "880119-890558-overflow.imp",
            "881299_overunderflow.imp",
            "891184_overflow_check.imp",
            "894004_overflow.imp",
            "894579_896954_overunderflow_1-2.imp",
            "895227_897270_overflow_interval_1.imp",
            "903942_overflow.imp",
            "913849_overflow.imp",
    };

    private static final String[] TAINT_FILES = {
            "1003406_three_taint_1.imp",
            "872966_TaintThreeLevels.imp",
            "876957-taint.imp",
            "880119-890558-taint-three-levels.imp",
            "881299_taint.imp",
            "891184_taint_check.imp",
            "894004_taint.imp",
            "894579_896954_taintthreelevel_1-2.imp",
            "903942_extendedsign_three_taint.imp",
            "913849_threetaint.imp",
    };

    private static final Set<String> SOURCE_NAMES = new HashSet<>(Arrays.asList(
            "source", "source1", "getRequestParam", "readRawInput", "readUserCommand",
            "getUserInput", "GetRequest", "getInput", "getExternalRequest", "getInternalToken",
            "sourceSerializedObject", "getRawInput", "readInput"));

    private static final Set<String> SANITIZER_NAMES = new HashSet<>(Arrays.asList(
            "sanitize", "sanitizer", "sanitizer1", "sanitizeInput", "escapeHtml", "escape",
            "sanitizeParam", "sanitizeJob", "basicSanitize", "advancedEncrypt"));

    private static final Set<String> SINK_NAMES = new HashSet<>(Arrays.asList(
            "sink", "sink1", "runQueryDB", "renderHtml", "sendEmail", "executeCommand",
            "renderPage", "logAudit", "writeToDatabase", "dbQuery", "execute",
            "readFile", "executeSystemCommand", "storeAsFile", "storePicture",
            "db_execute", "log_to_public_file", "buildCommand"));

    private static final String[] STRING_FILES = {
            "872966_Strings.imp",
            "876957-strings.imp",
            "880119-890558-dotcom-strings.imp",
            "880119-890558-http-strings.imp",
            "881299_presuffix.imp",
            "891184_strings.imp",
            "894004_strings.imp",
            "894579_896954_prefixsuffix_1.imp",
            "895227_897270_dot_com_string_1.imp",
            "895227_897270_http_string_1.imp",
    };

    @Test
    public void peerDivByZero() {
        runBatch(DIVBYZERO_FILES, "divbyzero", conf -> {
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        });
    }

    @Test
    public void peerOverflow() {
        runBatch(OVERFLOW_FILES, "overflow", conf -> {
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        });
    }

    @Test
    public void peerStringsPrefix() {
        runBatch(STRING_FILES, "strings-prefix", conf -> {
            conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new HTTPStringChecker<>());
        });
    }

    @Test
    public void peerTaint() {
        for (String f : TAINT_FILES) {
            String name = f.replace(".imp", "");
            String workdir = OUT_DIR + "taint/" + name;
            new File(workdir).mkdirs();
            try {
                Program program = IMPFrontend.processFile(PEER_DIR + f);
                LiSAConfiguration conf = new DefaultConfiguration();
                conf.workdir = workdir;
                conf.outputs.add(new JSONReportDumper());
                conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

                for (CFG cfg : program.getAllCFGs()) {
                    String fname = cfg.getDescriptor().getName();
                    if (SOURCE_NAMES.contains(fname))
                        cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
                    else if (SANITIZER_NAMES.contains(fname))
                        cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
                    else if (SINK_NAMES.contains(fname))
                        cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
                }

                conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
                LiSA lisa = new LiSA(conf);
                lisa.run(program);
                System.out.println("[OK]    taint/" + f);
            } catch (Exception e) {
                System.err.println("[FAIL]  taint/" + f + " : " + e.getMessage());
            }
        }
    }

    @Test
    public void peerStringsSuffix() {
        runBatch(STRING_FILES, "strings-suffix", conf -> {
            conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new DotComStringChecker<>());
        });
    }

    private void runBatch(String[] files, String category, Consumer<LiSAConfiguration> setup) {
        int ok = 0, failed = 0;
        for (String f : files) {
            String name = f.replace(".imp", "");
            String workdir = OUT_DIR + category + "/" + name;
            new File(workdir).mkdirs();
            try {
                Program program = IMPFrontend.processFile(PEER_DIR + f);
                LiSAConfiguration conf = new DefaultConfiguration();
                conf.workdir = workdir;
                conf.outputs.add(new JSONReportDumper());
                setup.accept(conf);
                LiSA lisa = new LiSA(conf);
                lisa.run(program);
                ok++;
                System.out.println("[OK]    " + category + "/" + f);
            } catch (Exception e) {
                failed++;
                System.err.println("[FAIL]  " + category + "/" + f + " : " + e.getMessage());
            }
        }
        System.out.println("=== " + category + ": " + ok + " ok, " + failed + " failed ===");
    }
}
