package it.unive.scsr.student_test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static it.unive.lisa.DefaultConfiguration.*;

public class StringAnalysesTest {
	
	
    @Test
    public void testStringPrefixAnalysis() throws ParsingException, AnalysisException {
        List<String> files = List.of("1003406_strings.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(new PointBasedHeap(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        for(String f : files){
            String dir = f.replaceAll(".imp$", "");
            Program program = IMPFrontend.processFile("inputs/student_programs/string/" + f);
            conf.workdir = "outputs/student_programs/strings/prefix/" + dir;
            LiSA lisa = new LiSA(conf);
            lisa.run(program);
        }
    }

    @Test
    public void testStringSuffixAnalysis() throws ParsingException, AnalysisException {
        List<String> files = List.of("1003406_strings.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(new PointBasedHeap(), new Suffix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DotComStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        for(String f : files) {
            String dir = f.replaceAll(".imp$", "");
            Program program = IMPFrontend.processFile("inputs/student_programs/string/" + f);
            conf.workdir = "outputs/student_programs/strings/suffix/" + dir;
            LiSA lisa = new LiSA(conf);
            lisa.run(program);
        }
    }
}
