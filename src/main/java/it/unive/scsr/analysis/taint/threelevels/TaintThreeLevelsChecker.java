package it.unive.scsr.analysis.taint.threelevels;

import java.util.HashSet;
import java.util.Set;
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
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.ProgramPoint;
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

public class TaintThreeLevelsChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>,
				SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> {

	@Override
	public boolean visit(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>,
			SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> tool, CFG graph, Statement node) {

		if (node instanceof UnresolvedCall uc) {
			for (var res : tool.getResultOf(graph)) {
				try {
					Call resolved = tool.getResolvedVersion(uc, res);
					if (resolved instanceof NativeCall nc) {
						for (NativeCFG n : nc.getTargetedConstructs())
							process(tool, uc, resolved, n.getDescriptor(), res);
					} else if (resolved instanceof CFGCall cc) {
						for (CFG n : cc.getTargetedCFGs())
							process(tool, uc, resolved, n.getDescriptor(), res);
					}
				} catch (SemanticException e) { e.printStackTrace(); }
			}
		}
		return true;
	}

	private void process(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>,
								 SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> tool,
	                     UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
	                     AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> res) {

		if (descriptor.getAnnotations().contains(TaintThreeLevels.SINK_MATCHER)) {
			int start = (resolved.getCallType() == CallType.INSTANCE ? 1 : 0);
			for (int i = start; i < uc.getParameters().length; i++) {
				Expression par = uc.getParameters()[i];
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(par);

				for (SymbolicExpression expr : postState.getExecutionExpressions()) {
					try {
						Set<SymbolicExpression> reachableIds = new HashSet<>(tool.getAnalysis().reachableFrom(postState, expr, (Statement) uc).elements);
						for (SymbolicExpression s : reachableIds) {
							if (!(s instanceof ValueExpression)) continue;

							Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, (Statement) uc);
							if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType())) continue;

							ValueEnvironment<TaintThreeLevelsLattice> valueState = postState.getExecutionState().valueState;
							TaintThreeLevels taintDomain = (TaintThreeLevels) tool.getAnalysis().domain.valueDomain;
							SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());

							TaintThreeLevelsLattice abstractValue = taintDomain.eval(valueState, (ValueExpression) s, (ProgramPoint) uc, oracle);

							if (abstractValue.isAlwaysTainted()) {
								tool.warnOn(uc, "[DEFINITE] Tainted value reaches sink: " + par.getLocation());
							} else if (abstractValue.isPossiblyTainted()) {
								tool.warnOn(uc, "[POSSIBLE] Possibly tainted value (uncertain) reaches sink: " + par.getLocation());
							}
						}
					} catch (SemanticException e) { e.printStackTrace(); }
				}
			}
		}
	}
}