package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultHeapDomain;
import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.realinterval.RealInterval;
import it.unive.scsr.analysis.realinterval.RealIntervalLattice;

public class RealIntervalAnalysisTest {

	@Test
	public void testRealIntervalLatticeOperations() throws SemanticException {
		RealIntervalLattice small = new RealIntervalLattice(-1.5, 0.0);
		RealIntervalLattice large = new RealIntervalLattice(-2.0, 3.0);

		assertTrue(small.lessOrEqual(large));
		assertEquals(new RealIntervalLattice(-2.0, 3.0), small.lub(large));
		assertEquals(new RealIntervalLattice(-1.5, 0.0), small.glb(large));
		assertEquals(new RealIntervalLattice(-1.5, Double.POSITIVE_INFINITY),
			small.widening(new RealIntervalLattice(-1.0, 1.0)));
	}

	@Test
	public void testRealIntervalAnalysisRunsOnFloatProgram() throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile("inputs/real-intervals.imp");

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/real-intervals";
		conf.analysis = simpleDomain(defaultHeapDomain(), new RealInterval(), defaultTypeDomain());
		conf.outputs.add(new JSONReportDumper());

		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}
}
