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

import static it.unive.lisa.DefaultConfiguration.*;

public class StringAnalysesTest {
	
	
    @Test
    public void testStringPrefixAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/strings.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/strings/prefix";

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

    @Test
    public void testStringSuffixAnalysis() throws ParsingException, AnalysisException {
        // we parse the program to get the CFG representation of the code in it
        Program program = IMPFrontend.processFile("inputs/strings.imp");

        // we build a new configuration for the analysis
        LiSAConfiguration conf = new DefaultConfiguration();

        // we specify where we want files to be generated
        conf.workdir = "outputs/strings/suffix";

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

    @Test
    public void testUrlRouter() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/strings-2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/strings-2/http";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);

        LiSAConfiguration conf2 = new DefaultConfiguration();
        conf2.workdir = "outputs/strings-2/dotcom";
        conf2.outputs.add(new HtmlResults<>(true));
        conf2.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf2.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf2.semanticChecks.add(new DotComStringChecker<>());
        conf2.outputs.add(new JSONReportDumper());
        new LiSA(conf2).run(program);
    }

    @Test
    public void testHttpApiClient() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_http_string_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/http-apiclient";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testHttpSecureRouter() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_http_string_2.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/http-securerouter";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new HTTPStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testDotComCloudRouter() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/other/895227_897270_dot_com_string_1.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/other/dotcom-cloudrouter";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
        conf.semanticChecks.add(new DotComStringChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }
}
