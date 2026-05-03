package it.unive.scsr;

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
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class TaintThreeLevelsAnalysisTest {

	// nomi dei metodi "source", "sanitizer" e "sink" utilizzati in taintthreelevels.imp
	String[] nameSources    = {"source1"}; // funzioni che restituiscono possibili valori "malevoli"
	String[] nameSanitizers = {"sanitizer1"}; // funzioni che vanno a sanitizzare i possibili valori "malevoli"
	String[] nameSinks      = {"sink1"}; // funzioni che vanno ad utilizzare i possibili valori "malevoli"

	@Test
	public void testTaintThreeLevels() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/taint.imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new DefaultConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/taint-three-levels";

		// we specify the visual format of the analysis results
		conf.outputs.add(new HtmlResults<>(true));

		// we specify the analysis that we want to execute
		conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

		for (CFG cfg : program.getAllCFGs()) {
			String name = cfg.getDescriptor().getName();
			if (isSource(name)) {
				// i valori restituiti dalla funzione che sto considerando sono "tainted"
				cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
			}
			else if (isSanitizer(name)) {
				// i valori restituiti dalla funzione che sto considerando sono "clean"
				cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
			}
			else if (isSink(name)) {
				// i valori restituiti dalla funzione che sto considerando devono lanciare un warning
				cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
			}
		}

		conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

		// json dumper per poter ispezionare eventuali warning (es: una riga con un valore TAINTED)
		conf.outputs.add(new JSONReportDumper());

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}

	/**
	 * Verifico se la funzione è tra quelle considerate "source"
	 */
	private boolean isSource(String name) {
		for (String src : nameSources) {
			if (src.equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifico se la funzione è tra quelle considerate "sanitizer"
	 */
	private boolean isSanitizer(String name) {
		for (String san : nameSanitizers) {
			if (san.equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifico se la funzione è tra quelle considerate "sink"
	 */
	private boolean isSink(String name) {
		for (String sink : nameSinks) {
			if (sink.equals(name)) {
				return true;
			}
		}
		return false;
	}
}