package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.junit.Before;
import org.junit.Test;

public class TaintThreeLevelsTest {

    private static final String[] SOURCES = {"source1"};
    private static final String[] SANITIZERS = {"sanitizer1"};
    private static final String[] SINKS = {"sink1"};
    private static final String WORK_DIR = "outputs/taint-three-levels";
    private static final String INPUT_FILE = "inputs/taintthreelevels.imp";

    @Before
    public void setup() throws IOException {
        Path path = Paths.get(WORK_DIR);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    public void testTaintThreeLevelsAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile(INPUT_FILE);

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = WORK_DIR;

        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(
                defaultHeapDomain(),
                new TaintThreeLevels(),
                defaultTypeDomain()
        );

        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();

            if (isMatching(name, SOURCES)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            } else if (isMatching(name, SANITIZERS)) {
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            } else if (isMatching(name, SINKS)) {
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
            }
        }

        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

        new LiSA(conf).run(program);
    }

    private boolean isMatching(String name, String[] targets) {
        for (String target : targets) {
            if (target.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}