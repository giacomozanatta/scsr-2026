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
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.analysis.extendedsign.base.ExtendedSign;
import it.unive.scsr.checkers.*;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static it.unive.lisa.DefaultConfiguration.*;

public class StudentCombinedTest {

    private static final String OUTPUT_BASE = "outputs/students/combined";

    private static final Set<String> SOURCES    = Set.of("GetRequest", "getUserInput", "getExternalRequest");
    private static final Set<String> SANITIZERS = Set.of("sanitizeInput", "escapeHtml", "sanitizeParam", "basicSanitize");
    private static final Set<String> SINKS      = Set.of("runQueryDB", "renderHtml", "sendEmail", "executeSystemCommand");

    private File[] getAllStudentFiles() {
        List<File> all = new ArrayList<>();
        for (String dir : new String[]{"inputs/students/numerical", "inputs/students/strings", "inputs/students/taint"}) {
            File folder = new File(dir);
            File[] files = folder.listFiles((d, name) -> name.endsWith(".imp"));
            if (files != null) {
                all.addAll(Arrays.asList(files));
            }
        }
        all.sort(java.util.Comparator.comparing(File::getName));
        return all.toArray(new File[0]);
    }

    @Test
    public void testDivByZeroExtendedSign() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/divbyzero_extendedsign";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new ExtendedSign(), defaultTypeDomain());
            conf.semanticChecks.add(new DivByZeroExtendedSignChecker<>());
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testDivByZeroExtendedSignPentagon() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/divbyzero_extendedsign_pentagon";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
            conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testOverflow8bit() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/overflow_8bit";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE));
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testOverflow16bit() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/overflow_16bit";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE));
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testOverflow32bit() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/overflow_32bit";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testHTTPStringChecker() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/http";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new HTTPStringChecker<>());
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testDotComStringChecker() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/dotcom";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new DotComStringChecker<>());
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testTaintSink() throws ParsingException, AnalysisException {
        for (File f : getAllStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/taint";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            for (CFG cfg : program.getAllCFGs()) {
                String methodName = cfg.getDescriptor().getName();
                if (SOURCES.contains(methodName)) {
                    cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
                } else if (SANITIZERS.contains(methodName)) {
                    cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
                } else if (SINKS.contains(methodName)) {
                    cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
                }
            }
            conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
            new LiSA(conf).run(program);
        }
    }
}
