package it.unive.scsr.student_test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.AnalysisExecutionException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.DivByZeroPentagonChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.unive.lisa.DefaultConfiguration.*;

public class NumericalAnalysesTest {

	
    @Test
    public void testDivByZeroIntervalAnalysis() throws ParsingException, AnalysisException {
        List<String> files = Arrays.asList("880119-890558-divbyzero.imp", "903942_div_by_zero.imp");
        // we parse the program to get the CFG representation of the code in it

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>(); // functions calling other functions
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        for( String filename: files){
            String dir = filename.replaceAll(".imp$", "");
            Program program = IMPFrontend.processFile("inputs/student_programs/divbyzero/interval/" + filename);
            conf.workdir = "outputs/student_programs/divbyzero/interval/" + dir;
            LiSA lisa = new LiSA(conf);
            lisa.run(program);
        }
    }

    @Test
    public void testDivByZeroPentagonAnalysis() throws ParsingException, AnalysisException {
        List<String> files = Arrays.asList("895227_897270_div_by_zero_pentagon_1.imp", "895227_897270_div_by_zero_pentagon_2.imp",
                "895227_897270_div_by_zero_pentagon_3.imp", "896827-895879-divbyzero_pentagon.imp",
                "1003406_pentagon_divbyzero-1.imp", "1003406_pentagon_divbyzero-2.imp", "1003406_pentagon_divbyzero-3.imp"

                );

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>(); // functions calling other functions
        conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
        conf.outputs.add(new JSONReportDumper());

        for(String f : files){
            Program program = IMPFrontend.processFile("inputs/student_programs/divbyzero/pentagon/" + f);
            String dir = f.replaceAll(".imp$", "");
            conf.workdir = "outputs/student_programs/divbyzero/pentagon/" + dir;
            LiSA lisa = new LiSA(conf);
            try{
                lisa.run(program);
            } catch (AnalysisException ae){
                System.err.println("Analysis exception caused by " + f);
            }
        }
    }
    
    @Test
    public void testOverflowInterval8bitsAnalysis() throws ParsingException, AnalysisException {
        List<String> files = Arrays.asList(
            "903942_overflow.imp",
                "896827-895879-overflow.imp"
        );
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>(); // functions calling other functions
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE)); // checks overflow for integer 8 bits
        conf.outputs.add(new JSONReportDumper());
        for(String f : files){
            String dir = f.replaceAll(".imp$", "");
            Program program = IMPFrontend.processFile("inputs/student_programs/overflow/" + f);
            conf.workdir = "outputs/student_programs/overflow8/" + dir;
            LiSA lisa = new LiSA(conf);
            lisa.run(program);
        }
    }
    
    @Test
    public void testOverflowInterval16bitsAnalysis() throws ParsingException, AnalysisException {
        List<String> files = Arrays.asList(
                "903942_overflow.imp",
                "896827-895879-overflow.imp"
        );
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>(); // functions calling other functions
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE)); // checks overflow for integer 8 bits
        conf.outputs.add(new JSONReportDumper());
        for(String f : files){
            String dir = f.replaceAll(".imp$", "");
            Program program = IMPFrontend.processFile("inputs/student_programs/overflow/" + f);
            conf.workdir = "outputs/student_programs/overflow16/" + dir;
            LiSA lisa = new LiSA(conf);
            lisa.run(program);
        }
    }
    
    @Test
    public void testOverflowInterval32bitsAnalysis() throws ParsingException, AnalysisException {
        List<String> files = Arrays.asList(
                "903942_overflow.imp",
                "896827-895879-overflow.imp"
        );
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>(); // functions calling other functions
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE)); // checks overflow for integer 8 bits
        conf.outputs.add(new JSONReportDumper());
        for(String f : files){
            String dir = f.replaceAll(".imp$", "");
            Program program = IMPFrontend.processFile("inputs/student_programs/overflow/" + f);
            conf.workdir = "outputs/student_programs/overflow32/" + dir;
            LiSA lisa = new LiSA(conf);
            lisa.run(program);
        }
    }
}
