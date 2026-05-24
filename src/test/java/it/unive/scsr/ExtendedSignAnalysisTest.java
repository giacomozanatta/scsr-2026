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
import it.unive.scsr.analysis.sign.extended.ExtendedSign;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;

public class ExtendedSignAnalysisTest {

	@Test
	public void testExtendedSignLatticeOrdering() throws SemanticException {
		assertTrue(ExtendedSignLattice.POS.lessOrEqual(ExtendedSignLattice.NON_NEG));
		assertTrue(ExtendedSignLattice.ZERO.lessOrEqual(ExtendedSignLattice.NON_NEG));
		assertTrue(ExtendedSignLattice.NEG.lessOrEqual(ExtendedSignLattice.NON_ZERO));
		assertEquals(ExtendedSignLattice.NON_NEG, ExtendedSignLattice.ZERO.lub(ExtendedSignLattice.POS));
		assertEquals(ExtendedSignLattice.ZERO, ExtendedSignLattice.NON_NEG.glb(ExtendedSignLattice.NON_POS));
	}

	@Test
	public void testExtendedSignAnalysisRuns() throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile("inputs/extended-signs.imp");

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/extended-signs";
		conf.analysis = simpleDomain(defaultHeapDomain(), new ExtendedSign(), defaultTypeDomain());
		conf.outputs.add(new JSONReportDumper());

		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}
}
