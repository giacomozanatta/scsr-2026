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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



public class UnusedParameterChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> {

	Set<String> unusedVariables = new HashSet<>();
	Set<Integer> processedFunctions = new HashSet<>();
	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {
		if (!processedFunctions.contains(graph.getDescriptor().hashCode())) {
			for (var v : graph.getDescriptor().getVariables()) {
				if (!v.getName().equals("this")) {
					unusedVariables.add(graph.getDescriptor().getName() + " " + v.getName());
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
				for (var x : unusedVariables) {
					if ((graph.getDescriptor().getName() + " " +reachId.toString()).equals(x)) {
						unusedVariables.remove(x);
						break;
					}
				}
			}
			try {
				if (postState == res.getExitState()) {
					for (var variable : unusedVariables) {
						tool.warnOn(graph, "There is(are) an unused variable(s): " + variable + " in " + graph.getDescriptor().getFullName());
					}
				}
			} catch (SemanticException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}