package it.unive.scsr.checkers;

import it.unive.lisa.analysis.Lattice;
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
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.NaryExpression;
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class AssignmentNotUsedChecker<H extends HeapValue<H>, V extends Lattice<V>, T extends TypeValue<T>>
        implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<V>, TypeEnvironment<T>>,
                SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<V>, TypeEnvironment<T>>> {

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<V>, TypeEnvironment<T>>,
                    SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<V>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if (!(node instanceof Assignment assignment))
            return true;

        if (!(assignment.getLeft() instanceof VariableRef))
            return true;

        String varName = ((VariableRef) assignment.getLeft()).getName();

        Set<Statement> visited = new HashSet<>();
        Queue<Statement> queue = new LinkedList<>();
        boolean foundOverwrite = false;

        for (Statement follower : graph.followersOf(node)) {
            queue.add(follower);
            visited.add(follower);
        }

        while (!queue.isEmpty()) {
            Statement current = queue.poll();

            if (readsVariable(current, varName))
                return true;

            if (overwritesVariable(current, varName)) {
                foundOverwrite = true;
                continue;
            }

            for (Statement follower : graph.followersOf(current))
                if (!visited.contains(follower)) {
                    visited.add(follower);
                    queue.add(follower);
                }
        }

        if (foundOverwrite)
            tool.warnOn(node,
                    "Assignment to '" + varName + "' not used (value overwritten before being read)");
        else
            tool.warnOn(node,
                    "Assignment to '" + varName + "' not used (dead store)");
        return true;
    }

    private static boolean readsVariable(Statement stmt, String varName) {
        if (stmt instanceof Assignment assignment) {
            if (assignment.getRight() instanceof VariableRef
                    && ((VariableRef) assignment.getRight()).getName().equals(varName))
                return true;
            return containsVariableRef(assignment.getRight(), varName);
        }

        if (stmt instanceof Return) {
            Expression expr = ((Return) stmt).getSubExpression();
            return containsVariableRef(expr, varName);
        }

        if (stmt instanceof Expression)
            return containsVariableRef((Expression) stmt, varName);

        return false;
    }

    private static boolean overwritesVariable(Statement stmt, String varName) {
        if (stmt instanceof Assignment assignment) {
            Expression target = assignment.getLeft();
            return target instanceof VariableRef
                    && ((VariableRef) target).getName().equals(varName);
        }
        return false;
    }

    private static boolean containsVariableRef(Expression expr, String varName) {
        if (expr instanceof VariableRef)
            return ((VariableRef) expr).getName().equals(varName);

        if (expr instanceof NaryExpression naryexpr)
            for (Expression sub : naryexpr.getSubExpressions())
                if (containsVariableRef(sub, varName))
                    return true;

        return false;
    }
}