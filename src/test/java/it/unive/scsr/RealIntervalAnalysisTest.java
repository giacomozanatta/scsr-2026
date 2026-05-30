package it.unive.scsr;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.realinterval.RealIntervalLattice;

public class RealIntervalAnalysisTest {

	@Test
	public void testRealIntervalLattice() throws SemanticException {
		RealIntervalLattice minusOnePointFive = new RealIntervalLattice(-1.5);
		RealIntervalLattice zero = new RealIntervalLattice(0.0);
		RealIntervalLattice unit = new RealIntervalLattice(0.0, 1.0);
		RealIntervalLattice bounded = new RealIntervalLattice(0.99, 98.0);

		RealIntervalLattice negativeRange = minusOnePointFive.lub(zero);
		assertEquals(-1.5, negativeRange.getLow(), 0.0);
		assertEquals(0.0, negativeRange.getHigh(), 0.0);

		RealIntervalLattice widened = unit.widening(new RealIntervalLattice(0.0, 2.0));
		assertTrue(Double.isInfinite(widened.getHigh()));
		assertEquals(0.0, widened.getLow(), 0.0);

		assertTrue(bounded.lessOrEqual(RealIntervalLattice.TOP));
		assertEquals(0.99, bounded.getLow(), 0.0);
		assertEquals(98.0, bounded.getHigh(), 0.0);
	}

	@Test
	public void testRealIntervalAnalysisOnFloatProgram() throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile("inputs/real-intervals.imp");

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/real-intervals";
		conf.analysis = simpleDomain(new PointBasedHeap(), new it.unive.scsr.analysis.realinterval.RealInterval(),
				defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());

		new LiSA(conf).run(program);
	}
}
