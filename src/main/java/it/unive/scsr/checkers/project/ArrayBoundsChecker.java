package it.unive.scsr.checkers.project;

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

import it.unive.lisa.imp.types.ArrayType;
import it.unive.lisa.lattices.SimpleAbstractState;

import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Statement;

import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.numeric.Division;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.numeric.IntInterval;

import java.util.*;

public class ArrayBoundsChecker <H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> {

    HashMap<String, IntInterval> arrays = new HashMap<>(16);

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        /* Whenever we assign to a var we need to check if that var is an
        * array and in that case remember its bounds (technically, its
        * upper bound is enough).
        *
        * */
        if(node instanceof Assignment assignment){
            if(!(assignment.getLeft() instanceof VariableRef varref))
                return true;
            store_array(tool, graph, assignment);
        }
        return true;
    }

    private void store_array(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> tool,
                               CFG graph, Assignment assignment) {
        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<IntInterval>, TypeEnvironment<T>>> res : tool.getResultOf(graph))
        {
            AnalysisState<
                    SimpleAbstractState<
                            HeapEnvironment<H>,
                            ValueEnvironment<IntInterval>,
                            TypeEnvironment<T>
                    >
            >
            postState = res.getAnalysisStateAfter(assignment.getRight()); // get post abstract state of denominator

            Set<SymbolicExpression> reachableIds = new HashSet<>();
            Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
            if (comExprIterator.hasNext()) {

                SymbolicExpression boolExpr = comExprIterator.next();
                try {
                    reachableIds
                            .addAll(tool.getAnalysis().reachableFrom(postState, boolExpr, assignment).elements);

                    for (SymbolicExpression s : reachableIds) {
                        Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, assignment);

                        if(types.stream().noneMatch(Type::isPointerType)){
                            continue;
                        }

                        tool.warnOn(assignment, "Found array assignment" + postState.getExecutionState().valueState.toString());


                    }
                } catch (SemanticException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
