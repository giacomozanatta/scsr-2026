package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.scsr.analysis.taint.Taint;
import it.unive.scsr.analysis.taint.TaintChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.*;
import it.unive.scsr.analysis.signthreetaint.SignThreeTaint;
import it.unive.scsr.analysis.signthreetaint.SignThreeTaintChecker;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class SignThreeTaintTest {

	String[] nameSource     = {"source1", "GetRequest", "getUserInput"};
	String[] nameSanitizers = {"sanitizer1", "sanitizeInput", "escapeHtml"};
	String[] nameSinks      = {"sink1", "runQueryDB", "renderHtml", "sendEmail"};	
	
	
    @Test
public void testTaintAnalysis() throws ParsingException, AnalysisException, IOException {
    List<Path> files = Files.walk(Path.of("inputs/otherprograms/taintthreelevels"))
        .filter(p -> p.toString().endsWith(".imp"))
        .collect(Collectors.toList());

    for (Path file : files) {
        String inputFile = file.toString();
        Program program = IMPFrontend.processFile(inputFile);

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/signthreetaint/"
            + file.getFileName().toString().replace(".imp", "");
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());
        conf.analysis = simpleDomain(defaultHeapDomain(), new SignThreeTaint(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSource(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if (isSanitizer(name))
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if (isSink(name))
                cfg.getDescriptor().addAnnotation(SignThreeTaint.SINK_ANNOTATION);
        }

        conf.semanticChecks.add(new SignThreeTaintChecker<>());
        new LiSA(conf).run(program);
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

