package it.unive.scsr.checkers;

import java.util.Iterator;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.SimpleAbstractDomain;
import it.unive.lisa.analysis.nonrelational.heap.HeapEnvironment;
import it.unive.lisa.analysis.nonrelational.heap.HeapValue;
import it.unive.lisa.analysis.nonrelational.type.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.type.TypeValue;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;

import it.unive.lisa.util.numeric.MathNumber;
import it.unive.scsr.analysis.cartesian.FloatIntervalThreeTaintLattice;
import it.unive.scsr.analysis.floatInterval.FloatIntervalLattice;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class UnboundedTaintedLoopChecker<H extends HeapValue<H>, T extends TypeValue<T>>
        implements SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<FloatIntervalThreeTaintLattice>, TypeEnvironment<T>>,
        SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<FloatIntervalThreeTaintLattice>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<FloatIntervalThreeTaintLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<FloatIntervalThreeTaintLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        boolean loopGuard = isLoopGuard(graph, node);
        if (node instanceof Expression) {
            System.out.println("[DEBUG] node=" + node.getClass().getSimpleName()
                    + " @ " + node.getLocation() + " isLoopGuard=" + loopGuard);
        }

        if (loopGuard) {
            Expression guard = (Expression) node;
            for (var res : tool.getResultOf(graph)) {
                try {
                    var post = res.getAnalysisStateAfter(guard);
                    Iterator<SymbolicExpression> it = post.getExecutionExpressions().iterator();
                    if (!it.hasNext()) { System.out.println("[DEBUG] nessuna execution expression per il guard"); continue; }
                    SymbolicExpression guardExpr = it.next();

                    if (guardExpr instanceof BinaryExpression) {
                        BinaryExpression cmp = (BinaryExpression) guardExpr;
                        checkOperand(tool, post, node, cmp.getLeft());
                        checkOperand(tool, post, node, cmp.getRight());
                    }
                } catch (SemanticException e) { e.printStackTrace(); }
            }
        }
        return true;
    }

    private void checkOperand(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<FloatIntervalThreeTaintLattice>, TypeEnvironment<T>>,
            SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<FloatIntervalThreeTaintLattice>, TypeEnvironment<T>>> tool,
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<FloatIntervalThreeTaintLattice>, TypeEnvironment<T>>> post,
            Statement node,
            SymbolicExpression operand) throws SemanticException {

        if (!(operand instanceof ValueExpression))
            return;

        var valueState = post.getExecutionState().valueState;
        var domain = (BaseNonRelationalValueDomain<FloatIntervalThreeTaintLattice>) tool.getAnalysis().domain.valueDomain;
        var oracle = tool.getAnalysis().domain.makeOracle(post.getExecutionState());

        FloatIntervalThreeTaintLattice abstractValue = domain.eval(valueState, (ValueExpression) operand, node, oracle);

        System.out.println("[DEBUG]   operand=" + operand
                + " taint=" + abstractValue.getTaint().representation()
                + " interval=" + abstractValue.getInterval().representation());

        boolean tainted = isHighTaint(abstractValue.getTaint());
        boolean unbounded = isUnboundedAbove(abstractValue.getInterval());

        if (tainted && unbounded) {
            tool.warnOn(node,
                    "Loop guard depends on a tainted, unbounded variable "
                            + "-> possible attacker-controlled iteration count (DoS)");
        }
    }

    private boolean isLoopGuard(CFG graph, Statement node) {
        if (!hasTrueAndFalseEdge(graph, node))
            return false;

        for (var edge : graph.getIngoingEdges(node)) {
            Statement source = (Statement) edge.getSource();
            if (isAfter(source, node))
                return true;
        }
        return false;
    }

    private boolean isAfter(Statement a, Statement b) {
        var locA = a.getLocation();
        var locB = b.getLocation();

        if (!(locA instanceof SourceCodeLocation)
                || !(locB instanceof SourceCodeLocation))
            return false;

        int lineA = ((SourceCodeLocation) locA).getLine();
        int lineB = ((SourceCodeLocation) locB).getLine();

        if (lineA == -1 || lineB == -1)
            return false;
        return lineA > lineB;
    }

    private boolean hasTrueAndFalseEdge(CFG graph, Statement node) {
        boolean hasTrue = false, hasFalse = false;
        for (var edge : graph.getOutgoingEdges(node)) {
            if (edge instanceof it.unive.lisa.program.cfg.edge.TrueEdge) hasTrue = true;
            if (edge instanceof it.unive.lisa.program.cfg.edge.FalseEdge) hasFalse = true;
        }
        return hasTrue && hasFalse;
    }

    private boolean isHighTaint(TaintThreeLevelsLattice taint) {
        return taint.isPossiblyTainted();
    }

    private boolean isUnboundedAbove(FloatIntervalLattice interval) {
        if (interval.isBottom())
            return false;
        MathNumber high = interval.getHigh();
        return high.equals(MathNumber.PLUS_INFINITY);
    }
}