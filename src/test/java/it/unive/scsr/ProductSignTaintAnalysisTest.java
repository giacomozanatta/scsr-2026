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
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.analysis.sign.extended.ExtendedSignLattice;
import it.unive.scsr.analysis.sign.taint.SignTaint;
import it.unive.scsr.analysis.sign.taint.SignTaintLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;
import it.unive.scsr.checkers.PaymentAmountChecker;
import it.unive.scsr.checkers.SignTaintSinkChecker;

public class ProductSignTaintAnalysisTest {

	@Test
	public void testProductLattice() throws SemanticException {
		SignTaintLattice positiveClean = new SignTaintLattice(ExtendedSignLattice.POS, TaintThreeLevelsLattice.CLEAN);
		SignTaintLattice zeroTainted = new SignTaintLattice(ExtendedSignLattice.ZERO, TaintThreeLevelsLattice.TAINTED);
		SignTaintLattice joined = positiveClean.lub(zeroTainted);

		assertEquals(ExtendedSignLattice.NON_NEG, joined.getSign());
		assertEquals(TaintThreeLevelsLattice.TOP, joined.getTaint());
		assertTrue(positiveClean.lessOrEqual(SignTaintLattice.TOP));
	}

	@Test
	public void testProductTaintAndPaymentPrograms() throws ParsingException, AnalysisException {
		runProductAnalysis("inputs/meaningfulprograms/product_taint_payments.imp", "product-taint-payments");
		runProductAnalysis("inputs/meaningfulprograms/product_branch_taint.imp", "product-branch-taint");
		runProductAnalysis("inputs/meaningfulprograms/product_payment_amounts.imp", "product-payment-amounts");
	}

	private void runProductAnalysis(String file, String outputName) throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile(file);
		annotateTaintMethods(program);

		LiSAConfiguration conf = new DefaultConfiguration();
		conf.workdir = "outputs/product-sign-taint/" + outputName;
		conf.analysis = simpleDomain(new PointBasedHeap(), new SignTaint(), defaultTypeDomain());
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
		conf.semanticChecks.add(new SignTaintSinkChecker<>());
		conf.semanticChecks.add(new PaymentAmountChecker<>());
		conf.outputs.add(new HtmlResults<>(true));
		conf.outputs.add(new JSONReportDumper());

		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}

	private void annotateTaintMethods(Program program) {
		for (CFG cfg : program.getAllCFGs()) {
			String name = cfg.getDescriptor().getName();
			if (isSource(name))
				cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
			else if (isSanitizer(name))
				cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
			else if (isSink(name))
				cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
		}
	}

	private boolean isSource(String name) {
		return "sourceAmount".equals(name) || "GetRequest".equals(name);
	}

	private boolean isSanitizer(String name) {
		return "sanitizeAmount".equals(name);
	}

	private boolean isSink(String name) {
		return "runQueryDB".equals(name) || "auditSink".equals(name);
	}
}
