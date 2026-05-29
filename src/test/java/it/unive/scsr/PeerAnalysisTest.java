package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.lattices.numeric.PentagonLattice;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.SimpleAbstractDomain;

import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import org.junit.Test;
import java.io.File;

// Importa tutti i tuoi checker
import it.unive.scsr.checkers.DotComStringChecker;
import it.unive.scsr.checkers.HTTPStringChecker;
import it.unive.scsr.checkers.DivByZeroIntervalChecker;
import it.unive.scsr.checkers.DivByZeroPentagonChecker;
import it.unive.scsr.checkers.OverflowIntervalChecker;
import it.unive.scsr.checkers.OverflowPentagonChecker;
import it.unive.scsr.checkers.ArrayBoundsPentagonChecker;

import static it.unive.lisa.DefaultConfiguration.*;

public class PeerAnalysisTest {

    // Helper per recuperare automaticamente tutti i file .imp da una specifica cartella
    private File[] getImpFilesFromFolder(String folderName) {
        File folder = new File("inputs/peer-programs/" + folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("ATTENZIONE: Cartella non trovata -> " + folder.getAbsolutePath());
            return new File[0];
        }
        return folder.listFiles((dir, name) -> name.endsWith(".imp"));
    }

    // ==========================================
    // TEST OVERFLOW
    // ==========================================

    @Test
    public void testOverflowInterval() {
        File[] files = getImpFilesFromFolder("overflow");
        for (File file : files) {
            try {
                Program program = IMPFrontend.processFile(file.getPath());
                LiSAConfiguration conf = new DefaultConfiguration();

                conf.workdir = "outputs/peer-analysis/overflow/interval/" + file.getName().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());

                conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new OverflowIntervalChecker<>(-2147483648, 2147483647)); // 32-bit

                LiSA lisa = new LiSA(conf);
                lisa.run(program);
            } catch (Exception e) {
                System.err.println("Errore su " + file.getName() + " (OverflowInterval): " + e.getMessage());
            }
        }
    }

    @Test
    public void testOverflowPentagon() {
        File[] files = getImpFilesFromFolder("overflow");
        for (File file : files) {
            try {
                Program program = IMPFrontend.processFile(file.getPath());
                LiSAConfiguration conf = new DefaultConfiguration();

                conf.workdir = "outputs/peer-analysis/overflow/pentagon/" + file.getName().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());

                conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new OverflowPentagonChecker<>(-2147483648, 2147483647)); // 32-bit

                LiSA lisa = new LiSA(conf);
                lisa.run(program);
            } catch (Exception e) {
                System.err.println("Errore su " + file.getName() + " (OverflowPentagon): " + e.getMessage());
            }
        }
    }

    // ==========================================
    // TEST DIVISION BY ZERO
    // ==========================================

    @Test
    public void testDivByZeroInterval() {
        File[] files = getImpFilesFromFolder("divbyzero");
        for (File file : files) {
            try {
                Program program = IMPFrontend.processFile(file.getPath());
                LiSAConfiguration conf = new DefaultConfiguration();

                conf.workdir = "outputs/peer-analysis/divbyzero/interval/" + file.getName().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());

                conf.analysis = simpleDomain(defaultHeapDomain(), new Interval(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new DivByZeroIntervalChecker<>());

                LiSA lisa = new LiSA(conf);
                lisa.run(program);
            } catch (Exception e) {
                System.err.println("Errore su " + file.getName() + " (DivByZeroInterval): " + e.getMessage());
            }
        }
    }

    @Test
    public void testDivByZeroPentagon() {
        File[] files = getImpFilesFromFolder("divbyzero");
        for (File file : files) {
            try {
                Program program = IMPFrontend.processFile(file.getPath());
                LiSAConfiguration conf = new DefaultConfiguration();

                conf.workdir = "outputs/peer-analysis/divbyzero/pentagon/" + file.getName().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());

                conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new DivByZeroPentagonChecker<>());

                LiSA lisa = new LiSA(conf);
                lisa.run(program);
            } catch (Exception e) {
                System.err.println("Errore su " + file.getName() + " (DivByZeroPentagon): " + e.getMessage());
            }
        }
    }

    // ==========================================
    // TEST STRINGS
    // ==========================================

    @Test
    public void testStringPrefixAnalysis() {
        File[] files = getImpFilesFromFolder("strings");
        for (File file : files) {
            try {
                Program program = IMPFrontend.processFile(file.getPath());
                LiSAConfiguration conf = new DefaultConfiguration();

                conf.workdir = "outputs/peer-analysis/strings/prefix/" + file.getName().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());

                conf.analysis = simpleDomain(defaultHeapDomain(), new Prefix(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new HTTPStringChecker<>());

                LiSA lisa = new LiSA(conf);
                lisa.run(program);
            } catch (Exception e) {
                System.err.println("Errore su " + file.getName() + " (StringPrefix): " + e.getMessage());
            }
        }
    }

    @Test
    public void testStringSuffixAnalysis() {
        File[] files = getImpFilesFromFolder("strings");
        for (File file : files) {
            try {
                Program program = IMPFrontend.processFile(file.getPath());
                LiSAConfiguration conf = new DefaultConfiguration();

                conf.workdir = "outputs/peer-analysis/strings/suffix/" + file.getName().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());

                conf.analysis = simpleDomain(defaultHeapDomain(), new Suffix(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new DotComStringChecker<>());

                LiSA lisa = new LiSA(conf);
                lisa.run(program);
            } catch (Exception e) {
                System.err.println("Errore su " + file.getName() + " (StringSuffix): " + e.getMessage());
            }
        }
    }

    // ==========================================
    // TEST ARRAY BOUNDS (PENTAGON)
    // ==========================================

    @Test
    public void testArrayBoundsPentagon() {
        // Metti i file in inputs/peer-analysis/arrays o cambia la stringa
        File[] files = getImpFilesFromFolder("taintthreelevels");
        for (File file : files) {
            try {
                Program program = IMPFrontend.processFile(file.getPath());
                LiSAConfiguration conf = new DefaultConfiguration();

                conf.workdir = "outputs/peer-analysis/taintthreelevels/" + file.getName().replace(".imp", "");
                conf.outputs.add(new HtmlResults<>(true));
                conf.outputs.add(new JSONReportDumper());

                // NOTA: ArrayBounds usa il PentagonLattice come dominio matematico

                conf.analysis = simpleDomain(defaultHeapDomain(), new Pentagon(), defaultTypeDomain());
                conf.interproceduralAnalysis = new ContextBasedAnalysis<>();
                conf.semanticChecks.add(new ArrayBoundsPentagonChecker<>());
                conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

                LiSA lisa = new LiSA(conf);
                lisa.run(program);
            } catch (Exception e) {
                System.err.println("Errore su " + file.getName() + " (ArrayBounds): " + e.getMessage());
            }
        }
    }
}