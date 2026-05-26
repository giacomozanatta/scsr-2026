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
import it.unive.scsr.analysis.extended_sign.ExtendedSign;
import it.unive.scsr.analysis.sign.NonNegativeSpeedInMoveForwardChecker;
import it.unive.scsr.analysis.taint.Taint;
import it.unive.scsr.analysis.taint.TaintChecker;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.DivByZeroPentagonChecker;
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ColleaguesProgramsAnalysesTest {
    // Fields for TaintThreeLevels analysis
    String[] nameSource = {"source1", "GetRequest", "getExternalRequest", "getInternalToken"};
	String[] nameSanitizers = {"sanitizer1", "basicSanitize", "advancedEncrypt"};
	String[] nameSinks = {"sink1", "runQueryDB", "db_execute", "log_to_public_file"};

    // Fields for sig

    // Tests for 876957-div_by_zero.imp
    @Test
    public void testDivByZeroIntervalAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-div_by_zero.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/div-by-zero/876957-div_by_zero";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void testDivByZeroPentagonAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-div_by_zero.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/div-by-zero-pentagon/876957-div_by_zero";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 876957-overflow.imp
    @Test
    public void testOverflowInterval8bitsAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-overflow.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-8bits/876957-overflow";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE)); // checks overflow for integer 8 bits
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval16bitsAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-overflow.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-16bits/876957-overflow";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE)); 
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval32bitsAnalysis1() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-overflow.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-32bits/876957-overflow";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE)); // checks overflow for integer 32bits
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 876957-strings.imp
    @Test
    public void testStringPrefixAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-strings.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/strings/prefix/876957-strings";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void testStringSuffixAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-strings.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/strings/suffix/876957-strings";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DotComStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 876957-taint.imp
    @Test
    public void testTaintAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/876957-taint.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/taint/876957-taint";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(new PointBasedHeap(), new Taint(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        for(CFG cfg : program.getAllCFGs()) {
        	String name = cfg.getDescriptor().getName();
        	if(isSource(name))
        		cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
        	else if(isSanitizer(name))
        		cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
        	else if(isSink(name))
        		cfg.getDescriptor().addAnnotation(Taint.SINK_ANNOTATION);
        }

        conf.semanticChecks.add(new TaintChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 894579_896954_taintthreelevel_1-2.imp
    @Test
    public void testTaintThreeLevelsAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/894579_896954_taintthreelevel_1-2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/taint_three_levels/894579_896954_taintthreelevel_1-2";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new TaintThreeLevels(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        for(CFG cfg : program.getAllCFGs()) {
			String name = cfg.getDescriptor().getName();
			if(isSource(name))
				cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
			else if(isSanitizer(name))
				cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
			else if(isSink(name))
				cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
        }
    
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    // Tests for 895227_897270_dot_com_string_1.imp
    @Test
    public void testStringPrefixAnalysis2() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/895227_897270_dot_com_string_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/strings/prefix/895227_897270_dot_com_string_1";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void testStringSuffixAnalysis2() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/895227_897270_dot_com_string_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/strings/suffix/895227_897270_dot_com_string_1";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DotComStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 895227_897270_overflow_interval_1.imp
    @Test
    public void testOverflowInterval8bitsAnalysis2() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/895227_897270_overflow_interval_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-8bits/895227_897270_overflow_interval_1";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE)); // checks overflow for integer 8 bits
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval16bitsAnalysis2() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/895227_897270_overflow_interval_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-16bits/895227_897270_overflow_interval_1";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE)); 
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval32bitsAnalysis2() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/colleagues/895227_897270_overflow_interval_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-32bits/895227_897270_overflow_interval_1";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE)); // checks overflow for integer 32bits
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 903942_extendedsign_taint.imp
    @Test
    public void testExtendedSignAnalysis1() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/903942_extendedsign_taint.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/extended_sign/903942_extendedsign_taint";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new ExtendedSign(), defaultTypeDomain());
        conf.semanticChecks.add(new NonNegativeSpeedInMoveForwardChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 903942_overflow.imp
    @Test
    public void testOverflowInterval8bitsAnalysis3() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/903942_overflow.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-8bits/903942_overflow";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE)); // checks overflow for integer 8 bits
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval16bitsAnalysis3() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/903942_overflow.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-16bits/903942_overflow";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE)); 
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval32bitsAnalysis3() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/colleagues/903942_overflow.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/overflow/interval-32bits/903942_overflow";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE)); // checks overflow for integer 32bits
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Tests for 903942_strings.imp
    @Test
    public void testStringPrefixAnalysis3() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/903942_strings.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/strings/prefix/903942_strings";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void testStringSuffixAnalysis3() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/colleagues/903942_strings.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/colleagues/strings/suffix/903942_strings";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DotComStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }


    // Helpers
    private boolean isSource(String name) {
		for(String src : nameSource)
			if(src.equals(name))
				return true;
		return false;
	}
	
	private boolean isSanitizer(String name) {
		for(String sanit : nameSanitizers)
			if(sanit.equals(name))
				return true;
		return false;
	}
	
	private boolean isSink(String name) {
		for(String sink : nameSinks)
			if(sink.equals(name))
				return true;
		return false;
	}

}
