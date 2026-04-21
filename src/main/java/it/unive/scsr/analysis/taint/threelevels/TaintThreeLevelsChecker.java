package it.unive.scsr.analysis.taint.threelevels;

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
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.scsr.analysis.taint.Taint;

public class TaintThreeLevelsChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> tool, CFG graph, Statement node) {
        if (node instanceof UnresolvedCall) {
            UnresolvedCall uc = (UnresolvedCall) node;
            for (var res : tool.getResultOf(graph)) {
                try {
                    Call resolved = tool.getResolvedVersion(uc, res);
                    if (resolved instanceof NativeCall) {
                        for (var n : ((NativeCall) resolved).getTargetedConstructs())
                            process(tool, uc, resolved, n.getDescriptor(), res);
                    } else if (resolved instanceof CFGCall) {
                        for (var n : ((CFGCall) resolved).getTargetedCFGs())
                            process(tool, uc, resolved, n.getDescriptor(), res);
                    }
                } catch (SemanticException e) { e.printStackTrace(); }
            }
        }
        return true;
    }

    private void process(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> tool, UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor, AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> res) throws SemanticException {
        if (descriptor.getAnnotations().contains(Taint.SINK_MATCHER)) {
            for (Expression par : uc.getParameters()) {
                AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(par);
                for (SymbolicExpression s : postState.getExecutionExpressions()) {
                    TaintThreeLevels domain = (TaintThreeLevels) tool.getAnalysis().domain.valueDomain;
                    TaintThreeLevelsLattice val = domain.eval(postState.getExecutionState().valueState, (ValueExpression) s, uc, tool.getAnalysis().domain.makeOracle(postState.getExecutionState()));
                    
                    if (val.isPossiblyTainted()) {
                        String msg = val.isAlwaysTainted() ? "TAINTED" : "POSSIBLY TAINTED";
                        tool.warnOn(uc, "There is a " + msg + " value in a sink: " + par.getLocation());
                    }
                }
            }
        }
    }
}