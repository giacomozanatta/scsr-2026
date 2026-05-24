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
import it.unive.scsr.analysis.extendedinterval.ExtendedInterval;
import it.unive.scsr.analysis.taint.Taint;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.scsr.checkers.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static it.unive.lisa.DefaultConfiguration.*;

public class CheckersTest {

    //sources, sinks and sanitizers extracted from test programs
    String[] nameSource = {
            "source1",
            "GetRequest",
            "GetRequestUnsafe",
            "getUserInput",
            "getExternalRequest",
            "getInternalToken",
            "sourceSerializedObject",
            "readSecretFile"
    };

    String[] nameSanitizers = {
            "sanitizer1",
            "sanitize",
            "sanitizeInput",
            "escapeHtml",
            "basicSanitize",
            "advancedEncrypt",
            "sanitizeParam",
            "sanitizeJob"
    };

    String[] nameSinks = {
            "sink1",
            "runQueryDB",
            "writeSession",
            "logAudit",
            "renderHtml",
            "sendEmail",
            "db_execute",
            "log_to_public_file",
            "deserializePathJob",
            "readFile",
            "execute",
            "sendToExternalServer",
            "executeSystemCommand",
            "writeToDatabase",
            "sendToExternalServer"
    };

    @Test
    public void checkerAnalysis() throws ParsingException, AnalysisException {

        //this test will perform one analysis per checker, analyzing all the programs in the given folder
        //the outputs will be divided per checker

        //input paths
        String DivByZeroInputsPath = "inputs/for_checkers/divbyzero";
        String OverflowInputsPath = "inputs/for_checkers/overflow";
        String StringsInputsPath = "inputs/for_checkers/strings";
        String TaintInputsPath = "inputs/for_checkers/taint";

        //files lists
        File[] DivByZeroInputs = new File(DivByZeroInputsPath).listFiles();
        File[] OverflowInputs = new File(OverflowInputsPath).listFiles();
        File[] StringsInputs = new File(StringsInputsPath).listFiles();
        File[] TaintInputs = new File(TaintInputsPath).listFiles();

        // one lisa conf per checker
        LiSAConfiguration DivByZeroLISAConf = new DefaultConfiguration();
        LiSAConfiguration DivByZeroPentagonLISAConf = new DefaultConfiguration();
        LiSAConfiguration OverflowLISAConf = new DefaultConfiguration();
        LiSAConfiguration OverflowPentagonLISAConf = new DefaultConfiguration();
        LiSAConfiguration SuffixDotComLISAConf = new DefaultConfiguration();
        LiSAConfiguration PrefixHTTPLISAConf = new DefaultConfiguration();
        LiSAConfiguration TaintLISAConf = new DefaultConfiguration();

        // one output dir per checker
        String DivByZeroOutput = "outputs/checkers/divbyzero";
        String DivByZeroPentagonOutput = "outputs/checkers/divbyzeropent";
        String OverflowOutput = "outputs/checkers/overflow";
        String OverflowPentagonOutput = "outputs/checkers/overflowpent";
        String DotcomOutput = "outputs/checkers/dotcom";
        String HttpOutput = "outputs/checkers/http";
        String TaintOutput = "outputs/checkers/taint";

        // we specify the visual format of the analysis results
        DivByZeroLISAConf.outputs.add(new HtmlResults<>(true));
        DivByZeroPentagonLISAConf.outputs.add(new HtmlResults<>(true));
        OverflowLISAConf.outputs.add(new HtmlResults<>(true));
        OverflowPentagonLISAConf.outputs.add(new HtmlResults<>(true));
        SuffixDotComLISAConf.outputs.add(new HtmlResults<>(true));
        PrefixHTTPLISAConf.outputs.add(new HtmlResults<>(true));
        TaintLISAConf.outputs.add(new HtmlResults<>(true));

        // A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder
        DivByZeroLISAConf.outputs.add(new JSONReportDumper());
        DivByZeroPentagonLISAConf.outputs.add(new JSONReportDumper());
        OverflowLISAConf.outputs.add(new JSONReportDumper());
        OverflowPentagonLISAConf.outputs.add(new JSONReportDumper());
        SuffixDotComLISAConf.outputs.add(new JSONReportDumper());
        PrefixHTTPLISAConf.outputs.add(new JSONReportDumper());
        TaintLISAConf.outputs.add(new JSONReportDumper());

        // we specify the analysis that we want to execute for each checker
        DivByZeroLISAConf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        DivByZeroPentagonLISAConf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        OverflowLISAConf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
        OverflowPentagonLISAConf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
        PrefixHTTPLISAConf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        SuffixDotComLISAConf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        TaintLISAConf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());


