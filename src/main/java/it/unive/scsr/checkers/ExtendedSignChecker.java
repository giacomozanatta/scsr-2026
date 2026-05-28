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
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.Call;           // TODO: verify — may be UnresolvedCall
import it.unive.lisa.program.cfg.statement.numeric.Multiplication;
import it.unive.lisa.imp.expressions.IMPArrayAccess;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.scsr.analysis.extendedSign.ExtendedSign;
import it.unive.scsr.analysis.extendedSign.ExtendedSignLattice;

/**
 * A single checker for three sign-related properties, all powered by the
 * ExtendedSign domain. For each Division node the analysis produces a warning
 * if the sign of the relevant operand/result could be negative.
 *
 * CHECK 1 — Negative array index
 *   Intercepts:  ArrayAccess nodes
 *   Inspects:    the index sub-expression (subs[1])
 *   Warns when:  index sign ∈ { LT0, LEQ0, NEQ0, TOP }
 *
 * CHECK 2 — Sign-flip in multiplication
 *   Intercepts:  Multiplication nodes
 *   Inspects:    the result (post-state of the node itself)
 *   Warns when:  result sign ∈ { LT0, LEQ0, NEQ0, TOP }
 *
 * CHECK 3 — Negative argument passed to a method call
 *   Intercepts:  Call nodes
 *   Inspects:    each argument sub-expression (subs[1..n])
 *   Warns when:  argument sign ∈ { LT0, LEQ0, NEQ0, TOP }
 */
