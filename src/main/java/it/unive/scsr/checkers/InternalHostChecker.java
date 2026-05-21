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
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.lattices.string.StrSuffix;
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
 * InternalHostChecker
 *
 * @brief A semantic checker over the {@link Suffix} abstract domain that detects hardcoded
 * {@code .internal} hostnames being passed as arguments to any call site.
 *
 * @note Internal hostnames (e.g., {@code vault.secrets.internal}, {@code prometheus.monitoring.internal})
 * 	are private infrastructure addresses that should never appear in externally observable
 * 	positions such as audit logs, routing tables, or external API dispatchers. Passing them
 * 	exposes internal network topology to potential attackers.
 *
 * @implNote the suffix domain tracks the longest known suffix of the full
 * 	string value. For a URL with a path component. For instance, in 
 * 	{@code "http://svc.internal/status"}, the {@code ".internal"} part is not visible to this domain.
 *
 * @param <H> heap abstract value type
 * @param <T> type abstract value type
 *
 * @author Gianmaria Pizzo 872966
 */
public class InternalHostChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
	SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrSuffix>,
	TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrSuffix>, TypeEnvironment<T>>> {

	/**
	 * Visits each statement in the CFG. 
	 * 
	 * @note Filters to {@link UnresolvedCall} nodes only, then resolves 
	 * 	each to its concrete target(s) (with either {@link NativeCall} or
	 * {@link CFGCall}) and delegates argument inspection to {@link #process}.
	 *
	 * @param tool the semantic analysis tool providing results and warning facilities
	 * @param graph the CFG being visited
	 * @param node the current statement
	 * @return {@code true} to continue visiting
	 */
	@Override
	public boolean visit(
		SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrSuffix>,
		TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrSuffix>,
		TypeEnvironment<T>>> tool, CFG graph, Statement node
	) {
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

	/**
	 * Inspects each non-receiver argument of a resolved call for a {@code .internal}
	 * suffix. 
	 * 
	 * @note It accumulates flagged parameter indices, then emits a single warning
	 * 	listing all offending positions.
	 *
	 * @param tool the semantic analysis tool
	 * @param uc the original unresolved call (carries argument expressions)
	 * @param resolved the resolved call (used to determine call type)
	 * @param descriptor the descriptor of the targeted CFG or native construct
	 * @param res the analysis result for the current context
	 */
	private void process(
		SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrSuffix>, TypeEnvironment<T>>,
		SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<StrSuffix>, TypeEnvironment<T>>> tool,
		UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
		AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrSuffix>, TypeEnvironment<T>>> res
	) {
		boolean[] paramsToWarn = new boolean[uc.getParameters().length];

		// For instance calls, parameter[0] is the receiver (`this`) — skip it
		for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0; i < uc.getParameters().length; i++) {
			Expression par = uc.getParameters()[i];

			// Post-state after evaluating this argument expression, not the call itself
			AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<StrSuffix>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(par);
			Set<SymbolicExpression> reachableIds = new HashSet<>();
			Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();

			if (comExprIterator.hasNext()) {
				SymbolicExpression boolExpr = comExprIterator.next();

				try {
					// Resolve all symbolic expressions reachable from this argument
					reachableIds.addAll(
						tool.getAnalysis().reachableFrom(postState, boolExpr, (Statement) uc).elements
					);

					for (SymbolicExpression s : reachableIds) {
						Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, (Statement) uc);

						// Skip heap locations and pointer types — only inspect string values
						if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType())){
							continue;
						}

						// Extract the StrSuffix abstract value for this argument
						ValueEnvironment<StrSuffix> valueState = postState.getExecutionState().valueState;
						Suffix analysisValueDomain = (Suffix) tool.getAnalysis().domain.valueDomain;
						SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
						StrSuffix abstractValue = analysisValueDomain.eval(valueState, (ValueExpression) s, (ProgramPoint) uc, oracle);

						// StrSuffix.suffix is the known suffix string
						// TOP has suffix ""
						if (abstractValue.suffix.endsWith(".internal")){
							paramsToWarn[i] = true;
						}
					}
				} catch (SemanticException e) {
					e.printStackTrace();
				}
			}
		}

		// Emit one warning per call listing all offending parameter indices
		if (requireWarning(paramsToWarn))
			tool.warnOn(uc, "The function uses a .internal host in the following parameters: " + prettyPrintParamsToWarn(paramsToWarn));
	}

	/**
	 * Returns {@code true} if at least one parameter was flagged.
	 *
	 * @param paramsToWarn per-parameter flag array
	 * @return {@code true} if any entry is {@code true}
	 */
	private boolean requireWarning(boolean[] paramsToWarn) {
		for (boolean p : paramsToWarn)
			if (p)
				return true;
		return false;
	}

	/**
	 * Formats the indices of flagged parameters as a human-readable ordinal list
	 * (e.g., {@code "1st, 3rd"}).
	 *
	 * @param paramsToWarn per-parameter flag array
	 * @return comma-separated ordinal string of flagged indices
	 */
	private String prettyPrintParamsToWarn(boolean[] paramsToWarn) {
		String res = "";
		for (int i = 0; i < paramsToWarn.length; i++) {
			if (paramsToWarn[i]) {
				res += (res.isEmpty() ? "" : ", ") + i;
				switch (i) {
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
