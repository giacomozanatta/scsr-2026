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
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.scsr.analysis.combined.SignTaint;
import it.unive.scsr.analysis.taint.Taint;

// This tool checks if dirty (tainted) variables are passed into sensitive functions (sinks).
// It specifically uses the SignTaint combined analysis to understand both sign and taint.
public class TaintThreeLevelsChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements 
    SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>, 
    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>> {

    // Visits every node in the program graph to look for function calls
    @Override
    public boolean visit(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>> tool, CFG graph, Statement node) {
        if (node instanceof UnresolvedCall) {
            UnresolvedCall uc = (UnresolvedCall) node;
            for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
                try {
                    // Try to understand what exact function is being called
                    Call resolved = tool.getResolvedVersion(uc, res);
                    if (resolved instanceof NativeCall) {
                        for (var n : ((NativeCall) resolved).getTargetedConstructs())
                            process(tool, uc, resolved, n.getDescriptor(), res);
                    } else if (resolved instanceof CFGCall) {
                        for (var n : ((CFGCall) resolved).getTargetedCFGs())
                            process(tool, uc, resolved, n.getDescriptor(), res);
                    }
                } catch (SemanticException e) {}
            }
        }
        return true;
    }

    // Checks the function and its arguments to see if it is a sink receiving bad data
    private void process(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>> tool, 
                         UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor, 
                         AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>> res) throws SemanticException {
        
        // If this function is marked as a SINK (dangerous place like a database query)
        if (descriptor.getAnnotations().contains(Taint.SINK_MATCHER)) {
            // Check all parameters passed to this sink
            for (Expression par : uc.getParameters()) {
                AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignTaint>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(par);
                
                for (SymbolicExpression s : postState.getExecutionExpressions()) {
                    // Get the combined SignTaint state for this variable
                    if (s instanceof Identifier) {
                        ValueEnvironment<SignTaint> valueEnv = postState.getExecutionState().valueState;
                        SignTaint combinedValue = valueEnv.getState((Identifier) s);

                        // If the variable might be dirty, print a warning in the console
                        if (combinedValue.getTaint().isPossiblyTainted()) {
                            String msg = combinedValue.getTaint().isAlwaysTainted() ? "TAINTED" : "POSSIBLY TAINTED";
                            tool.warnOn(uc, "There is a " + msg + " value in a sink: " + par.getLocation());
                        }
                    }
                }
            }
        }
    }
}