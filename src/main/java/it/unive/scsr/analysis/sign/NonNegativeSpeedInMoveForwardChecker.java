package it.unive.scsr.analysis.sign;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unive.lisa.analysis.AnalysisState;
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
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;

// This checker looks for functions whose names start with "moveForward".
// Inside these functions, it finds calls to "setSpeed".
// It checks the speed value and warns if it is negative or might be negative.
public class NonNegativeSpeedInMoveForwardChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<SignLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        // Check if the current function name starts with "moveForward"
        if (graph.getDescriptor().getName().startsWith("moveForward")) { 
            // Check if the current node is a function call
            if (node instanceof Call) { 
                Call c = (Call) node;
                // Check if the called function is "setSpeed"
                if (c.getTargetName().equals("setSpeed")) { 
                    for (var res : tool.getResultOf(graph)) { 
                        // Get the state of the first parameter passed to "setSpeed"
                        AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<SignLattice>, TypeEnvironment<T>>> postState = res
                                .getAnalysisStateAfter(c.getCallType() == CallType.STATIC ? c.getParameters()[0] : c.getParameters()[1]); 
                        Set<SymbolicExpression> reachableIds = new HashSet<>();
                        Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
                        if (comExprIterator.hasNext()) {

                            SymbolicExpression boolExpr = comExprIterator.next();
                            try {
                                reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr,
                                        (Statement) node).elements);

                                for (SymbolicExpression s : reachableIds) {
                                    Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s,
                                            (Statement) node);

                                    // Skip memory and pointer types
                                    if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                                        continue;

                                    // Extract the abstract value (the sign)
                                    ValueEnvironment<SignLattice> valueState = postState.getExecutionState().valueState;
                                    Sign signAnalysisValueDomain = (Sign) tool.getAnalysis().domain.valueDomain;
                                    SemanticOracle oracle = tool.getAnalysis().domain
                                            .makeOracle(postState.getExecutionState());
                                    SignLattice abstractValue = signAnalysisValueDomain.eval(valueState,
                                            (ValueExpression) s, (ProgramPoint) node, oracle);

                                    // Check the sign of the speed parameter and show a warning if needed
                                    if(abstractValue == SignLattice.NEG)
                                        tool.warnOn(c, "The speed is negative within the function " + graph.getDescriptor().getName());
                                    else if(abstractValue == SignLattice.TOP)
                                        tool.warnOn(c, "The speed may be negative within the function " + graph.getDescriptor().getName());
                                }
                            } catch (SemanticException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }

            }
        }
        return true;
    }

}