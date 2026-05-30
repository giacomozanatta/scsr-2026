package it.unive.scsr.checkers.project;

import it.unive.lisa.analysis.*;
import it.unive.lisa.analysis.nonrelational.heap.HeapEnvironment;
import it.unive.lisa.analysis.nonrelational.heap.HeapValue;
import it.unive.lisa.analysis.nonrelational.type.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.type.TypeValue;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;

import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.numeric.Pentagon;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.checks.semantic.SemanticTool;

import it.unive.lisa.imp.expressions.IMPArrayAccess;

import it.unive.lisa.imp.expressions.IMPNewArray;
import it.unive.lisa.lattices.SimpleAbstractState;
import it.unive.lisa.lattices.numeric.PentagonLattice;

import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;

import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;


import java.util.*;

public class ArrayBoundsChecker <H extends HeapValue<H>, T extends TypeValue<T>> implements
        SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> {

    HashMap<String, IntInterval> arrays = new HashMap<>(16);
    byte NEGPOSEXCPOS = (byte) 1 + (byte) 8;
    byte NEGPOS = (byte) 1;
    byte EXCPOS = (byte) 8;
    byte NEGDEF = (byte) 2;
    byte EXCDEF = (byte) 4;

    @Override
    public boolean visit(
            SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
            CFG graph, Statement node) {

        if(node instanceof Assignment assignment){
            if(assignment.getRight() instanceof IMPNewArray narr){
                Expression exp = narr.getSubExpressions()[0]; // N-ary expression (e.g new int[4]) i don't think imp really supports complicated expressions as array size
                IntInterval arr_bounds = get_interval(tool, graph, node, exp);
                if(assignment.getLeft() instanceof VariableRef var){
                    arrays.put(var.getName(), arr_bounds);
                }
                else{
                    throw new TypeNotPresentException("VariableRef", null);
                }
            }
        }
        if(node instanceof IMPArrayAccess access){
            check_out_of_bounds(tool, graph, access);
        }
        return true;
    }

    private void check_out_of_bounds(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
                               CFG graph, IMPArrayAccess access) {
        // We ignore statements that don't reference a variable of the current CFG, basically
        if(!(access.getLeft() instanceof VariableRef var)){
           return;
        }
        System.out.println("www.check.com: " + var.getVariable());
        Expression access_exp = access.getRight();
        PentagonLattice index_pentagon_lattice = get_pentagon_lattice(tool, graph, access, access_exp);
        IntInterval index_interval = get_interval(tool, graph, access, access_exp);

        if( !arrays.containsKey(var.getName()) || arrays.get(var.getName()) == null || index_interval == null){
            return;
        }

        byte access_type = 0;
        boolean flag_definite_neg;
        if(index_interval.getLow().lt(MathNumber.ZERO)){
            flag_definite_neg = index_interval.getHigh().lt(MathNumber.ZERO);
            access_type |= flag_definite_neg ? NEGDEF : NEGPOS ;
        }


        MathNumber size_high = arrays.get(var.getName()).getHigh();
        System.out.println("Checking interval " + index_interval + " against size high " + size_high);
        boolean flag_definite_exc;
        // TODO Use pentagon to make the analysis more precise
        if(index_interval.getHigh().geq(size_high)){
            flag_definite_exc = index_interval.getLow().geq(size_high);
            access_type |= flag_definite_exc ? EXCDEF : EXCPOS;
        }

        if(access_type == 0){
            return;
        }
        String msg = "Index is definitely exceeding size";
        if(access_type == NEGPOSEXCPOS){
            msg = "Index might be negative or exceeding size";
        }
        else if(access_type == NEGPOS){
            msg = "Index might be negative";
        }
        else if(access_type == EXCPOS){
            msg = "Index might be exceeding size";
        }
        else if(access_type == NEGDEF){
            msg = "Index is definitely negative";
        }

        tool.warnOn(access, msg);
    }

    private IntInterval get_interval(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
                                     CFG graph, Statement statement, Expression exp){
        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>>
                res : tool.getResultOf(graph))
        {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>>
                    postState = res.getAnalysisStateAfter(exp); // get post abstract state of denominator

            Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
            if(comExprIterator.hasNext()){
                SymbolicExpression boolExpr = comExprIterator.next();
                try{
                    Set<SymbolicExpression> reachableIds = new HashSet<>(tool.getAnalysis().reachableFrom(postState, boolExpr, exp).elements);
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());

                    for (SymbolicExpression s : reachableIds) {
                        Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, statement);

                        if (types.stream().allMatch(t -> !t.isNumericType() || t.isInMemoryType() || t.isPointerType()))
                            continue;

                        Collection<PentagonLattice> abstractValues = postState.getExecutionState()
                                .getAllLatticeInstances(PentagonLattice.class);

                        for (PentagonLattice a : abstractValues) {
                            ValueEnvironment<IntInterval> intervalEnv = a.first;
                            Interval intervalDomain = new Interval();
                            IntInterval i = intervalDomain.eval(intervalEnv, (ValueExpression) s, (ProgramPoint) exp, oracle);
                            if (i != null && !i.isBottom())
                                return i;
                        }
                    }
                } catch (SemanticException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private PentagonLattice get_pentagon_lattice(SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>> tool,
                                     CFG graph, Statement statement, Expression exp){
        for (AnalyzedCFG<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>>
                res : tool.getResultOf(graph))
        {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<PentagonLattice>, TypeEnvironment<T>>>
                    postState = res.getAnalysisStateAfter(exp); // get post abstract state of denominator

            Iterator<SymbolicExpression> comExprIterator = postState.getExecutionExpressions().iterator();
            if(comExprIterator.hasNext()){
                SymbolicExpression boolExpr = comExprIterator.next();
                try{
                    Set<SymbolicExpression> reachableIds = new HashSet<>(tool.getAnalysis().reachableFrom(postState, boolExpr, exp).elements);
                    SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());

                    for (SymbolicExpression s : reachableIds) {
                        Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, statement);

                        if (types.stream().allMatch(t -> !t.isNumericType() || t.isInMemoryType() || t.isPointerType()))
                            continue;

                        Collection<PentagonLattice> abstractValues = postState.getExecutionState()
                                .getAllLatticeInstances(PentagonLattice.class);

                        for (PentagonLattice a : abstractValues) {
                            if(a != null && !a.isBottom()){
                                return a;
                            }
                        }
                    }
                } catch (SemanticException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
