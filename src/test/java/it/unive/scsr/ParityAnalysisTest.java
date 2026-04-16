package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.scsr.analysis.parity.Parity; // <-- Importiamo solo il dominio Parity
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class ParityAnalysisTest {

    @Test
    public void testParityAnalysis() throws ParsingException, AnalysisException {
        // 1. Leggiamo il programma da analizzare (es. un file con la classe parity)
        Program program = IMPFrontend.processFile("inputs/parity.imp");

        LiSAConfiguration conf = new DefaultConfiguration();

        // 2. Impostiamo una cartella di output separata per non sovrascrivere i risultati precedenti
        conf.workdir = "outputs/parity";

        conf.outputs.add(new HtmlResults<>(true));

        // 3. IL CAMBIAMENTO FONDAMENTALE:
        // Diciamo a LiSA di usare il dominio 'Parity' (invece di 'Sign') per i calcoli
        conf.analysis = simpleDomain(defaultHeapDomain(), new Parity(), defaultTypeDomain());

        // (Abbiamo rimosso il semanticChecks.add(...) e il JSONReportDumper perché non richiesti)

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}