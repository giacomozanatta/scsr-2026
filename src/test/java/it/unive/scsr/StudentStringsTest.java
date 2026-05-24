package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static it.unive.lisa.DefaultConfiguration.*;

public class StudentStringsTest {

    private static final String INPUT_DIR  = "inputs/students/strings";
    private static final String OUTPUT_BASE = "outputs/students/strings";

    private File[] getStudentFiles() {
        File dir = new File(INPUT_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".imp"));
        if (files == null) files = new File[0];
        Arrays.sort(files);
        return files;
    }

    @Test
    public void testHTTPStringChecker() throws ParsingException, AnalysisException {
        for (File f : getStudentFiles()) {
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
        for (File f : getStudentFiles()) {
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
}
