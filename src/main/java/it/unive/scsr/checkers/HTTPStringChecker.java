package it.unive.scsr.checkers;

import java.util.HashSet;
import java.util.Iterator;
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
import it.unive.lisa.analysis.string.Prefix;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.lattices.string.StrPrefix;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;

/**
 * It checks if there are calls where their parameters contain string values with the prefix "http://" or "https://"
 */
public class HTTPStringChecker <H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (node instanceof UnresolvedCall) { // check if the current visited node correspond to a function call
            UnresolvedCall uc = (UnresolvedCall) node;
            for (var res : tool.getResultOf(graph)) { // get analysis result for that graph
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
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> tool,
            UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
            AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> res) {

        boolean[] paramsToWarn = new boolean[uc.getParameters().length];
        for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
            Expression par = uc.getParameters()[i];
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> postState = res
                    .getAnalysisStateAfter(par); // compute the post state related to each parameter
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
                        //extraction of the abstract value
                        ValueEnvironment<StrPrefix> valueState = postState.getExecutionState().valueState;
                        Prefix analysisValueDomain = (Prefix) tool.getAnalysis().domain.valueDomain;
                        SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                        StrPrefix abstractValue = analysisValueDomain.eval(valueState, (ValueExpression) s,
                                (ProgramPoint) uc, oracle);

                        //check the abstractValue of the parameter
                        if (abstractValue.prefix.startsWith("http://") || abstractValue.prefix.startsWith("https://"))
                            paramsToWarn[i] = true;
                    }
                } catch (SemanticException e) {
                    e.printStackTrace();
                }

            }

        }
        if(requireWarning(paramsToWarn))
            tool.warnOn(uc, "The function uses an http/https url in the following parameters: " + prettyPrintParamsToWarn(paramsToWarn));
    }

    private boolean requireWarning(boolean[] paramsToWarn) {
        for(boolean p :paramsToWarn)
            if(p)
                return true;
        return false;
    }

    private String prettyPrintParamsToWarn(boolean[] paramsToWarn) {
        String res = "";
        for(int i= 0; i< paramsToWarn.length; i++) {
            if(paramsToWarn[i]) {
                res += (res.isEmpty() ? "" : ", ") + i;
                switch(i) {
                    case 1:
                        res += "st";
                        break;
                    case 2:
                        res += "nd";
                        break;
                    case 3:
                        res += "rd";
                        break;
                    default:
                        res += "th";
                }
            }
        }
        return res;
    }


}
