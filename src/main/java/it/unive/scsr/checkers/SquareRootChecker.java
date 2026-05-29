package it.unive.scsr.checkers;

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
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.scsr.analysis.interval.FloatInterval;
import it.unive.scsr.analysis.interval.FloatIntervalLattice;

// This checker is realized using Float Interval domains to calculate the results and effectiveness of the square root, 
// by verifiying the validity and the calculations with Babylonian method

public class SquareRootChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<FloatIntervalLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<FloatIntervalLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<FloatIntervalLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<FloatIntervalLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (node instanceof Call) {
            Call c = (Call) node;
            if (c.getTargetName().equals("sqrt")) {
                for (var res : tool.getResultOf(graph)) {
                    AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<FloatIntervalLattice>, TypeEnvironment<T>>> postState = res
                            .getAnalysisStateAfter(
                                    c.getCallType() == CallType.STATIC ? c.getParameters()[0] : c.getParameters()[1]);
                    Set<SymbolicExpression> reachableIds = new HashSet<>();
                    Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
                    if (comExprIterator.hasNext()) {
                        SymbolicExpression boolExpr = comExprIterator.next();
                        try {
                            reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, node).elements);
                            for (SymbolicExpression s : reachableIds) {
                                Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, node);
                                if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                                    continue;

                                ValueEnvironment<FloatIntervalLattice> valueState = postState
                                        .getExecutionState().valueState;
                                FloatInterval domain = (FloatInterval) tool.getAnalysis().domain.valueDomain;
                                SemanticOracle oracle = tool.getAnalysis().domain
                                        .makeOracle(postState.getExecutionState());
                                FloatIntervalLattice abstractValue = domain.eval(valueState, (ValueExpression) s,
                                        (ProgramPoint) node, oracle);

                                if (abstractValue == FloatIntervalLattice.BOTTOM || abstractValue == null)
                                    continue;

                                if (abstractValue.getHigh().compareTo(MathNumber.ZERO) < 0)
                                    tool.warnOn(c, "sqrt argument is definitely negative " +
                                            abstractValue.representation() +
                                            " in " + graph.getDescriptor().getName());
                                else if (abstractValue.getLow().compareTo(MathNumber.ZERO) < 0)
                                    tool.warnOn(c, "sqrt argument may be negative " +
                                            abstractValue.representation() +
                                            " in " + graph.getDescriptor().getName());
                            }
                        } catch (SemanticException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return true;
    }
}