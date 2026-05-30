package it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.DefaultConfiguration;
import it.unive.lisa.LiSA;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.outputs.HtmlResults;
import it.unive.lisa.program.Program;
import it.unive.scsr.analysis.combined.SignTaint;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsChecker;
import org.junit.Test;

import static it.unive.lisa.DefaultConfiguration.*;

public class TaintThreeLevelsTest {

    @Test
    public void testTaintThreeLevels() throws ParsingException, AnalysisException {
        // Парсим входной файл с примером уязвимого кода
        // Убедись, что файл inputs/taint.imp существует, или замени на свой .imp файл
        Program program = IMPFrontend.processFile("inputs/taint.imp");

        LiSAConfiguration conf = new DefaultConfiguration();
        conf.workdir = "outputs/taint-three-levels";
        
        // Добавляем HTML отчеты для красоты
        conf.outputs.add(new HtmlResults<>(true));

        // 1. ПОДКЛЮЧАЕМ ТВОЙ ЧЕКЕР (тот самый, что ищет Taint в Sink)
        conf.semanticChecks.add(new TaintThreeLevelsChecker<>());

        // 2. УКАЗЫВАЕМ ТВОЙ ДОМЕН SignTaint
        conf.analysis = simpleDomain(
            defaultHeapDomain(), 
            new SignTaint(), 
            defaultTypeDomain()
        );

        LiSA lisa = new LiSA(conf);
        lisa.run(program);
    }
}