package it.unive.scsr.checkers;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.AnalyzedCFG;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.SimpleAbstractDomain;
import it.unive.lisa.analysis.nonrelational.heap.HeapEnvironment;
import it.unive.lisa.analysis.nonrelational.heap.HeapValue;
import it.unive.lisa.analysis.nonrelational.type.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.type.TypeValue;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.numeric.Division;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.scsr.analysis.extendedsign.base.ExtendedSignLattice;
import it.unive.scsr.analysis.extendedsign.signtaint.ExtendedSignThreeTaint;
import it.unive.scsr.analysis.extendedsign.signtaint.ExtendedSignThreeTaintLattice;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Checker per emettere un warning in caso di divisione per zero con un valore "tainted" in ExtendedSign x ThreeTaint;
 * viene emesso [DEFINITE] qualora (sign in (ZERO, LTE_ZERO, GTE_ZERO, TOP); taint=TAINT) --> valore controllato dall'attaccante e che potrebbe essere zero;
 * viene emesso [POSSIBLE] qualora (sign in (ZERO, LTE_ZERO, GTE_ZERO, TOP); taint=TOP) --> il valore potrebbe essere "taint" e potrebbe essere zero:
 *          potrebbe essere una situazione tipo:
 *          1. ramo if in cui il valore inserito dall'utente viene sanitizzato;
 *          2. ramo if-else in cui da una parte il valore viene inserito dall'utente, nell'altra è fisso;
 * Questo checker è interessante perché permette di rilevare una possibile situazione di DoS; se si suppone che il valore "zero", il divisore,
 * viene salvato a DB, siamo di fronte a una cosa molto simile allo stored-XSS; in pratica, chiunque visiti la pagina dove il divisore=zero
 * viene utilizzato, vedrà crashare l'applicazione.
 */
public class TaintedDivisorChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<
                SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {
        if (node instanceof Division) {
            checkDivision(tool, graph, (Division) node);
        }
        return true;
    }

    private void checkDivision(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Division div) {

        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignThreeTaintLattice>, TypeEnvironment<T>>> postState =
                    res.getAnalysisStateAfter(div.getRight());

            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> iter = postState.getExecutionExpressions().iterator();
            if (!iter.hasNext()) {
                continue;
            }

            SymbolicExpression boolExpr = iter.next();
            try {
                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, div).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, div);
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType())) {
                        continue;
                    }

                    ValueEnvironment<ExtendedSignThreeTaintLattice> valueState = postState.getExecutionState().valueState;
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    ExtendedSignThreeTaint domain = (ExtendedSignThreeTaint) tool.getAnalysis().domain.valueDomain;
                    ExtendedSignThreeTaintLattice value = domain.eval(valueState, (ValueExpression) s, (ProgramPoint) div, oracle);

                    boolean signCouldBeZero = value.sign == ExtendedSignLattice.ZERO
                            || value.sign == ExtendedSignLattice.LTE_ZERO
                            || value.sign == ExtendedSignLattice.GTE_ZERO
                            || value.sign == ExtendedSignLattice.TOP;

                    if (!signCouldBeZero || !value.taint.isPossiblyTainted()) {
                        continue;
                    }

                    if (value.taint.isAlwaysTainted()) {
                        tool.warnOn(div, "[DEFINITE] Divisor is attacker-controlled and may be zero: potential DoS attack");
                    } else {
                        tool.warnOn(div, "[POSSIBLE] Divisor may be attacker-controlled and may be zero: potential DoS attack");
                    }
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }
}