        // all need interprocedural analysis
        DivByZeroLISAConf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        DivByZeroPentagonLISAConf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        OverflowLISAConf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        OverflowPentagonLISAConf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        SuffixDotComLISAConf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        PrefixHTTPLISAConf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        TaintLISAConf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        //we add checkers

        DivByZeroLISAConf.semanticChecks.add(new DivByZeroIntervalChecker<>());

        DivByZeroPentagonLISAConf.semanticChecks.add(new DivByZeroPentagonChecker<>());

        OverflowLISAConf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE));
        OverflowLISAConf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE));
        OverflowLISAConf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));

        OverflowPentagonLISAConf.semanticChecks.add(new OverflowPentagonChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE));
        OverflowPentagonLISAConf.semanticChecks.add(new OverflowPentagonChecker<>(Short.MIN_VALUE, Short.MAX_VALUE));
        OverflowPentagonLISAConf.semanticChecks.add(new OverflowPentagonChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));

        SuffixDotComLISAConf.semanticChecks.add(new DotComStringChecker<>());

        PrefixHTTPLISAConf.semanticChecks.add(new HTTPStringChecker<>());

        TaintLISAConf.semanticChecks.add(new TaintThreeLevelsChecker<>());




        // we instantiate LiSA and run it for each program

        for(File f : DivByZeroInputs){
            try {
                Program p = IMPFrontend.processFile(f.getAbsolutePath());
                DivByZeroLISAConf.workdir = DivByZeroOutput + "/" + f.getName().replace(".imp", "");
                LiSA lisa = new LiSA(DivByZeroLISAConf);
                lisa.run(p);

                DivByZeroPentagonLISAConf.workdir = DivByZeroPentagonOutput + "/" + f.getName().replace(".imp", "");
                lisa = new LiSA(DivByZeroPentagonLISAConf);
                lisa.run(p);
            } catch (AnalysisException e) {
                System.err.println("Skipping program " + f.getName() + " due to error: " + e.getMessage());
            }
        }

        for(File f : OverflowInputs){
            try {
                Program p = IMPFrontend.processFile(f.getAbsolutePath());
                OverflowLISAConf.workdir = OverflowOutput + "/" + f.getName().replace(".imp", "");
                LiSA lisa = new LiSA(OverflowLISAConf);
                lisa.run(p);

                OverflowPentagonLISAConf.workdir = OverflowPentagonOutput + "/" + f.getName().replace(".imp", "");
                lisa = new LiSA(OverflowPentagonLISAConf);
                lisa.run(p);
            } catch (AnalysisException e) {
                System.err.println("Skipping program " + f.getName() + " due to error: " + e.getMessage());
            }
        }

        for(File f : StringsInputs){
            try {
                Program p = IMPFrontend.processFile(f.getAbsolutePath());
                PrefixHTTPLISAConf.workdir = HttpOutput + "/" + f.getName().replace(".imp", "");
                LiSA lisa = new LiSA(PrefixHTTPLISAConf);
                lisa.run(p);

                SuffixDotComLISAConf.workdir = DotcomOutput + "/" + f.getName().replace(".imp", "");
                lisa = new LiSA(SuffixDotComLISAConf);
                lisa.run(p);
            } catch (AnalysisException e) {
                System.err.println("Skipping program " + f.getName() + " due to error: " + e.getMessage());
            }
        }

        for(File f : TaintInputs){
            try {
                Program p = IMPFrontend.processFile(f.getAbsolutePath());
                TaintLISAConf.workdir = TaintOutput + "/" + f.getName().replace(".imp", "");

                //annotate methods to find sources, sinks and sanitizers
                for(CFG cfg : p.getAllCFGs()) {
                    String name = cfg.getDescriptor().getName();
                    if(isSource(name))
                        cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
                    else if(isSanitizer(name))
                        cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
                    else if(isSink(name))
                        cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
                }

                LiSA lisa = new LiSA(TaintLISAConf);
                lisa.run(p);
            } catch (AnalysisException e) {
                System.err.println("Skipping program " + f.getName() + " due to error: " + e.getMessage());
            }
        }
    }

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