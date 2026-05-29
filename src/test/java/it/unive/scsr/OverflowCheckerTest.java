package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import org.junit.Test;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import it.unive.lisa.analysis.numeric.Interval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static it.unive.lisa.DefaultConfiguration.*;

public class OverflowCheckerTest {

    @Test
    public void testOverflowAnalysis() throws ParsingException, AnalysisException, IOException {
        List<Path> files = Files.walk(Path.of("inputs/otherprograms/overflow"))
            .filter(p -> p.toString().endsWith(".imp"))
            .collect(Collectors.toList());

        for (Path file : files) {
            Program program;
            try {
                program = IMPFrontend.processFile(file.toString());
            } catch (ParsingException e) {
                System.out.println("Skipping " + file.getFileName() + ": " + e.getMessage());
                continue;
            }
            try {
                LiSAConfiguration conf = new DefaultConfiguration();
                conf.workdir = "outputs/definitive_overflowanalysisdef/" + file.getFileName().toString().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());
                conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new OverflowIntervalChecker<>(0, 0));
                
                new LiSA(conf).run(program);
            } catch (AnalysisException e) {
                System.out.println("Analysis failed for " + file.getFileName() + ": " + e.getMessage());
            }
        }
    }
}