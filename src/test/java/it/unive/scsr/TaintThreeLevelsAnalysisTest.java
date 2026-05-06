package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;

import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class TaintThreeLevelsAnalysisTest {

    // Definiamo i nomi delle funzioni nel linguaggio IMP da marcare
    String[] nameSource = {"source1", "GetRequest", "readInput"};
    String[] nameSanitizers = {"sanitizer1", "cleanData"};
    String[] nameSinks = {"sink1", "runQueryDB", "printSensitive"};

    @Test
    public void testTaintAnalysisThreeLevels() throws ParsingException, AnalysisException {
        // 1. Parsing del file .imp (assicurati che il file esista in quella cartella)
        Program program = IMPFrontend.processFile("inputs/taint.imp");

        // 2. Configurazione di LiSA
        LiSAConfiguration conf = new DefaultConfiguration();

        // Cartella di output dedicata per non sovrascrivere il vecchio esercizio
        conf.workdir = "outputs/taint_three_levels";

        // Formato visuale (HTML) e report JSON per i warning
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        // 3. Specifica del dominio di analisi (Heap + Nuovo Dominio Taint + Types)
        // Usiamo TaintThreeLevels al posto del vecchio Taint
        conf.analysis = simpleDomain(new PointBasedHeap(), new TaintThreeLevels(), defaultTypeDomain());

        // Analisi interprocedurale per gestire le chiamate tra funzioni
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // 4. Marcatura dei CFG con le annotazioni (Source, Sanitizer, Sink)
        for(CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();

            if(isSource(name)) {
                // Annotazione standard di LiSA per le sorgenti
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            }
            else if(isSanitizer(name)) {
                // Annotazione standard di LiSA per i sanificatori
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            }
            else if(isSink(name)) {
                // Annotazione SPECIFICA del nostro nuovo dominio a tre livelli
                cfg.getDescriptor().addAnnotation(TaintThreeLevels.SINK_ANNOTATION);
            }
        }

        // 5. Aggiunta del nuovo Checker differenziato
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

        // 6. Esecuzione dell'analisi
        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    // Metodi ausiliari per identificare i nomi delle funzioni
    private boolean isSource(String name) {
        for(String src : nameSource)
            if(src.equals(name)) return true;
        return false;
    }

    private boolean isSanitizer(String name) {
        for(String sanit : nameSanitizers)
            if(sanit.equals(name)) return true;
        return false;
    }

    private boolean isSink(String name) {
        for(String sink : nameSinks)
            if(sink.equals(name)) return true;
        return false;
    }
}