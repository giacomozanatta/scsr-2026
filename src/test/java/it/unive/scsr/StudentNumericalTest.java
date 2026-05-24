package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.scsr.analysis.extendedsign.base.ExtendedSign;
import it.unive.scsr.checkers.DivByZeroExtendedSignChecker;
import it.unive.scsr.checkers.DivByZeroPentagonChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static it.unive.lisa.DefaultConfiguration.*;

public class StudentNumericalTest {

    private static final String INPUT_DIR  = "inputs/students/numerical";
    private static final String OUTPUT_BASE = "outputs/students/numerical";

    private File[] getStudentFiles() {
        File dir = new File(INPUT_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".imp"));
        if (files == null) files = new File[0];
        Arrays.sort(files);
        return files;
    }

    @Test
    public void testDivByZeroExtendedSign() throws ParsingException, AnalysisException {
        for (File f : getStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/divbyzero_extendedsign";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new ExtendedSign(), defaultTypeDomain());
            conf.semanticChecks.add(new DivByZeroExtendedSignChecker<>());
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testDivByZeroExtendedSignPentagon() throws ParsingException, AnalysisException {
        for (File f : getStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/divbyzero_extendedsign_pentagon";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
            conf.semanticChecks.add(new DivByZeroPentagonChecker<>());
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testOverflow8bit() throws ParsingException, AnalysisException {
        for (File f : getStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/overflow_8bit";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Byte.MIN_VALUE, Byte.MAX_VALUE));
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testOverflow16bit() throws ParsingException, AnalysisException {
        for (File f : getStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/overflow_16bit";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Short.MIN_VALUE, Short.MAX_VALUE));
            new LiSA(conf).run(program);
        }
    }

    @Test
    public void testOverflow32bit() throws ParsingException, AnalysisException {
        for (File f : getStudentFiles()) {
            String name = f.getName().replace(".imp", "");
            Program program = IMPFrontend.processFile(f.getPath());
            LiSAConfiguration conf = new DefaultConfiguration();
            conf.workdir = OUTPUT_BASE + "/" + name + "/overflow_32bit";
            conf.outputs.add(new HtmlResults<>(true));
            conf.outputs.add(new JSONReportDumper());
            conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
            conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
            conf.semanticChecks.add(new OverflowIntervalChecker<>(Integer.MIN_VALUE, Integer.MAX_VALUE));
            new LiSA(conf).run(program);
        }
    }
}