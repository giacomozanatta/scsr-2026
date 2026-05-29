package it.unive.scsr.checkers;

import java.util.HashSet;
import java.util.Iterator;
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
import it.unive.lisa.symbolic.heap.AccessChild;
import it.unive.lisa.symbolic.heap.HeapDereference;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Variable;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.Untyped;

public class ArrayBoundsPentagonChecker<H extends HeapValue<H>, T extends TypeValue<T>> implements
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

        Expression arrayExpr = access.getLeft();
        Expression indexExpr = access.getRight();

        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> res : tool.getResultOf(graph)) {

            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> indexState = res.getAnalysisStateAfter(indexExpr);
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> arrayState = res.getAnalysisStateAfter(arrayExpr);

            Iterator<SymbolicExpression> indexIt = indexState.getExecutionExpressions().iterator();
            Iterator<SymbolicExpression> arrayIt = arrayState.getExecutionExpressions().iterator();
            if (!indexIt.hasNext() || !arrayIt.hasNext())
                continue;

            SymbolicExpression indexSymbolic = indexIt.next();
            SymbolicExpression arraySymbolic = arrayIt.next();

            try {
                checkLowerBound(tool, access, indexState, indexSymbolic);
                checkUpperBound(tool, access, indexState, arrayState, indexSymbolic, arraySymbolic);
            } catch (SemanticException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkLowerBound(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
            IMPArrayAccess access,
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> indexState,
            SymbolicExpression indexSymbolic) throws SemanticException {

        Constant zero = new Constant(Int32Type.INSTANCE, 0, access.getLocation());
        BinaryExpression predicate = new BinaryExpression(
                BoolType.INSTANCE, indexSymbolic, zero,
                ComparisonGe.INSTANCE, access.getLocation());

        Satisfiability sat = tool.getAnalysis().domain.satisfies(
                indexState.getExecutionState(), predicate, access);

        if (sat == Satisfiability.NOT_SATISFIED)
            tool.warnOn(access, "Array index is definitely negative — underflow");
        else if (sat == Satisfiability.UNKNOWN)
            tool.warnOn(access, "Array index may be negative — possible underflow");
    }

    private void checkUpperBound(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
            IMPArrayAccess access,
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> indexState,
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> arrayState,
            SymbolicExpression indexSymbolic,
            SymbolicExpression arraySymbolic) throws SemanticException {

        Type derefType = arrayElementType(tool, arrayState, arraySymbolic, access);
        if (derefType == null)
            return;

        HeapDereference deref = new HeapDereference(derefType, arraySymbolic, access.getLocation());
        Variable lenVar = new Variable(Untyped.INSTANCE, "len", access.getLocation());
        AccessChild lengthExpr = new AccessChild(Int32Type.INSTANCE, deref, lenVar, access.getLocation());

        BinaryExpression predicate = new BinaryExpression(
                BoolType.INSTANCE, indexSymbolic, lengthExpr,
                ComparisonLt.INSTANCE, access.getLocation());

        Satisfiability sat = tool.getAnalysis().domain.satisfies(
                indexState.getExecutionState(), predicate, access);

        if (sat == Satisfiability.NOT_SATISFIED)
            tool.warnOn(access, "Array index definitely exceeds length — out-of-bounds");
        else if (sat == Satisfiability.UNKNOWN)
            tool.warnOn(access, "Array index may exceed length — possible out-of-bounds");
    }

    private Type arrayElementType(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> arrayState,
            SymbolicExpression arraySymbolic,
            IMPArrayAccess access) throws SemanticException {

        Set<Type> runtimeTypes = tool.getAnalysis().getRuntimeTypesOf(arrayState, arraySymbolic, access);
        Set<Type> arrayTypes = new HashSet<>();
        for (Type t : runtimeTypes) {
            if (t.isPointerType() && t.asPointerType().getInnerType().isArrayType())
                arrayTypes.add(t.asPointerType().getInnerType());
        }
        if (arrayTypes.isEmpty())
            return null;
        return Type.commonSupertype(arrayTypes, access.getStaticType());
    }
}
