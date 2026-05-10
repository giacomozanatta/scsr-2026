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
import it.unive.scsr.analysis.extendedsign.base.ExtendedSign;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Checker per emettere un warning in caso di divisione per zero in extendedsign;
 * viene emesso [DEFINITE] qualora il valore sia zero;
 * viene emesso [POSSIBLE] qualora il valore potrebbe essere zero (TOP, LTE_ZERO, GTE_ZERO);
 */
public class DivByZeroExtendedSignChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {
        if (node instanceof Division) {
            checkDivision(tool, graph, (Division) node);
        }
        return true;
    }

    private void checkDivision(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Division div) {

        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> postState =
                    res.getAnalysisStateAfter(div.getRight());

            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
            if (!it.hasNext()) continue;

            SymbolicExpression expr = it.next();
            try {
                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, expr, div).elements);

                for (SymbolicExpression s : reachableIds) {
                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, div);
                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                        continue;

                    var valueState = postState.getExecutionState().valueState;
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    ExtendedSign domain = (ExtendedSign) tool.getAnalysis().domain.valueDomain;
                    ExtendedSignLattice value = domain.eval(valueState, (ValueExpression) s, (ProgramPoint) div, oracle);

                    if (value == ExtendedSignLattice.ZERO) {
                        tool.warnOn(div, "[DEFINITE] Division by zero: divisor is always zero");
                    } else if (
                        value == ExtendedSignLattice.LTE_ZERO
                        || value == ExtendedSignLattice.GTE_ZERO
                        || value == ExtendedSignLattice.TOP
                    ) {
                        tool.warnOn(div, "[POSSIBLE] Division by zero: divisor may be zero");
                    }
                    // NEG, POS, NON_ZERO → safe, no warning
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }
}
