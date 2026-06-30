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
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.NativeCFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.type.Type;

public class NullDereferenceChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
    SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> {

  private final Set<String> emittedWarnings = new HashSet<>();

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

        if (resolved instanceof NativeCall nc) {
          for (NativeCFG n : nc.getTargetedConstructs())
            process(tool, uc, resolved, n.getDescriptor(), res);

        } else if (resolved instanceof CFGCall cfg) {
          for (CFG n : cfg.getTargetedCFGs())
            process(tool, uc, resolved, n.getDescriptor(), res);

        } else {
          process(tool, uc, resolved, null, res);
        }

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
      CodeMemberDescriptor descriptor,
      AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> res) {

    if (uc.getCallType() != CallType.INSTANCE || uc.getParameters().length == 0)
      return;

    Expression receiverExpression = uc.getParameters()[0];

    if (!(receiverExpression instanceof VariableRef receiver))
      return;

    try {
      AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> pre = res
          .getAnalysisStateBefore(uc);

      ValueEnvironment<NullnessLattice> values = pre.getExecutionState().valueState;
      TypeEnvironment<T> types = pre.getExecutionState().typeState;

      NullnessLattice value = null;
      for (Identifier id : values.getKeys())
        if (id.getName().equals(receiver.getName())) {
          value = values.getState(id);
          break;
        }

      T typeValue = null;
      for (Identifier id : types.getKeys())
        if (id.getName().equals(receiver.getName())) {
          typeValue = types.getState(id);
          break;
        }

      if (value != null && value.isDefinitelyNull()) {
        warnOnce(tool, uc, "[NPE] '" + receiver.getName() + "' is definitely null here.");
        return;
      }

      if (typeValue != null) {
        if (typeValue.isTop()) {
          warnOnce(tool, uc,
              "[POSSIBLE_NPE] '" + receiver.getName()
                  + "' may be null here, depending on the execution path.");
          return;
        }

        if (!typeValue.isBottom()) {
          boolean hasNull = false;
          boolean hasNonNull = false;

          for (Type type : typeValue.getRuntimeTypes()) {
            if (type.isNullType()
                || (type.isPointerType() && type.asPointerType().getInnerType().isNullType()))
              hasNull = true;
            else
              hasNonNull = true;
          }

          if (hasNull && !hasNonNull) {
            warnOnce(tool, uc, "[NPE] '" + receiver.getName() + "' is definitely null here.");
            return;
          }

          if (hasNull) {
            warnOnce(tool, uc,
                "[POSSIBLE_NPE] '" + receiver.getName()
                    + "' may be null here, depending on the execution path.");
            return;
          }

          if (hasNonNull)
            return;
        }
      }

      if (value != null && value.isMaybeNull())
        warnOnce(tool, uc,
            "[POSSIBLE_NPE] '" + receiver.getName()
                + "' may be null here, depending on the execution path.");

    } catch (SemanticException e) {
      e.printStackTrace();
    }
  }

  private void warnOnce(
      SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<NullnessLattice>, TypeEnvironment<T>>> tool,
      Statement node,
      String message) {

    if (emittedWarnings.add(node.getLocation() + "::" + message))
      tool.warnOn(node, message);
  }
}
