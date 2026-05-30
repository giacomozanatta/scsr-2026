package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
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
import it.unive.scsr.checkers.*;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class AllAnalysisTest {

    String[] nameSource = {"readSecretFile","getUserInput","GetRequest"};
    String[] nameSanitizers = {"sanitizeInput","escapeHtml"};
    String[] nameSinks = {"sendToExternalServer", "executeSystemCommand","runQueryDB","renderHtml","sendEmail"};
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

    public void testDivByZero(String input_name) throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checked_programs/" + input_name + ".imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checked_programs/" + input_name + "/divbyzero_interval";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // added checker to the analysis
        conf.semanticChecks.add(new DivByZeroIntervalChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    public void testDivByZeroPentagon(String input_name) throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checked_programs/" + input_name + ".imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checked_programs/" + input_name + "/divbyzero_pentagon";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // added checker to the analysis
        conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    public void testDotCom(String input_name) throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checked_programs/" + input_name + ".imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checked_programs/" + input_name + "/dotcom";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // added checker to the analysis
        conf.semanticChecks.add(new DotComStringChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    public void testHTTP(String input_name) throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checked_programs/" + input_name + ".imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checked_programs/" + input_name + "/http";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        // added checker to the analysis
        conf.semanticChecks.add(new HTTPStringChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    public void testOverflow(String input_name, int min, int max) throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checked_programs/" + input_name + ".imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checked_programs/" + input_name + "/overflow_interval_"+min+"_"+max;

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // added checker to the analysis
        conf.semanticChecks.add(new OverflowIntervalChecker<>(min,max));
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    public void testOverflowPentagon(String input_name, int min, int max) throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checked_programs/" + input_name + ".imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checked_programs/" + input_name + "/overflow_pentagon_"+min+"_"+max;

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
        conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // added checker to the analysis
        conf.semanticChecks.add(new OverflowPentagonChecker<>(min,max));
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    public void testTaint(String input_name) throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/checked_programs/" + input_name + ".imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/checked_programs/" + input_name + "/taintthreelevels";

        // we specify the visual format of the analysis results
        //conf.outputs.add(new HtmlInputs(true));
        conf.outputs.add(new HtmlResults<>(true));
        // we specify the analysis that we want to execute
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

        // added checker to the analysis
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        conf.outputs.add(new JSONReportDumper());

        // we instantiate LiSA with our configuration
        LiSA lisa = new LiSA(conf);


        // finally, we tell LiSA to analyze the program
        lisa.run(program);
    }

    @Test
    public void testAllAnalysis() throws AnalysisException {
        String[] programs = {"881299_divbyzero",
                             "881299_presuffix",
                             "894069_taint",
                             "894579_896954_divisionbyzero_1-2",
                             "895227_897270_div_by_zero_pentagon_3",
                             "895227_897270_taint_three_level_2",
                             "895227_897270_taint_three_level_3",
                             "896827-895879-divbyzero",
                             "904329_overflowunderflow_2",
                             "904329_overflowunderflow_3"};
        for (String p : programs) {
            try {
                testDivByZero(p);
            } catch (Exception e) {
                System.out.println(p + " --------- divByZeroInterval -----------" + e);
            }
            try {
                testDivByZeroPentagon(p);
            } catch (Exception e) {
                System.out.println(p + " --------- divByZeroPentagon -----------" + e);
            }
            try{
                testDotCom(p);
            } catch (Exception e) {
                System.out.println(p + " --------- dotCom -----------" + e);
            }
            try {
                testHTTP(p);
            } catch (Exception e) {
                System.out.println(p + " --------- HTTP -----------" + e);
            }
            try {
                testOverflow(p, Byte.MIN_VALUE, Byte.MAX_VALUE);
            } catch (Exception e) {
                System.out.println(p + " --------- overflowIntervalB -----------" + e);
            }
            try {
                testOverflow(p, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } catch (Exception e) {
                System.out.println(p + " --------- overflowIntervalB -----------" + e);
            }
            try {
                testOverflowPentagon(p, Byte.MIN_VALUE, Byte.MAX_VALUE);
            } catch (Exception e) {
                System.out.println(p + " --------- overflowPentagonI -----------" + e);
            }
            try {
                testOverflowPentagon(p, Integer.MIN_VALUE, Integer.MAX_VALUE);
            } catch (Exception e) {
                System.out.println(p + " --------- overflowPentagonI -----------" + e);
            }
            try {
                testTaint(p);
            } catch (Exception e) {
                System.out.println(p + " --------- taint -----------" + e);
            }
        }
    }
}
