package it.unive.scsr.checkers;

import it.unive.lisa.analysis.*;
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
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
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
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SQLInjectionChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> {

    public static final Annotation SINK_ANNOTATION = new Annotation("lisa.sql.Sink");
    public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

    private boolean isDangerousPrefix(String prefix) {
        return prefix.startsWith("'")
                || prefix.startsWith("\"")
                || prefix.startsWith(";")
                || prefix.startsWith("--")
                || prefix.startsWith("/*")
                || prefix.startsWith("' OR")
                || prefix.startsWith("' AND")
                || prefix.startsWith("' UNION")
                || prefix.startsWith("'; DROP")
                || prefix.startsWith("1=1")
                || prefix.startsWith("OR 1=1")
                || prefix.startsWith("admin'");
    }

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (!(node instanceof UnresolvedCall))
            return true;

        UnresolvedCall uc = (UnresolvedCall) node;
        for (var res : tool.getResultOf(graph)) {
            try {
                Call resolved = tool.getResolvedVersion(uc, res);
                if (resolved instanceof NativeCall) {
                    var nativeCfgs = ((NativeCall) resolved).getTargetedConstructs();
                    for (NativeCFG n : nativeCfgs)
                        if (n.getDescriptor().getAnnotations().contains(SINK_MATCHER))
                            process(tool, uc, resolved, n.getDescriptor(), res);
                } else if (resolved instanceof CFGCall) {
                    CFGCall cfg = (CFGCall) resolved;
                    for (CFG n : cfg.getTargetedCFGs())
                        if (n.getDescriptor().getAnnotations().contains(SINK_MATCHER))
                            process(tool, uc, resolved, n.getDescriptor(), res);
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void process(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> tool,
            UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
            AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> res) {

        boolean[] paramsToWarn = new boolean[uc.getParameters().length];
        for (int i = resolved.getCallType() == Call.CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
            Expression par = uc.getParameters()[i];
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrPrefix>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(par);
            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
            if (comExprIterator.hasNext()) {
                SymbolicExpression boolExpr = comExprIterator.next();
                try {
                    reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, (Statement) uc).elements);

                    for (SymbolicExpression s : reachableIds) {
                        Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, (Statement) uc);

                        if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                            continue;

                        ValueEnvironment<StrPrefix> valueState = postState.getExecutionState().valueState;
                        Prefix analysisValueDomain = (Prefix) tool.getAnalysis().domain.valueDomain;
                        SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                        StrPrefix abstractValue = analysisValueDomain.eval(valueState, (ValueExpression) s, (ProgramPoint) uc, oracle);

                        if (!abstractValue.isTop() && !abstractValue.isBottom() && isDangerousPrefix(abstractValue.prefix))
                            paramsToWarn[i] = true;
                    }
                } catch (SemanticException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requireWarning(paramsToWarn))
            tool.warnOn(uc, "Potential SQL injection: dangerous string pattern detected in parameters passed to sink '"
                    + descriptor.getName() + "': "
                    + prettyPrintParamsToWarn(paramsToWarn));
    }

    private boolean requireWarning(boolean[] paramsToWarn) {
        for (boolean p : paramsToWarn)
            if (p)
                return true;
        return false;
    }

    private String prettyPrintParamsToWarn(boolean[] paramsToWarn) {
        String res = "";
        for (int i = 0; i < paramsToWarn.length; i++) {
            if (paramsToWarn[i]) {
                res += (res.isEmpty() ? "" : ", ") + i;
                switch (i) {
                    case 1: res += "st"; break;
                    case 2: res += "nd"; break;
                    case 3: res += "rd"; break;
                    default: res += "th";
                }
            }
        }
        return res;
    }
}