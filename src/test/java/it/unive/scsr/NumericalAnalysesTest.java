package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.DivByZeroPentagonChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import it.unive.scsr.checkers.OverflowPentagonChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class NumericalAnalysesTest {
	
	
    @Test
    public void testDivByZeroIntervalAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/div-zero.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/div-zero";

        // we specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());

        // added checker to the analysis
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder 
        conf.outputs.add(new JSONReportDumper());
        
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);
        
        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    @Test
    public void testDivByZeroPentagonAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/div-zero.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/div-zero-pentagon";

        // we specify the visual format of the analysis results
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());

        // added checker to the analysis
        conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder 
        conf.outputs.add(new JSONReportDumper());
        
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        
        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }
    
    @Test
    public void testOverflowPentagon8bitsAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/overflow.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/overflow-pentagon";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowPentagonChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    @Test
    public void testOverflowInterval8bitsAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/overflow.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/overflow";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
    
        // added checker to the analysis
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE)); // checks overflow for integer 8 bits
        
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder 
        conf.outputs.add(new JSONReportDumper());
        
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval16bitsAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/overflow.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/overflow";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        // added checker to the analysis
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE)); // checks overflow for integer 16 bits
        
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder 
        conf.outputs.add(new JSONReportDumper());
        
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }
    
    @Test
    public void testOverflowInterval32bitsAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/overflow.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/overflow";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        // added checker to the analysis
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE)); // checks overflow for integer 32bits
        
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder 
        conf.outputs.add(new JSONReportDumper());
        
        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);

        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    @Test
    public void testDivByZeroLoanCalculator() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/div-zero-2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/div-zero-2";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testDivByZeroTrafficMonitor() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/div-zero-3.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/div-zero-3";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testOverflowGameScore() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/overflow-2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/overflow-2";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testOverflowPacketCounter() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/overflow-3.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/overflow-3";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testDivByZeroECommerce() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_div_by_zero_interval_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/divzero-ecommerce";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testDivByZeroPagination() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_div_by_zero_interval_2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/divzero-pagination";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testDivByZeroThermalSensor() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_div_by_zero_interval_3.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/divzero-thermal";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testOverflowCryptoMiner() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_overflow_interval_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/overflow-crypto";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testOverflowSmartContract() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_overflow_interval_2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/overflow-smartcontract";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testOverflowSubmarineNavigation() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_overflow_interval_3.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/overflow-submarine";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }
}
