package it.unive.scsr.checkers;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.AnalyzedCFG;
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
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.scsr.analysis.sign.extendedSign.ExtendedSignLattice;

public class DeadCodeChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        // Filtro anti-spam: controlliamo solo le istruzioni principali che hanno impatto logico
        if (!(node instanceof Assignment) && !(node instanceof Return) && !(node instanceof Call)) {
            return true;
        }

        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(node);

            if (postState != null) {
                // ESTRAZIONE PURA: Controlliamo SOLO il nostro dominio matematico ExtendedSign!
                ValueEnvironment<ExtendedSignLattice> valueState = postState.getExecutionState().valueState;

                if (valueState.isBottom()) {
                    tool.warnOn(node, "Code Property Detected [Dead Code]: This statement is mathematically unreachable.");
                }
            }
        }

        return true;
    }
}
