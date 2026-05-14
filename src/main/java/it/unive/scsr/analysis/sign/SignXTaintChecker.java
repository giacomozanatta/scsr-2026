package it.unive.scsr.analysis.sign;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.AnalyzedCFG;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SemanticOracle;
import it.unive.lisa.analysis.SimpleAbstractDomain;
import it.unive.lisa.analysis.combination.LatticeProduct;
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
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

// Checker for SignXTaint domain: extracts the taint component (second) from
// the LatticeProduct<SignLattice, TaintThreeLevelsLattice> to detect tainted sink arguments.
public class SignXTaintChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>> {

	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {

		if (node instanceof UnresolvedCall) {
			UnresolvedCall uc = (UnresolvedCall) node;
			for (var res : tool.getResultOf(graph)) {
				try {
					Call resolved = tool.getResolvedVersion(uc, res);
					if (resolved instanceof NativeCall) {
						var nativeCfgs = ((NativeCall) resolved).getTargetedConstructs();
						for (NativeCFG n : nativeCfgs)
							process(tool, uc, resolved, n.getDescriptor(), res);
					} else if (resolved instanceof CFGCall) {
						CFGCall cfg = (CFGCall) resolved;
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
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>> tool,
			UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
			AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>> res) {

		if (descriptor.getAnnotations().contains(TaintThreeLevels.SINK_MATCHER)) {
			for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
				Expression par = uc.getParameters()[i];
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>>, TypeEnvironment<T>>> postState = res
						.getAnalysisStateAfter(par);
				Set<SymbolicExpression> reachableIds = new HashSet<>();
				Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
				if (comExprIterator.hasNext()) {
					SymbolicExpression boolExpr = comExprIterator.next();
					try {
						reachableIds
								.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, (Statement) uc).elements);

						for (SymbolicExpression s : reachableIds) {
							Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, (Statement) uc);

							if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
								continue;

							ValueEnvironment<LatticeProduct<SignLattice, TaintThreeLevelsLattice>> valueState = postState
									.getExecutionState().valueState;
							SignXTaint signAnalysisValueDomain = (SignXTaint) tool.getAnalysis().domain.valueDomain;
							SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
							LatticeProduct<SignLattice, TaintThreeLevelsLattice> abstractValue = signAnalysisValueDomain
									.eval(valueState,
											(ValueExpression) s, (ProgramPoint) uc, oracle);

							if (abstractValue.second.isAlwaysTainted()) {
								tool.warnOn(uc, "Definite vulnerability: argument " + (i + 1) + " is tainted");
							} else if (abstractValue.second.isPossiblyTainted()) {
								tool.warnOn(uc, "Possible vulnerability: argument " + (i + 1) + " is possibly tainted");
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
