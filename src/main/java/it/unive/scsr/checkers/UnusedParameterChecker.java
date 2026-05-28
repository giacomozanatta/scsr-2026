package it.unive.scsr.checkers;

import it.unive.lisa.analysis.*;
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
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.util.numeric.IntInterval;

import java.util.*;


public class UnusedParameterChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> {

	Dictionary<Integer,Set<String>> unusedVariables = new Hashtable<>();
	Set<Integer> processedFunctions = new HashSet<>();
	Boolean startedProcessing = false;
	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {
		if (!processedFunctions.contains(graph.getDescriptor().hashCode())) {
            for (Iterator<Integer> it = unusedVariables.keys().asIterator(); it.hasNext(); ) {
                Integer x = it.next();
				if (x == graph.getDescriptor().hashCode()) {
					startedProcessing = false;
				}
            }
			if (!startedProcessing)
				unusedVariables.put(graph.getDescriptor().hashCode(), new HashSet<>());

			for (var v : graph.getDescriptor().getVariables()) {
				if (!v.getName().equals("this")) {
					unusedVariables.get(graph.getDescriptor().hashCode()).add(v.getName());
				}
			}
			processedFunctions.add(graph.getDescriptor().hashCode());
		}
		for (var res : tool.getResultOf(graph)) {
			AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> postState = res
					.getAnalysisStateAfter(node);
			Set<SymbolicExpression> reachableIds = new HashSet<>();
			Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
			if (comExprIterator.hasNext()) {

				SymbolicExpression boolExpr = comExprIterator.next();
				try {
					reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr,
							(Statement) node).elements);
				} catch (SemanticException e) {
					e.printStackTrace();
				}
			}
			for (var reachId : reachableIds) {
                for (Iterator<Set<String>> it = unusedVariables.elements().asIterator(); it.hasNext(); ) {
                    Set<String> set = it.next();
                    set.remove(reachId.toString());
                }
			}
			try {
				if (postState == res.getExitState()) {
					if (!unusedVariables.isEmpty()) {
						tool.warnOn(graph, "There is(are) an unused variable(s): " + unusedVariables.get(graph.getDescriptor().hashCode()) + " in " + graph.getDescriptor().getFullName());
					}
					unusedVariables.remove(graph.getDescriptor().getFullName());
				}
			} catch (SemanticException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}