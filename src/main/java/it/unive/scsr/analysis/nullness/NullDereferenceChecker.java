package it.unive.scsr.analysis.nullness;

import java.util.HashSet;
import java.util.Set;

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
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.call.MultiCall;
import it.unive.lisa.program.cfg.statement.call.TruncatedParamsCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.type.Type;

public class NullDereferenceChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
    SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> {

  private final Set<String> emittedWarnings = new HashSet<>();

  private enum NullnessStatus {
    DEFINITELY_NULL,
    MAYBE_NULL,
    DEFINITELY_NOT_NULL,
    UNKNOWN
  }

  @Override
  public boolean visit(
      SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> tool,
      CFG graph,
      Statement node) {

    if (!(node instanceof UnresolvedCall uc))
      return true;

    var results = tool.getResultOf(graph);
    if (results == null)
      return true;

    for (var res : results) {
      try {
        Call resolved = tool.getResolvedVersion(uc, res);
        process(tool, uc, resolved, res);

      } catch (SemanticException e) {
        e.printStackTrace();
      }
    }

    return true;
  }

  private void process(
      SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> tool,
      UnresolvedCall uc,
      Call resolved,
      AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> res) {

    if (!mayDereferenceReceiver(uc, resolved) || uc.getParameters().length == 0)
      return;

    Expression receiverExpression = uc.getParameters()[0];
    String receiverName = receiverExpression.toString();

    try {
      AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> pre = res
          .getAnalysisStateBefore(uc);

      NullnessStatus status = receiverExpression instanceof VariableRef receiver
          ? statusOfVariable(pre, receiver)
          : statusOfExpression(tool, res, receiverExpression);

      if (status == NullnessStatus.DEFINITELY_NULL)
        warnOnce(tool, uc, "[NPE] '" + receiverName + "' is definitely null here.");
      else if (status == NullnessStatus.MAYBE_NULL)
        warnOnce(tool, uc,
            "[POSSIBLE_NPE] '" + receiverName
                + "' may be null here, depending on the execution path.");

    } catch (SemanticException e) {
      e.printStackTrace();
    }
  }

  private boolean mayDereferenceReceiver(UnresolvedCall uc, Call resolved) {
    if (uc.getCallType() == CallType.INSTANCE)
      return true;

    if (uc.getCallType() != CallType.UNKNOWN)
      return false;

    if (resolved instanceof TruncatedParamsCall)
      return false;

    if (resolved instanceof MultiCall multi) {
      for (Call call : multi.getCalls())
        if (mayDereferenceReceiver(uc, call))
          return true;

      return false;
    }

    return resolved.getCallType() != CallType.STATIC;
  }

  private NullnessStatus statusOfVariable(
      AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> state,
      VariableRef receiver) {

    ValueEnvironment<NullnessLattice> values = state.getExecutionState().valueState;
    TypeEnvironment<T> types = state.getExecutionState().typeState;
    Identifier id = receiver.getVariable();

    boolean hasValueInformation = values.isTop() || values.knowsIdentifier(id);
    NullnessStatus valueStatus = hasValueInformation
        ? statusOfValue(values.getState(id))
        : NullnessStatus.UNKNOWN;

    boolean hasTypeInformation = types.isTop() || types.knowsIdentifier(id);
    NullnessStatus typeStatus = hasTypeInformation
        ? statusOfType(types.getState(id))
        : NullnessStatus.UNKNOWN;

    return combineDomainStatuses(valueStatus, typeStatus);
  }

  private NullnessStatus statusOfExpression(
      SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> tool,
      AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> res,
      Expression receiverExpression)
      throws SemanticException {

    AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> receiverState = res
        .getAnalysisStateAfter(receiverExpression);

    NullnessStatus result = NullnessStatus.UNKNOWN;
    for (var expr : receiverState.getExecutionExpressions()) {
      NullnessStatus valueStatus = NullnessStatus.UNKNOWN;
      if (expr instanceof Identifier id) {
        ValueEnvironment<NullnessLattice> values = receiverState.getExecutionState().valueState;
        if (values.isTop() || values.knowsIdentifier(id))
          valueStatus = statusOfValue(values.getState(id));
      }

      NullnessStatus typeStatus = statusOfRuntimeTypes(
          tool.getAnalysis().getRuntimeTypesOf(receiverState, expr, receiverExpression));
      result = combineAlternativeStatuses(result, combineDomainStatuses(valueStatus, typeStatus));
    }

    return result;
  }

  private NullnessStatus statusOfValue(NullnessLattice value) {
    if (value.isDefinitelyNull())
      return NullnessStatus.DEFINITELY_NULL;
    if (value.isMaybeNull())
      return NullnessStatus.MAYBE_NULL;
    if (value.isDefinitelyNotNull())
      return NullnessStatus.DEFINITELY_NOT_NULL;
    return NullnessStatus.UNKNOWN;
  }

  private NullnessStatus statusOfType(T typeValue) {
    if (typeValue.isTop())
      return NullnessStatus.MAYBE_NULL;
    if (typeValue.isBottom())
      return NullnessStatus.UNKNOWN;
    return statusOfRuntimeTypes(typeValue.getRuntimeTypes());
  }

  private NullnessStatus statusOfRuntimeTypes(Set<Type> runtimeTypes) {
    if (runtimeTypes == null || runtimeTypes.isEmpty())
      return NullnessStatus.UNKNOWN;

    boolean hasNull = false;
    boolean hasNonNull = false;

    for (Type type : runtimeTypes) {
      if (isNullType(type))
        hasNull = true;
      else
        hasNonNull = true;
    }

    if (hasNull && !hasNonNull)
      return NullnessStatus.DEFINITELY_NULL;
    if (hasNull)
      return NullnessStatus.MAYBE_NULL;
    if (hasNonNull)
      return NullnessStatus.DEFINITELY_NOT_NULL;
    return NullnessStatus.UNKNOWN;
  }

  private boolean isNullType(Type type) {
    return type.isNullType()
        || (type.isPointerType() && type.asPointerType().getInnerType().isNullType());
  }

  private NullnessStatus combineDomainStatuses(NullnessStatus valueStatus, NullnessStatus typeStatus) {
    if (valueStatus == NullnessStatus.DEFINITELY_NULL || typeStatus == NullnessStatus.DEFINITELY_NULL)
      return NullnessStatus.DEFINITELY_NULL;
    if (valueStatus == NullnessStatus.MAYBE_NULL || typeStatus == NullnessStatus.MAYBE_NULL)
      return NullnessStatus.MAYBE_NULL;
    if (valueStatus == NullnessStatus.DEFINITELY_NOT_NULL || typeStatus == NullnessStatus.DEFINITELY_NOT_NULL)
      return NullnessStatus.DEFINITELY_NOT_NULL;
    return NullnessStatus.UNKNOWN;
  }

  private NullnessStatus combineAlternativeStatuses(NullnessStatus first, NullnessStatus second) {
    if (first == NullnessStatus.UNKNOWN)
      return second;
    if (second == NullnessStatus.UNKNOWN || first == second)
      return first;
    return NullnessStatus.MAYBE_NULL;
  }

  private void warnOnce(
      SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> tool,
      Statement node,
      String message) {

    if (emittedWarnings.add(node.getLocation() + "::" + message))
      tool.warnOn(node, message);
  }
}
