package it.unive.scsr.checkers;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.AnalyzedCFG;
import it.unive.lisa.analysis.SemanticException;
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
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.symbolic.value.Variable;
import it.unive.scsr.analysis.uninitialized.UninitializedLattice;

public class UninitializedVariableChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<UninitializedLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<UninitializedLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<UninitializedLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<UninitializedLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        // We only care about statements that reference a variable
        if (node instanceof VariableRef varRef) {

            if (varRef.getParentStatement() instanceof Assignment assignment) {
                if (assignment.getLeft() == varRef) {
                    return true; // Skip checking , not an uninitialized read
                }
            }

            for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<UninitializedLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
                try {
                    AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<UninitializedLattice>, TypeEnvironment<T>>> preState = res.getAnalysisStateBefore(varRef);
                    ValueEnvironment<UninitializedLattice> valueEnvironment = preState.getExecutionState().valueState;

                    Variable varIdentifier = new Variable(varRef.getStaticType(), varRef.getName(), varRef.getLocation());
                    UninitializedLattice variableState = valueEnvironment.getState(varIdentifier);

                    if (variableState == UninitializedLattice.UNINITIALIZED) {
                        tool.warnOn(node, "The variable '" + varRef.getName() + "' is definitely uninitialized.");
                    } else if (variableState == UninitializedLattice.TOP) {
                        tool.warnOn(node, "The variable '" + varRef.getName() + "' may be uninitialized.");
                    }
                } catch (SemanticException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}