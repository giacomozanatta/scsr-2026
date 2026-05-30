package it.unive.scsr.student_test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.outputs.compare.ResultComparer;
import it.unive.lisa.outputs.json.JsonReport;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThreeTaintEvaluation {

	String[] nameSource = {"source1", "GetRequest", "getUserInput"};
	String[] nameSanitizers = {"sanitizeInput", "sanitizer1"};
	String[] nameSinks = {"sink1", "runQueryDB", "sendEmail", "renderHtml"};



	@Test
	public void testThreeTaintAnalysis() throws ParsingException, AnalysisException {
		List<String> files = List.of("894069_taint.imp", "894004_taint.imp");

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.outputs.add(new HtmlResults<>(true));
		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

	
		// added checker to the analysis
		conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
		// A report file (.json) containing the warning triggered by the analysis can be found in the analysis output folder 
		conf.outputs.add(new JSONReportDumper());

		for(String f : files){
			String dir = f.replaceAll(".imp$", "");
			Program program = IMPFrontend.processFile("inputs/student_programs/taint/" + f);
			conf.workdir = "outputs/student_programs/taint/" + dir;
			for(CFG cfg : program.getAllCFGs()) {
				String name = cfg.getDescriptor().getName();
				if(isSource(name))
					cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
				else if(isSanitizer(name))
					cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
				else if(isSink(name))
					cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
			}
			LiSA lisa = new LiSA(conf);
			lisa.run(program);

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
