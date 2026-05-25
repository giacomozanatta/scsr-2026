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
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.scsr.analysis.sign.taint.SignTaint;
import it.unive.scsr.analysis.sign.taint.SignTaintLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class SignTaintSinkChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
		SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> {

	@Override
	public boolean visit(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> tool,
			CFG graph, Statement node) {
		if (node instanceof UnresolvedCall) {
			UnresolvedCall uc = (UnresolvedCall) node;
			for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> res : tool
					.getResultOf(graph))
				try {
					Call resolved = tool.getResolvedVersion(uc, res);
					if (resolved instanceof NativeCall) {
						for (NativeCFG n : ((NativeCall) resolved).getTargetedConstructs())
							process(tool, uc, resolved, n.getDescriptor(), res);
					} else if (resolved instanceof CFGCall) {
						for (CFG cfg : ((CFGCall) resolved).getTargetedCFGs())
							process(tool, uc, resolved, cfg.getDescriptor(), res);
					}
				} catch (SemanticException e) {
					e.printStackTrace();
				}
		}
		return true;
	}

	private void process(
			SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> tool,
			UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
			AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> res) {
		if (!descriptor.getAnnotations().contains(TaintThreeLevels.SINK_MATCHER))
			return;

		try {
			for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
				Expression par = uc.getParameters()[i];
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> beforePar = res
						.getAnalysisStateBefore(par);
				AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaintLattice>, TypeEnvironment<T>>> afterPar = res
						.getAnalysisStateAfter(par);
				ValueEnvironment<SignTaintLattice> valueState = beforePar.getExecutionState().valueState;
				SignTaint domain = (SignTaint) tool.getAnalysis().domain.valueDomain;
				SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(beforePar.getExecutionState());

				for (SymbolicExpression expr : afterPar.getExecutionExpressions()) {
					if (!(expr instanceof ValueExpression))
						continue;
					SignTaintLattice value = domain.eval(valueState, (ValueExpression) expr, (ProgramPoint) par, oracle);
					TaintThreeLevelsLattice taint = value.getTaint();

					if (taint.isAlwaysTainted())
						tool.warnOn(uc, "Definite warning: sink parameter is definitely tainted at " + par.getLocation());
					else if (taint.isPossiblyTainted())
						tool.warnOn(uc, "Possible warning: sink parameter may be tainted at " + par.getLocation());
				}
			}
		} catch (SemanticException e) {
			e.printStackTrace();
		}
	}
}
