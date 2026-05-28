package it.unive.scsr.checkers;

import java.util.Iterator;

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
import it.unive.lisa.imp.expressions.IMPArrayAccess;
import it.unive.lisa.lattices.Satisfiability;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.lattices.numeric.PentagonLattice;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.type.BoolType;
import it.unive.lisa.program.type.Int32Type;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;

public class ArrayLowerBoundPentagonChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (node instanceof IMPArrayAccess) {
            checkArrayAccess(tool, graph, (IMPArrayAccess) node);
        }
        return true;
    }

    private void checkArrayAccess(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
            CFG graph, IMPArrayAccess access) {

        Expression indexExpr = access.getRight();

        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(indexExpr);

            Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();
            if (!it.hasNext())
                continue;
            SymbolicExpression indexSymbolic = it.next();

            try {
                Constant zero = new Constant(Int32Type.INSTANCE, 0, access.getLocation());
                BinaryExpression predicate = new BinaryExpression(
                        BoolType.INSTANCE,
                        indexSymbolic,
                        zero,
                        ComparisonGe.INSTANCE,
                        access.getLocation());

                Satisfiability sat = tool.getAnalysis().domain.satisfies(
                        postState.getExecutionState(),
                        predicate,
                        access);

                if (sat == Satisfiability.NOT_SATISFIED)
                    tool.warnOn(access, "Array index is definitely negative — out-of-bounds access");
                else if (sat == Satisfiability.UNKNOWN)
                    tool.warnOn(access, "Array index may be negative — possible out-of-bounds access");
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }
}
