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
import it.unive.scsr.analysis.parity.Parity;
import it.unive.scsr.analysis.parity.ParityLattice;

/*
 * Checks if an odd value is passed to a function annotated as requiring
 * an even-sized argument (e.g. allocateBuffer, writeUTF16).
 *
 * Real-world motivation: memory buffers for UTF-16 strings or DMA transfers
 * must be even-sized. An odd size causes misalignment bugs.
 *
 * - Definite warning: value is certainly ODD
 * - Possible warning: value is TOP (unknown parity)
 * - No warning:       value is EVEN or BOTTOM
 */
public class ParityChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<
            SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>,
        SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>> {

        // Names of functions that require even-sized arguments
        private static final Set<String> EVEN_REQUIRED_SINKS = Set.of(
                "allocateBuffer",
                "writeUTF16",
                "dmaTransfer"
        );

        @Override
        public boolean visit(
                SemanticTool<
                        SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>> tool,
                CFG graph, Statement node) {

            if (node instanceof UnresolvedCall) {
                UnresolvedCall uc = (UnresolvedCall) node;
                for (var res : tool.getResultOf(graph)) {
                    try {
                        Call resolved = tool.getResolvedVersion(uc, res);
                        if (resolved instanceof NativeCall) {
                            for (NativeCFG n : ((NativeCall) resolved).getTargetedConstructs())
                                process(tool, uc, resolved, n.getDescriptor(), res);
                        } else if (resolved instanceof CFGCall) {
                            for (CFG n : ((CFGCall) resolved).getTargetedCFGs())
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
                SemanticTool<
                        SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>> tool,
                UnresolvedCall uc, Call resolved, CodeMemberDescriptor descriptor,
                AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>> res) {

            // Only check calls to functions that require even-sized arguments
            if (!EVEN_REQUIRED_SINKS.contains(descriptor.getName()))
                return;

            for (int i = resolved.getCallType() == CallType.INSTANCE ? 1 : 0;
                 i < uc.getParameters().length; i++) {

                Expression par = uc.getParameters()[i];
                AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ParityLattice>, TypeEnvironment<T>>>
                        postState = res.getAnalysisStateAfter(par);

                Set<SymbolicExpression> reachableIds = new HashSet<>();
                Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
                if (!it.hasNext()) continue;

                SymbolicExpression expr = it.next();
                try {
                    reachableIds.addAll(
                            tool.getAnalysis().reachableFrom(postState, expr, (Statement) uc).elements);

                    for (SymbolicExpression s : reachableIds) {
                        Set<Type> types = tool.getAnalysis()
                                .getRuntimeTypesOf(postState, s, (Statement) uc);
                        if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                            continue;

                        ValueEnvironment<ParityLattice> valueState =
                                postState.getExecutionState().valueState;
                        Parity domain = (Parity) tool.getAnalysis().domain.valueDomain;
                        SemanticOracle oracle =
                                tool.getAnalysis().domain.makeOracle(postState.getExecutionState());

                        ParityLattice abstractValue = domain.eval(
                                valueState, (ValueExpression) s, (ProgramPoint) uc, oracle);

                        if (abstractValue.isOdd()) {
                            // Definite: size is always odd → alignment bug
                            tool.warnOn(uc, "Odd-sized argument certainly passed to '"
                                    + descriptor.getName()
                                    + "' at parameter " + i
                                    + " — even size required for alignment");
                        } else if (abstractValue.isTop()) {
                            // Possible: parity unknown → might be odd
                            tool.noticeOn(uc, "Argument of unknown parity passed to '"
                                    + descriptor.getName()
                                    + "' at parameter " + i
                                    + " — even size required, parity could not be determined");
                        }
                        // EVEN or BOTTOM → safe, no warning
                    }
                } catch (SemanticException e) {
                    e.printStackTrace();
                }
            }
        }
}