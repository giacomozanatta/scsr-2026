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
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;

import it.unive.scsr.analysis.extendedsign.ExtendedSign;
import it.unive.scsr.analysis.extendedsign.ExtendedSignLattice;

public class NegativeValueExtendedSignChecker
        <H extends HeapValue<H>, T extends TypeValue<T>>
        implements SemanticCheck<
        SimpleAbstractState<
                HeapEnvironment<H>,
                ValueEnvironment<ExtendedSignLattice>,
                TypeEnvironment<T>>,
        SimpleAbstractDomain<
                HeapEnvironment<H>,
                ValueEnvironment<ExtendedSignLattice>,
                TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<
                    SimpleAbstractState<
                            HeapEnvironment<H>,
                            ValueEnvironment<ExtendedSignLattice>,
                            TypeEnvironment<T>>,
                    SimpleAbstractDomain<
                            HeapEnvironment<H>,
                            ValueEnvironment<ExtendedSignLattice>,
                            TypeEnvironment<T>>> tool,
            CFG graph,
            Statement node) {

        checkNegativeValues(tool, graph, node);
        return true;
    }

    private void checkNegativeValues(
            SemanticTool<
                    SimpleAbstractState<
                            HeapEnvironment<H>,
                            ValueEnvironment<ExtendedSignLattice>,
                            TypeEnvironment<T>>,
                    SimpleAbstractDomain<
                            HeapEnvironment<H>,
                            ValueEnvironment<ExtendedSignLattice>,
                            TypeEnvironment<T>>> tool,
            CFG graph,
            Statement node) {

        for (AnalyzedCFG<
                SimpleAbstractState<
                        HeapEnvironment<H>,
                        ValueEnvironment<ExtendedSignLattice>,
                        TypeEnvironment<T>>> res
                : tool.getResultOf(graph)) {

            AnalysisState<
                    SimpleAbstractState<
                            HeapEnvironment<H>,
                            ValueEnvironment<ExtendedSignLattice>,
                            TypeEnvironment<T>>> postState =
                    res.getAnalysisStateAfter(node);

            Set<SymbolicExpression> reachableIds =
                    new HashSet<>();

            Iterator<SymbolicExpression> it =
                    postState.getExecutionExpressions().iterator();

            if (!it.hasNext())
                continue;

            SymbolicExpression expr = it.next();

            try {

                reachableIds.addAll(
                        tool.getAnalysis()
                                .reachableFrom(postState, expr, node)
                                .elements);

                for (SymbolicExpression s : reachableIds) {

                    Set<Type> types =
                            tool.getAnalysis()
                                    .getRuntimeTypesOf(postState, s, node);

                    if (types.stream().allMatch(
                            t -> t.isInMemoryType()
                                    || t.isPointerType()))
                        continue;

                    ValueEnvironment<ExtendedSignLattice> valueState =
                            postState.getExecutionState().valueState;

                    SemanticOracle oracle =
                            tool.getAnalysis()
                                    .domain
                                    .makeOracle(postState.getExecutionState());

                    ExtendedSign domain =
                            (ExtendedSign) tool.getAnalysis()
                                    .domain
                                    .valueDomain;

                    ExtendedSignLattice value =
                            domain.eval(
                                    valueState,
                                    (ValueExpression) s,
                                    (ProgramPoint) node,
                                    oracle);

                    if (value == ExtendedSignLattice.LT_ZERO)
                        tool.warnOn(
                                node,
                                "Value is definitely negative");

                    else if (value == ExtendedSignLattice.LEQ_ZERO)
                        tool.warnOn(
                                node,
                                "Value may be negative");
                }

            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }
}