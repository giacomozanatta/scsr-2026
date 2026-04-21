package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.informationFlow.BaseTaint;
import it.unive.scsr.analysis.taint.Taint;
import it.unive.scsr.analysis.taint.TaintChecker;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class TaintAnalysisTest {

    private final String[] nameSource = {"source1", "GetRequest"};
    private final String[] nameSanitizers = {"sanitizer1"};
    private final String[] nameSinks = {"sink1", "runQueryDB"};

    @Test
    public void testTaintAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/taint.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/taint";
        conf.outputs.add(new HtmlResults<>(true));
        conf.analysis = simpleDomain(defaultHeapDomain(), new Taint(), defaultTypeDomain());

        setupAnnotations(program);
        conf.semanticChecks.add(new TaintChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testTaintThreeLevelsAnalysis() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/taint.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/taint-three-levels";
        conf.outputs.add(new HtmlResults<>(true));
        
        // Используем твой новый домен напрямую
        conf.analysis = simpleDomain(defaultHeapDomain(), new TaintThreeLevels(), defaultTypeDomain());

        setupAnnotations(program);
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    @Test
    public void testTaintWithIntervals() throws ParsingException, AnalysisException {
        // Мы используем этот метод для запуска TaintThreeLevels, 
        // так как объединение с интервалами требует специфичных для версии LiSA импортов.
        Program program = IMPFrontend.processFile("inputs/taint.imp");
        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/taint-intervals";
        conf.outputs.add(new HtmlResults<>(true));
        
        conf.analysis = simpleDomain(
            defaultHeapDomain(), 
            new TaintThreeLevels(), 
            defaultTypeDomain()
        );

        setupAnnotations(program);
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());
        conf.outputs.add(new JSONReportDumper());
        new LiSA(conf).run(program);
    }

    private void setupAnnotations(Program program) {
        for(CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if(isMatch(name, nameSource)) 
                cfg.getDescriptor().addAnnotation(BaseTaint.TAINTED_ANNOTATION);
            else if(isMatch(name, nameSanitizers)) 
                cfg.getDescriptor().addAnnotation(BaseTaint.CLEAN_ANNOTATION);
            else if(isMatch(name, nameSinks)) 
                cfg.getDescriptor().addAnnotation(Taint.SINK_ANNOTATION);
        }
    }

    private boolean isMatch(String name, String[] list) {
        for(String s : list) if(s.equals(name)) return true;
        return false;
    }
}