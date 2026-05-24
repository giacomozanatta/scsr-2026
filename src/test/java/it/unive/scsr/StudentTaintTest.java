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

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import static it.unive.lisa.DefaultConfiguration.*;

public class StudentTaintTest {

    private static final String INPUT_DIR   = "inputs/students/taint";
    private static final String OUTPUT_BASE = "outputs/students/taint";

    private static final Set<String> SOURCES    = Set.of("GetRequest", "getUserInput");
    private static final Set<String> SANITIZERS = Set.of("sanitizeInput", "escapeHtml");
    private static final Set<String> SINKS      = Set.of("runQueryDB", "renderHtml", "sendEmail");

    private File[] getStudentFiles() {
        File dir = new File(INPUT_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".imp"));
        if (files == null) files = new File[0];
        Arrays.sort(files);
        return files;
    }

    @Test
    public void testTaintSink() throws ParsingException, AnalysisException {
        for (File f : getStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());

            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name;
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
