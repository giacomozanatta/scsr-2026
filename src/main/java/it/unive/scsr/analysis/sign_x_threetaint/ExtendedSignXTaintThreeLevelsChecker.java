package it.unive.scsr.analysis.sign_x_threetaint;

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
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// This checker detects functions annotated as sinks, it inspects the arguments 
// passed to that call and emits a definite warning when its value is tainted, and a possible warning when its value is top.
public class ExtendedSignXTaintThreeLevelsChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>> {

	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {

		if (node instanceof UnresolvedCall uc) { // check if the current visited node correspond to a function call
            for (var res : tool.getResultOf(graph)) { // get analysis result for that graph
				try {
					Call resolved = tool.getResolvedVersion(uc, res);
					if (resolved instanceof NativeCall) {
						var nativeCfgs = ((NativeCall) resolved).getTargetedConstructs();
						for (NativeCFG n : nativeCfgs)
							process(tool, uc, resolved, n.getDescriptor(), res);
					} else if (resolved instanceof CFGCall cfg) {
                        for (CFG n : cfg.getTargetedCFGs())
							process(tool, uc, resolved, n.getDescriptor(), res);
					}
				} catch (SemanticException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private void process(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>> tool,
			UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
			AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>> res) {
		if (descriptor.getAnnotations().contains(ExtendedSignXThreeTaint.SINK_MATCHER)) { // check if the called function is annotated as sink
			for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
				Expression par = uc.getParameters()[i];
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignXThreeTaintLattice>, TypeEnvironment<T>>> postState = res
						.getAnalysisStateAfter(par); // compute the post state related to each parameter
				Set<SymbolicExpression> reachableIds = new HashSet<>();
				Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
				if (comExprIterator.hasNext()) {

					SymbolicExpression boolExpr = comExprIterator.next();
					try {
						reachableIds
								.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, (Statement) uc).elements);

						for (SymbolicExpression s : reachableIds) {
							Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, uc);

							if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
								continue;
							//extraction of the abstract value
							ValueEnvironment<ExtendedSignXThreeTaintLattice> valueState = postState.getExecutionState().valueState;
							ExtendedSignXThreeTaint signAnalysisValueDomain = (ExtendedSignXThreeTaint) tool.getAnalysis().domain.valueDomain;
							SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
							ExtendedSignXThreeTaintLattice abstractValue = signAnalysisValueDomain.eval(valueState, (ValueExpression) s,
									uc, oracle);

							//check abstract value
							if (abstractValue.threeTaint == TaintThreeLevelsLattice.TAINT){
								tool.warnOn(uc,"There is a taint value in a sink: " + par.getLocation());
							}
							else if (abstractValue.threeTaint == TaintThreeLevelsLattice.TOP){
								tool.warnOn(uc,"There might be a taint value in a sink: " + par.getLocation());
							}
						}
					} catch (SemanticException e) {
						e.printStackTrace();
					}
				}

			}
		}

	}

}