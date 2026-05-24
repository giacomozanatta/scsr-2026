package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.analysis.heap.pointbased.PointBasedHeap;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.outputs.JSONReportDumper;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.scsr.checkers.SQLInjectionChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.defaultTypeDomain;
import static it.unive.lisa.DefaultConfiguration.simpleDomain;

public class SQLInjectionAnalysisTest {

    // list of known SQL sink function names
    private static final String[] SINK_NAMES = {
            "runQueryDB",
            "executeQuery",
            "runQuery",
            "execSQL",
            "query",
            "execute",
            "prepareStatement",
            "executeUpdate"
    };

    @Test
    public void testSQLInjection() throws ParsingException, AnalysisException {
        Program program = IMPFrontend.processFile("inputs/sqli.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/sqli";
        conf.outputs.add(new HtmlResults<>(true));
        conf.outputs.add(new JSONReportDumper());

        conf.analysis = simpleDomain(new PointBasedHeap(), new it.unive.lisa.analysis.string.Prefix(), defaultTypeDomain());
        conf.interproceduralAnalysis = new ContextBasedAnalysis<>();

        // annotate sink functions
        for (CFG cfg : program.getAllCFGs()) {
            String name = cfg.getDescriptor().getName();
            if (isSink(name))
                cfg.getDescriptor().addAnnotation(SQLInjectionChecker.SINK_ANNOTATION);
        }

        conf.semanticChecks.add(new SQLInjectionChecker<>());

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }

    private boolean isSink(String name) {
        for (String sink : SINK_NAMES)
            if (sink.equals(name))
                return true;
        return false;
    }
}