public class ExtendedSignChecker<H extends HeapValue<H>, T extends TypeValue<T>>
        implements SemanticCheck<
        SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
        SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> {

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    @Override
    public boolean visit(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            Statement node) {

        if (node instanceof IMPArrayAccess)
            checkNegativeArrayIndex(tool, graph, node);

        if (node instanceof Multiplication)
            checkMultiplicationSignFlip(tool, graph, node);

        if (node instanceof Call)
            checkNegativeCallArgument(tool, graph, node);

        return true;
    }

    // -------------------------------------------------------------------------
    // Check 1 — Negative array index
    // -------------------------------------------------------------------------

    private void checkNegativeArrayIndex(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            Statement node) {

        // Sub-expressions of an ArrayAccess: subs[0]=array, subs[1]=index
        IMPArrayAccess access = (IMPArrayAccess) node;
        Expression[] subs = access.getSubExpressions();
        if (subs == null || subs.length < 2) return;
        Expression indexNode = subs[1];

        for (ExtendedSignLattice sign : getSignsOf(tool, graph, indexNode)) {
            if (mightBeNegative(sign)) {
                tool.warnOn(node, sign == ExtendedSignLattice.LT0
                        ? "Negative array index: index is always negative"
                        : "Possible negative array index: index sign is "
                          + sign.representation() + " (includes negative values)");
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 2 — Sign-flip in multiplication
    // -------------------------------------------------------------------------

    private void checkMultiplicationSignFlip(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            Statement node) {

        // Evaluate the result of the multiplication (post-state of the node itself)
        Multiplication mul = (Multiplication) node;
        for (ExtendedSignLattice sign : getSignsOf(tool, graph, mul)) {
            if (mightBeNegativeFromMul(sign)) {
                tool.warnOn(node, sign == ExtendedSignLattice.LT0
                        ? "Multiplication result is always negative (sign flip)"
                        : "Multiplication result may be negative: result sign is "
                          + sign.representation());
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check 3 — Negative argument passed to a method call
    // -------------------------------------------------------------------------

    private void checkNegativeCallArgument(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            Statement node) {

        // For INSTANCE calls subs[0] is the receiver (this), args start at 1.
        // For STATIC/UNKNOWN calls there is no receiver, args start at 0.
        Call call = (Call) node;
        Expression[] subs = call.getParameters();
        if (subs == null || subs.length == 0) return;

        int startIdx = (call.getCallType() == CallType.INSTANCE) ? 1 : 0;

        for (int i = startIdx; i < subs.length; i++) {
            final int argPos = i; // 1-based argument position for the message
            for (ExtendedSignLattice sign : getSignsOf(tool, graph, subs[i])) {
                if (mightBeNegative(sign)) {
                    tool.warnOn(node, sign == ExtendedSignLattice.LT0
                            ? "Argument " + argPos + " is always negative"
                            : "Argument " + argPos + " may be negative: sign is "
                              + sign.representation());
                    break; // one warning per argument position
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Shared evaluation pipeline
    //
    // Evaluates the ExtendedSign abstract value of a given sub-statement across
    // all analyzed CFG results and returns the set of non-bottom signs found.
    // This avoids repeating the reachableFrom + eval boilerplate in each check.
    // -------------------------------------------------------------------------

    private Set<ExtendedSignLattice> getSignsOf(
            SemanticTool<
                    SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>> tool,
            CFG graph,
            Expression targetNode) {

        Set<ExtendedSignLattice> signs = new HashSet<>();

        for (AnalyzedCFG<
                SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>>
                res : tool.getResultOf(graph)) {

            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<ExtendedSignLattice>, TypeEnvironment<T>>>
                    state = res.getAnalysisStateAfter(targetNode);

            Iterator<SymbolicExpression> exprIt =
                    state.getExecutionExpressions().iterator();
            if (!exprIt.hasNext()) continue;

            SymbolicExpression rootExpr = exprIt.next();

            try {
                Set<SymbolicExpression> reachable = new HashSet<>();
                reachable.addAll(
                        tool.getAnalysis().reachableFrom(state, rootExpr, targetNode).elements);

                for (SymbolicExpression s : reachable) {
                    // Skip non-numeric expressions
                    Set<Type> types =
                            tool.getAnalysis().getRuntimeTypesOf(state, s, targetNode);
                    if (types.stream().allMatch(
                            t -> t.isInMemoryType() || t.isPointerType() || !t.isNumericType()))
                        continue;

                    var valueState = state.getExecutionState().valueState;
                    SemanticOracle oracle =
                            tool.getAnalysis().domain.makeOracle(state.getExecutionState());
                    if (!(tool.getAnalysis().domain.valueDomain instanceof ExtendedSign))
                        return signs; // checker requires ExtendedSign domain
                    ExtendedSign domain =
                            (ExtendedSign) tool.getAnalysis().domain.valueDomain;

                    ExtendedSignLattice sign = domain.eval(
                            valueState, (ValueExpression) s, (ProgramPoint) targetNode, oracle);

                    if (!sign.isBottom())
                        signs.add(sign);
                }
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }

        return signs;
    }

    // -------------------------------------------------------------------------
    // Sign classification helpers
    // -------------------------------------------------------------------------

    /**
     * For array index and call arguments: warn if the value could be negative.
     * TOP is included because passing an unknown value to a sensitive position
     * is worth flagging.
     *
     *   LT0  = { ..,-2,-1 }       → always negative
     *   LEQ0 = { ..,-1, 0 }       → may be negative
     *   NEQ0 = { ..,-1, 1,.. }    → may be negative
     *   TOP  = ℤ                  → may be negative
     *   ─────────────────────────────────
     *   GT0, GEQ0, EQ0            → safe
     */
    private boolean mightBeNegative(ExtendedSignLattice v) {
        return v == ExtendedSignLattice.LT0
                || v == ExtendedSignLattice.LEQ0
                || v == ExtendedSignLattice.NEQ0
                || v == ExtendedSignLattice.TOP;
    }

    /**
     * For multiplication: only warn when the sign is concretely negative or
     * mixed. TOP is excluded here to avoid false positives on methods that
     * receive unknown parameters and happen to contain a multiplication.
     *
     *   LT0  → always negative (definite sign flip)
     *   LEQ0 → may be negative
     *   NEQ0 → may be negative
     *   ─────────────────────────────────
     *   TOP  → too imprecise to blame the multiplication itself
     *   GT0, GEQ0, EQ0 → safe
     */
    private boolean mightBeNegativeFromMul(ExtendedSignLattice v) {
        return v == ExtendedSignLattice.LT0
                || v == ExtendedSignLattice.LEQ0
                || v == ExtendedSignLattice.NEQ0;
    }
}