package it.unive.scsr.checkers;

import java.util.Collection;
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
import it.unive.lisa.program.cfg.controlFlow.ControlFlowStructure;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.type.Type;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevels;
import it.unive.scsr.analysis.taint.threelevels.TaintThreeLevelsLattice;

public class ImplicitFlow_Checker <H extends HeapValue<H>, T extends TypeValue<T>> implements
SemanticCheck<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> 
{
	@Override
	public boolean visit(
	SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> tool,
	CFG graph, Statement node) 
	{
		/*
		This piece of code retrieve the control flow graph structure (while loop/if-else statement) if there are.
		If the cfs is null, then the node is not a condition so it is not use for the implicit flow checker 
		*/
		ControlFlowStructure cfs = null;
		try {
        	cfs = graph.getControlFlowStructureOf(node);
    	} catch (IllegalArgumentException e) {}

    	if (cfs == null)
        	return true;
    	
		/*
		Here the checker checks if the condition of the control flow is tainted
		*/
    	for (var res : tool.getResultOf(graph)) 
		{
        	AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(node);
			boolean taint_check = false;

        	for (SymbolicExpression expr : postState.getExecutionExpressions()) 
			{
            	try {
                	Set<SymbolicExpression> reachableIds = new HashSet<>(tool.getAnalysis().reachableFrom(postState, expr, node).elements);

                	for (SymbolicExpression s : reachableIds) 
					{
                    	Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, node);

                    	if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                        	continue;

                    	TaintThreeLevels valueDomain = (TaintThreeLevels) tool.getAnalysis().domain.valueDomain;
                    	SemanticOracle oracle = tool.getAnalysis().domain.makeOracle(postState.getExecutionState());
                    	TaintThreeLevelsLattice val = valueDomain.eval(postState.getExecutionState().valueState, (ValueExpression) s, (ProgramPoint) node, oracle);

                    	if (val.isPossiblyTainted() || val.isAlwaysTainted()) 
                        	taint_check = true;
                	}
            	} catch (SemanticException e) {
                	e.printStackTrace();
            	}
				
				/*
				If at least one reachable identifier in the condition is tainted, the whole condition is treated 
				as tainted so no other conditions in the if-else/while loop structure is checked
				*/
            	if (taint_check) 
					break;
        	}

			if (taint_check) 
			{
				/*
				Warning on the tainted condition
				*/
        		tool.warnOn(node, "Tainted Condition, the implicit flow could affect variables in this branch");

				/*
				Checking the body of the tainted condition
				*/
        		inside_branch_checker(tool, graph, node, cfs);
    		}
    	}

    	return true;
	}

	private void inside_branch_checker(
    SemanticTool<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>, SimpleAbstractDomain<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> tool,
    CFG graph, Statement condition, ControlFlowStructure cfs)
    {
		/*
		The checker recover all the statements inside the body of the if-else/while loop structure
		*/
        Collection <Statement> a = cfs.getTargetedStatements();
		
		//This statement recover the first statement after the while loop/if-else 
		Statement exitNode = cfs.getFirstFollower();

        for (var res : tool.getResultOf(graph)) 
        {
            AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> conditionState = res.getAnalysisStateAfter(condition);

            for(Statement statement : a)
            {
				//Here i skip the first line of code after the conditional structure
				if (statement.equals(exitNode))
        			continue;

                AnalysisState<SimpleAbstractState<HeapEnvironment<H>, ValueEnvironment<TaintThreeLevelsLattice>, TypeEnvironment<T>>> postState = res.getAnalysisStateAfter(statement);

                Set<SymbolicExpression> reachableIds = new HashSet<>();
                Iterator<SymbolicExpression> it = postState.getExecutionExpressions().iterator();

                if(it.hasNext())
                {
                    try {
                        reachableIds.addAll(tool.getAnalysis().reachableFrom(postState, it.next(), statement).elements);

                        for (SymbolicExpression s : reachableIds) 
                        {
							//I use this if statement to filter all the variables, since i am interested in their state 
                            if (!(s instanceof Identifier)) 
                                continue;

                            Set<Type> types = tool.getAnalysis().getRuntimeTypesOf(postState, s, statement);
                            
                            if (types.stream().allMatch(t -> t.isInMemoryType() || t.isPointerType()))
                                continue;

                            TaintThreeLevels valueDomain = (TaintThreeLevels) tool.getAnalysis().domain.valueDomain;
                            
                            SemanticOracle oracle_prev = tool.getAnalysis().domain.makeOracle(conditionState.getExecutionState());

							//I check if the variable was tainted before the conditional structure 
                            TaintThreeLevelsLattice val = valueDomain.eval(conditionState.getExecutionState().valueState,(ValueExpression) s, (ProgramPoint) statement, oracle_prev);

                            if (val==null) 
                                continue;

							/*
							Here i am checking if the variable inside the conditional structure was clean before, this 
							means that since i am already considering a conditional structure with a tainted condition, 
							the variable that was clean before such structure, cannot be trusted anymore
							*/
                            if (!val.isAlwaysTainted() && !val.isPossiblyTainted())
                                tool.warnOn(statement, "Implicit flow detected at row " + statement.getLocation().getCodeLocation() +", variable modified inside a conditional statement, it should be considered tainted");
						}
               		}
                	catch (SemanticException e) {
                    	e.printStackTrace();
                	}    
                }
            }
        }
    }
}


